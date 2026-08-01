package ru.yandex.practicum.telemetry.collector.mapper.hub;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.telemetry.collector.model.hub.*;

@Component
public class HubEventAvroMapper {

    public HubEventAvro toAvro(HubEvent event) {
        if (event == null) {
            throw new IllegalArgumentException(
                    "Событие хаба для преобразования не должно быть null"
            );
        }

        return HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp())
                .setPayload(mapPayload(event))
                .build();
    }

    private Object mapPayload(HubEvent event) {
        return switch (event) {
            case DeviceAddedEvent deviceAdded ->
                    mapDeviceAdded(deviceAdded);

            case DeviceRemovedEvent deviceRemoved ->
                    mapDeviceRemoved(deviceRemoved);

            case ScenarioAddedEvent scenarioAdded ->
                    mapScenarioAdded(scenarioAdded);

            case ScenarioRemovedEvent scenarioRemoved ->
                    mapScenarioRemoved(scenarioRemoved);

            default -> throw new IllegalArgumentException(
                    "Неизвестный тип события хаба: "
                            + event.getClass().getName()
            );
        };
    }

    private DeviceAddedEventAvro mapDeviceAdded(
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

    private DeviceRemovedEventAvro mapDeviceRemoved(
            DeviceRemovedEvent event
    ) {
        return DeviceRemovedEventAvro.newBuilder()
                .setId(event.getId())
                .build();
    }

    private ScenarioAddedEventAvro mapScenarioAdded(
            ScenarioAddedEvent event
    ) {
        return ScenarioAddedEventAvro.newBuilder()
                .setName(event.getName())
                .setConditions(
                        event.getConditions()
                                .stream()
                                .map(this::mapCondition)
                                .toList()
                )
                .setActions(
                        event.getActions()
                                .stream()
                                .map(this::mapAction)
                                .toList()
                )
                .build();
    }

    private ScenarioRemovedEventAvro mapScenarioRemoved(
            ScenarioRemovedEvent event
    ) {
        return ScenarioRemovedEventAvro.newBuilder()
                .setName(event.getName())
                .build();
    }

    private ScenarioConditionAvro mapCondition(
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

    private DeviceActionAvro mapAction(DeviceAction action) {
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
