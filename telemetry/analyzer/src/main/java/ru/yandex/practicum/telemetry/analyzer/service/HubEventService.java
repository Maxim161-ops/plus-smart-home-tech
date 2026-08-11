package ru.yandex.practicum.telemetry.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.telemetry.analyzer.model.Action;
import ru.yandex.practicum.telemetry.analyzer.model.Condition;
import ru.yandex.practicum.telemetry.analyzer.model.Scenario;
import ru.yandex.practicum.telemetry.analyzer.model.Sensor;
import ru.yandex.practicum.telemetry.analyzer.repository.ScenarioRepository;
import ru.yandex.practicum.telemetry.analyzer.repository.SensorRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class HubEventService {

    private final SensorRepository sensorRepository;
    private final ScenarioRepository scenarioRepository;

    /**
     * Определяет тип события хаба и передаёт его
     * соответствующему обработчику.
     */
    @Transactional
    public void handle(HubEventAvro event) {
        if (event == null) {
            throw new IllegalArgumentException(
                    "Событие хаба не должно быть null"
            );
        }

        Object payload = event.getPayload();

        switch (payload) {
            case DeviceAddedEventAvro deviceAdded ->
                    handleDeviceAdded(event, deviceAdded);

            case DeviceRemovedEventAvro deviceRemoved ->
                    handleDeviceRemoved(event, deviceRemoved);

            case ScenarioAddedEventAvro scenarioAdded ->
                    handleScenarioAdded(event, scenarioAdded);

            case ScenarioRemovedEventAvro scenarioRemoved ->
                    handleScenarioRemoved(event, scenarioRemoved);

            case null -> throw new IllegalArgumentException(
                    "Payload события хаба не должен быть null"
            );

            default -> throw new IllegalArgumentException(
                    "Неизвестный тип события хаба: "
                            + payload.getClass().getName()
            );
        }
    }

    private void handleDeviceAdded(
            HubEventAvro event,
            DeviceAddedEventAvro payload
    ) {
        String hubId = event.getHubId();
        String sensorId = payload.getId();

        /*
         * Повторное событие добавления допустимо.
         * Если датчик уже принадлежит этому хабу,
         * повторно сохранять его не нужно.
         */
        boolean sensorAlreadyExists = sensorRepository
                .findByIdAndHubId(sensorId, hubId)
                .isPresent();

        if (sensorAlreadyExists) {
            log.debug(
                    "Датчик уже существует: sensorId={}, hubId={}",
                    sensorId,
                    hubId
            );
            return;
        }

        /*
         * ID датчика является глобальным первичным ключом.
         * Если такой ID уже закреплён за другим хабом,
         * молча переносить датчик нельзя.
         */
        sensorRepository.findById(sensorId).ifPresent(existingSensor -> {
            throw new IllegalStateException(
                    "Датчик с id=" + sensorId
                            + " уже принадлежит хабу "
                            + existingSensor.getHubId()
            );
        });

        Sensor sensor = new Sensor(sensorId, hubId);
        sensorRepository.save(sensor);

        log.info(
                "Добавлен датчик: sensorId={}, hubId={}, type={}",
                sensorId,
                hubId,
                payload.getType()
        );
    }

    private void handleDeviceRemoved(
            HubEventAvro event,
            DeviceRemovedEventAvro payload
    ) {
        String hubId = event.getHubId();
        String sensorId = payload.getId();

        sensorRepository.findByIdAndHubId(sensorId, hubId)
                .ifPresentOrElse(
                        sensor -> {
                            List<Scenario> scenarios =
                                    scenarioRepository.findByHubId(hubId);

                            for (Scenario scenario : scenarios) {
                                scenario.getConditions().remove(sensor);
                                scenario.getActions().remove(sensor);
                            }

                            scenarioRepository.saveAll(scenarios);

                            sensorRepository.delete(sensor);

                            log.info(
                                    "Удалён датчик: sensorId={}, hubId={}",
                                    sensorId,
                                    hubId
                            );
                        },
                        () -> log.debug(
                                "Удаляемый датчик не найден: sensorId={}, hubId={}",
                                sensorId,
                                hubId
                        )
                );
    }

    private Sensor findSensor(String sensorId, String hubId) {
        return sensorRepository
                .findByIdAndHubId(sensorId, hubId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Датчик с id=" + sensorId
                                + " не найден в хабе " + hubId
                ));
    }

    private Integer convertConditionValue(Object value) {
        return switch (value) {
            case null -> null;
            case Integer integerValue -> integerValue;
            case Boolean booleanValue -> booleanValue ? 1 : 0;

            default -> throw new IllegalArgumentException(
                    "Неизвестный тип значения условия: "
                            + value.getClass().getName()
            );
        };
    }

    private Integer convertActionValue(Object value) {
        return switch (value) {
            case null -> null;
            case Integer integerValue -> integerValue;

            default -> throw new IllegalArgumentException(
                    "Неизвестный тип значения действия: "
                            + value.getClass().getName()
            );
        };
    }

    private void handleScenarioAdded(
            HubEventAvro event,
            ScenarioAddedEventAvro payload
    ) {
        String hubId = event.getHubId();
        String scenarioName = payload.getName();

        Map<Sensor, Condition> conditions = new HashMap<>();
        Map<Sensor, Action> actions = new HashMap<>();

        for (ScenarioConditionAvro conditionAvro : payload.getConditions()) {
            String sensorId = conditionAvro.getSensorId();

            Sensor sensor = findSensor(sensorId, hubId);

            Condition condition = new Condition();
            condition.setType(conditionAvro.getType());
            condition.setOperation(conditionAvro.getOperation());
            condition.setValue(convertConditionValue(conditionAvro.getValue()));

            conditions.put(sensor, condition);
        }

        for (DeviceActionAvro actionAvro : payload.getActions()) {
            String sensorId = actionAvro.getSensorId();

            Sensor sensor = findSensor(sensorId, hubId);

            Action action = new Action();
            action.setType(actionAvro.getType());
            action.setValue(convertActionValue(actionAvro.getValue()));

            actions.put(sensor, action);
        }

        Scenario scenario = scenarioRepository
                .findByHubIdAndName(hubId, scenarioName)
                .orElseGet(Scenario::new);

        scenario.setHubId(hubId);
        scenario.setName(scenarioName);

        /*
         * Если сценарий уже существовал, полностью заменяем
         * его старые условия и действия новой версией.
         */
        scenario.getConditions().clear();
        scenario.getConditions().putAll(conditions);

        scenario.getActions().clear();
        scenario.getActions().putAll(actions);

        scenarioRepository.save(scenario);

        log.info(
                "Сценарий сохранён: hubId={}, name={}, conditions={}, actions={}",
                hubId,
                scenarioName,
                conditions.size(),
                actions.size()
        );
    }

    private void handleScenarioRemoved(
            HubEventAvro event,
            ScenarioRemovedEventAvro payload
    ) {
        String hubId = event.getHubId();
        String scenarioName = payload.getName();

        scenarioRepository
                .findByHubIdAndName(hubId, scenarioName)
                .ifPresentOrElse(
                        scenario -> {
                            scenarioRepository.delete(scenario);

                            log.info(
                                    "Сценарий удалён: hubId={}, name={}",
                                    hubId,
                                    scenarioName
                            );
                        },
                        () -> log.debug(
                                "Удаляемый сценарий не найден: hubId={}, name={}",
                                hubId,
                                scenarioName
                        )
                );
    }
}
