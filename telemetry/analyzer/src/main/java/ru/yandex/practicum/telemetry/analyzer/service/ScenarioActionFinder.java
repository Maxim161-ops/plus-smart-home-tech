package ru.yandex.practicum.telemetry.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro;
import ru.yandex.practicum.telemetry.analyzer.model.Condition;
import ru.yandex.practicum.telemetry.analyzer.model.Scenario;
import ru.yandex.practicum.telemetry.analyzer.model.Sensor;
import ru.yandex.practicum.telemetry.analyzer.repository.ScenarioRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScenarioActionFinder {

    private final ScenarioRepository scenarioRepository;

    @Transactional(readOnly = true)
    public List<ActionCommand> findActions(
            SensorsSnapshotAvro snapshot
    ) {
        String hubId = snapshot.getHubId();

        List<Scenario> scenarios =
                scenarioRepository.findByHubId(hubId);

        log.debug(
                "Для хаба {} найдено сценариев: {}",
                hubId,
                scenarios.size()
        );

        List<ActionCommand> commands =
                new ArrayList<>();

        for (Scenario scenario : scenarios) {
            if (!checkScenario(snapshot, scenario)) {
                log.debug(
                        "Условия сценария '{}' хаба '{}' не выполнены",
                        scenario.getName(),
                        hubId
                );

                continue;
            }

            log.info(
                    "Сработал сценарий '{}' для хаба '{}'",
                    scenario.getName(),
                    hubId
            );

            scenario.getActions()
                    .forEach((sensor, action) ->
                            commands.add(
                                    new ActionCommand(
                                            hubId,
                                            scenario.getName(),
                                            sensor.getId(),
                                            action
                                    )
                            )
                    );
        }

        return commands;
    }

    private boolean checkScenario(
            SensorsSnapshotAvro snapshot,
            Scenario scenario
    ) {
        Map<Sensor, Condition> conditions =
                scenario.getConditions();

        if (conditions == null || conditions.isEmpty()) {
            log.debug(
                    "У сценария '{}' нет условий",
                    scenario.getName()
            );

            return false;
        }

        return conditions.entrySet()
                .stream()
                .allMatch(entry ->
                        checkCondition(
                                snapshot,
                                entry.getKey(),
                                entry.getValue()
                        )
                );
    }

    private boolean checkCondition(
            SensorsSnapshotAvro snapshot,
            Sensor sensor,
            Condition condition
    ) {
        SensorStateAvro state =
                snapshot.getSensorsState().get(sensor.getId());

        if (state == null) {
            log.debug(
                    "В снапшоте хаба {} отсутствует датчик {}",
                    snapshot.getHubId(),
                    sensor.getId()
            );

            return false;
        }

        Integer actualValue =
                extractValue(
                        state,
                        condition.getType()
                );

        Integer expectedValue =
                condition.getValue();

        if (actualValue == null) {
            log.debug(
                    "Не удалось получить значение типа {} для датчика {}",
                    condition.getType(),
                    sensor.getId()
            );

            return false;
        }

        if (expectedValue == null) {
            log.debug(
                    "У условия датчика {} отсутствует опорное значение",
                    sensor.getId()
            );

            return false;
        }

        boolean result = compare(
                actualValue,
                expectedValue,
                condition.getOperation()
        );

        log.debug(
                "Проверка условия: sensorId={}, type={}, actual={}, operation={}, expected={}, result={}",
                sensor.getId(),
                condition.getType(),
                actualValue,
                condition.getOperation(),
                expectedValue,
                result
        );

        return result;
    }

    private Integer extractValue(
            SensorStateAvro state,
            ConditionTypeAvro conditionType
    ) {
        Object data = state.getData();

        if (data == null) {
            return null;
        }

        return switch (conditionType) {
            case MOTION -> extractMotion(data);
            case LUMINOSITY -> extractLuminosity(data);
            case SWITCH -> extractSwitch(data);
            case TEMPERATURE -> extractTemperature(data);
            case CO2LEVEL -> extractCo2Level(data);
            case HUMIDITY -> extractHumidity(data);
        };
    }

    private Integer extractMotion(Object data) {
        if (data instanceof MotionSensorAvro motionSensor) {
            return motionSensor.getMotion() ? 1 : 0;
        }

        return null;
    }

    private Integer extractLuminosity(Object data) {
        if (data instanceof LightSensorAvro lightSensor) {
            return lightSensor.getLuminosity();
        }

        return null;
    }

    private Integer extractSwitch(Object data) {
        if (data instanceof SwitchSensorAvro switchSensor) {
            return switchSensor.getState() ? 1 : 0;
        }

        return null;
    }

    private Integer extractTemperature(Object data) {
        if (data instanceof TemperatureSensorAvro temperatureSensor) {
            return temperatureSensor.getTemperatureC();
        }

        if (data instanceof ClimateSensorAvro climateSensor) {
            return climateSensor.getTemperatureC();
        }

        return null;
    }

    private Integer extractCo2Level(Object data) {
        if (data instanceof ClimateSensorAvro climateSensor) {
            return climateSensor.getCo2Level();
        }

        return null;
    }

    private Integer extractHumidity(Object data) {
        if (data instanceof ClimateSensorAvro climateSensor) {
            return climateSensor.getHumidity();
        }

        return null;
    }

    private boolean compare(
            int actualValue,
            int expectedValue,
            ConditionOperationAvro operation
    ) {
        return switch (operation) {
            case EQUALS ->
                    actualValue == expectedValue;

            case GREATER_THAN ->
                    actualValue > expectedValue;

            case LOWER_THAN ->
                    actualValue < expectedValue;
        };
    }
}
