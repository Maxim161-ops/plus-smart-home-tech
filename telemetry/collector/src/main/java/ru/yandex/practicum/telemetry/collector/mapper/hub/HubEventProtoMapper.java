package ru.yandex.practicum.telemetry.collector.mapper.hub;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioConditionProto;
import ru.yandex.practicum.telemetry.collector.model.hub.*;

import java.time.Instant;

@Component
public class HubEventProtoMapper {

    public HubEvent toModel(HubEventProto event) {
        if (event == null) {
            throw new IllegalArgumentException(
                    "Событие хаба для преобразования не должно быть null"
            );
        }

        return switch (event.getPayloadCase()) {
            case DEVICE_ADDED -> mapDeviceAdded(event);
            case DEVICE_REMOVED -> mapDeviceRemoved(event);
            case SCENARIO_ADDED -> mapScenarioAdded(event);
            case SCENARIO_REMOVED -> mapScenarioRemoved(event);

            case PAYLOAD_NOT_SET -> throw new IllegalArgumentException(
                    "Payload события хаба не задан"
            );
        };
    }

    private DeviceAddedEvent mapDeviceAdded(HubEventProto event) {
        DeviceAddedEvent result = new DeviceAddedEvent();

        setCommonFields(result, event);

        result.setId(event.getDeviceAdded().getId());
        result.setDeviceType(
                DeviceType.valueOf(
                        event.getDeviceAdded().getType().name()
                )
        );

        return result;
    }

    private DeviceRemovedEvent mapDeviceRemoved(HubEventProto event) {
        DeviceRemovedEvent result = new DeviceRemovedEvent();

        setCommonFields(result, event);
        result.setId(event.getDeviceRemoved().getId());

        return result;
    }

    private ScenarioAddedEvent mapScenarioAdded(HubEventProto event) {
        ScenarioAddedEvent result = new ScenarioAddedEvent();

        setCommonFields(result, event);

        result.setName(event.getScenarioAdded().getName());

        result.setConditions(
                event.getScenarioAdded()
                        .getConditionList()
                        .stream()
                        .map(this::mapCondition)
                        .toList()
        );

        result.setActions(
                event.getScenarioAdded()
                        .getActionList()
                        .stream()
                        .map(this::mapAction)
                        .toList()
        );

        return result;
    }

    private ScenarioRemovedEvent mapScenarioRemoved(HubEventProto event) {
        ScenarioRemovedEvent result = new ScenarioRemovedEvent();

        setCommonFields(result, event);
        result.setName(event.getScenarioRemoved().getName());

        return result;
    }

    private ScenarioCondition mapCondition(
            ScenarioConditionProto condition
    ) {
        ScenarioCondition result = new ScenarioCondition();

        result.setSensorId(condition.getSensorId());
        result.setType(
                ConditionType.valueOf(condition.getType().name())
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

    private DeviceAction mapAction(DeviceActionProto action) {
        DeviceAction result = new DeviceAction();

        result.setSensorId(action.getSensorId());
        result.setType(
                ActionType.valueOf(action.getType().name())
        );
        result.setValue(
                action.hasValue() ? action.getValue() : null
        );

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
}
