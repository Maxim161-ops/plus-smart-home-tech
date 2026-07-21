package ru.yandex.practicum.telemetry.collector.mapper;

import org.springframework.stereotype.Component;
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
import ru.yandex.practicum.telemetry.collector.model.hub.DeviceAction;
import ru.yandex.practicum.telemetry.collector.model.hub.DeviceAddedEvent;
import ru.yandex.practicum.telemetry.collector.model.hub.DeviceRemovedEvent;
import ru.yandex.practicum.telemetry.collector.model.hub.HubEvent;
import ru.yandex.practicum.telemetry.collector.model.hub.ScenarioAddedEvent;
import ru.yandex.practicum.telemetry.collector.model.hub.ScenarioCondition;
import ru.yandex.practicum.telemetry.collector.model.hub.ScenarioRemovedEvent;

@Component
public class HubEventMapper {

    /**
     * Преобразует HTTP-модель события хаба в Avro-модель для Kafka.
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
                .setPayload(mapPayload(event))
                .build();
    }

    private Object mapPayload(HubEvent event) {
        return switch (event) {
            case DeviceAddedEvent deviceAddedEvent ->
                    mapDeviceAddedEvent(deviceAddedEvent);

            case DeviceRemovedEvent deviceRemovedEvent ->
                    mapDeviceRemovedEvent(deviceRemovedEvent);

            case ScenarioAddedEvent scenarioAddedEvent ->
                    mapScenarioAddedEvent(scenarioAddedEvent);

            case ScenarioRemovedEvent scenarioRemovedEvent ->
                    mapScenarioRemovedEvent(scenarioRemovedEvent);

            default -> throw new IllegalArgumentException(
                    "Неизвестный тип события хаба: "
                            + event.getClass().getName()
            );
        };
    }

    private DeviceAddedEventAvro mapDeviceAddedEvent(DeviceAddedEvent event) {
        return DeviceAddedEventAvro.newBuilder()
                .setId(event.getId())
                .setType(DeviceTypeAvro.valueOf(event.getDeviceType().name()))
                .build();
    }

    private DeviceRemovedEventAvro mapDeviceRemovedEvent(DeviceRemovedEvent event) {
        return DeviceRemovedEventAvro.newBuilder()
                .setId(event.getId())
                .build();
    }

    private ScenarioAddedEventAvro mapScenarioAddedEvent(ScenarioAddedEvent event) {
        return ScenarioAddedEventAvro.newBuilder()
                .setName(event.getName())
                .setConditions(
                        event.getConditions()
                                .stream()
                                .map(this::mapScenarioCondition)
                                .toList()
                )
                .setActions(
                        event.getActions()
                                .stream()
                                .map(this::mapDeviceAction)
                                .toList()
                )
                .build();
    }

    private ScenarioRemovedEventAvro mapScenarioRemovedEvent(
            ScenarioRemovedEvent event
    ) {
        return ScenarioRemovedEventAvro.newBuilder()
                .setName(event.getName())
                .build();
    }

    private ScenarioConditionAvro mapScenarioCondition(
            ScenarioCondition condition
    ) {
        return ScenarioConditionAvro.newBuilder()
                .setSensorId(condition.getSensorId())
                .setType(ConditionTypeAvro.valueOf(condition.getType().name()))
                .setOperation(
                        ConditionOperationAvro.valueOf(
                                condition.getOperation().name()
                        )
                )
                .setValue(condition.getValue())
                .build();
    }

    private DeviceActionAvro mapDeviceAction(DeviceAction action) {
        return DeviceActionAvro.newBuilder()
                .setSensorId(action.getSensorId())
                .setType(ActionTypeAvro.valueOf(action.getType().name()))
                .setValue(action.getValue())
                .build();
    }
}
