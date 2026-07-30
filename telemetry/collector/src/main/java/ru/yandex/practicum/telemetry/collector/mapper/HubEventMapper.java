package ru.yandex.practicum.telemetry.collector.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioConditionProto;
import ru.yandex.practicum.kafka.telemetry.event.ActionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;
import ru.yandex.practicum.telemetry.collector.model.hub.ActionType;
import ru.yandex.practicum.telemetry.collector.model.hub.ConditionOperation;
import ru.yandex.practicum.telemetry.collector.model.hub.ConditionType;
import ru.yandex.practicum.telemetry.collector.model.hub.DeviceAction;
import ru.yandex.practicum.telemetry.collector.model.hub.DeviceAddedEvent;
import ru.yandex.practicum.telemetry.collector.model.hub.DeviceRemovedEvent;
import ru.yandex.practicum.telemetry.collector.model.hub.DeviceType;
import ru.yandex.practicum.telemetry.collector.model.hub.HubEvent;
import ru.yandex.practicum.telemetry.collector.model.hub.ScenarioAddedEvent;
import ru.yandex.practicum.telemetry.collector.model.hub.ScenarioCondition;
import ru.yandex.practicum.telemetry.collector.model.hub.ScenarioRemovedEvent;

import java.time.Instant;

@Component
public class HubEventMapper {

    /**
     * Преобразует внутреннюю модель события хаба
     * в Avro-модель для отправки в Kafka.
     */
    public HubEventAvro toAvro(HubEvent event) {
        if (event == null) {
            throw new IllegalArgumentException(
                    "Событие хаба для преобразования не должно быть null"
            );
        }

        return HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp())
                .setPayload(mapPayloadToAvro(event))
                .build();
    }

    /**
     * Преобразует полученное по gRPC событие
     * во внутреннюю модель приложения.
     */
    public HubEvent toModel(HubEventProto event) {
        if (event == null) {
            throw new IllegalArgumentException(
                    "Событие хаба для преобразования не должно быть null"
            );
        }

        return switch (event.getPayloadCase()) {
            case DEVICE_ADDED -> mapDeviceAddedToModel(event);
            case DEVICE_REMOVED -> mapDeviceRemovedToModel(event);
            case SCENARIO_ADDED -> mapScenarioAddedToModel(event);
            case SCENARIO_REMOVED -> mapScenarioRemovedToModel(event);

            case PAYLOAD_NOT_SET -> throw new IllegalArgumentException(
                    "Payload события хаба не задан"
            );
        };
    }

    /*
     * Proto -> внутренняя модель
     */

    private DeviceAddedEvent mapDeviceAddedToModel(HubEventProto event) {
        DeviceAddedEvent result = new DeviceAddedEvent();

        setCommonFields(result, event);

        result.setId(event.getDeviceAdded().getId());

        result.setDeviceType(
                DeviceType.valueOf(
                        event.getDeviceAdded()
                                .getType()
                                .name()
                )
        );

        return result;
    }

    private DeviceRemovedEvent mapDeviceRemovedToModel(HubEventProto event) {
        DeviceRemovedEvent result = new DeviceRemovedEvent();

        setCommonFields(result, event);

        result.setId(
                event.getDeviceRemoved().getId()
        );

        return result;
    }

    private ScenarioAddedEvent mapScenarioAddedToModel(HubEventProto event) {
        ScenarioAddedEvent result = new ScenarioAddedEvent();

        setCommonFields(result, event);

        result.setName(
                event.getScenarioAdded().getName()
        );

        result.setConditions(
                event.getScenarioAdded()
                        .getConditionList()
                        .stream()
                        .map(this::mapScenarioConditionToModel)
                        .toList()
        );

        result.setActions(
                event.getScenarioAdded()
                        .getActionList()
                        .stream()
                        .map(this::mapDeviceActionToModel)
                        .toList()
        );

        return result;
    }

    private ScenarioRemovedEvent mapScenarioRemovedToModel(
            HubEventProto event
    ) {
        ScenarioRemovedEvent result = new ScenarioRemovedEvent();

        setCommonFields(result, event);

        result.setName(
                event.getScenarioRemoved().getName()
        );

        return result;
    }

    private ScenarioCondition mapScenarioConditionToModel(
            ScenarioConditionProto condition
    ) {
        ScenarioCondition result = new ScenarioCondition();

        result.setSensorId(condition.getSensorId());

        result.setType(
                ConditionType.valueOf(
                        condition.getType().name()
                )
        );

        result.setOperation(
                ConditionOperation.valueOf(
                        condition.getOperation().name()
                )
        );

        result.setValue(
                switch (condition.getValueCase()) {
                    case BOOL_VALUE -> condition.getBoolValue() ? 1 : 0;
                    case INT_VALUE -> condition.getIntValue();

                    case VALUE_NOT_SET -> throw new IllegalArgumentException(
                            "Значение условия сценария не задано"
                    );
                }
        );

        return result;
    }

    private DeviceAction mapDeviceActionToModel(
            DeviceActionProto action
    ) {
        DeviceAction result = new DeviceAction();

        result.setSensorId(action.getSensorId());

        result.setType(
                ActionType.valueOf(
                        action.getType().name()
                )
        );

        if (action.hasValue()) {
            result.setValue(action.getValue());
        } else {
            result.setValue(null);
        }

        return result;
    }

    private void setCommonFields(
            HubEvent result,
            HubEventProto source
    ) {
        result.setHubId(source.getHubId());

        result.setTimestamp(
                Instant.ofEpochSecond(
                        source.getTimestamp().getSeconds(),
                        source.getTimestamp().getNanos()
                )
        );
    }

    /*
     * Внутренняя модель -> Avro
     */

    private Object mapPayloadToAvro(HubEvent event) {
        return switch (event) {
            case DeviceAddedEvent deviceAddedEvent ->
                    mapDeviceAddedToAvro(deviceAddedEvent);

            case DeviceRemovedEvent deviceRemovedEvent ->
                    mapDeviceRemovedToAvro(deviceRemovedEvent);

            case ScenarioAddedEvent scenarioAddedEvent ->
                    mapScenarioAddedToAvro(scenarioAddedEvent);

            case ScenarioRemovedEvent scenarioRemovedEvent ->
                    mapScenarioRemovedToAvro(scenarioRemovedEvent);

            default -> throw new IllegalArgumentException(
                    "Неизвестный тип события хаба: "
                            + event.getClass().getName()
            );
        };
    }

    private DeviceAddedEventAvro mapDeviceAddedToAvro(
            DeviceAddedEvent event
    ) {
        return DeviceAddedEventAvro.newBuilder()
                .setId(event.getId())
                .setType(
                        DeviceTypeAvro.valueOf(
                                event.getDeviceType().name()
                        )
                )
                .build();
    }

    private DeviceRemovedEventAvro mapDeviceRemovedToAvro(
            DeviceRemovedEvent event
    ) {
        return DeviceRemovedEventAvro.newBuilder()
                .setId(event.getId())
                .build();
    }

    private ScenarioAddedEventAvro mapScenarioAddedToAvro(
            ScenarioAddedEvent event
    ) {
        return ScenarioAddedEventAvro.newBuilder()
                .setName(event.getName())
                .setConditions(
                        event.getConditions()
                                .stream()
                                .map(this::mapScenarioConditionToAvro)
                                .toList()
                )
                .setActions(
                        event.getActions()
                                .stream()
                                .map(this::mapDeviceActionToAvro)
                                .toList()
                )
                .build();
    }

    private ScenarioRemovedEventAvro mapScenarioRemovedToAvro(
            ScenarioRemovedEvent event
    ) {
        return ScenarioRemovedEventAvro.newBuilder()
                .setName(event.getName())
                .build();
    }

    private ScenarioConditionAvro mapScenarioConditionToAvro(
            ScenarioCondition condition
    ) {
        return ScenarioConditionAvro.newBuilder()
                .setSensorId(condition.getSensorId())
                .setType(
                        ConditionTypeAvro.valueOf(
                                condition.getType().name()
                        )
                )
                .setOperation(
                        ConditionOperationAvro.valueOf(
                                condition.getOperation().name()
                        )
                )
                .setValue(condition.getValue())
                .build();
    }

    private DeviceActionAvro mapDeviceActionToAvro(
            DeviceAction action
    ) {
        return DeviceActionAvro.newBuilder()
                .setSensorId(action.getSensorId())
                .setType(
                        ActionTypeAvro.valueOf(
                                action.getType().name()
                        )
                )
                .setValue(action.getValue())
                .build();
    }
}
