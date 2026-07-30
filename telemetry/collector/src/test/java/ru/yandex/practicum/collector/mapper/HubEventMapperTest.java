package ru.yandex.practicum.collector.mapper;

import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.ConditionOperationProto;
import ru.yandex.practicum.grpc.telemetry.event.ConditionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioAddedEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioConditionProto;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.telemetry.collector.mapper.HubEventMapper;
import ru.yandex.practicum.telemetry.collector.model.hub.DeviceAddedEvent;
import ru.yandex.practicum.telemetry.collector.model.hub.DeviceType;
import ru.yandex.practicum.telemetry.collector.model.hub.HubEvent;
import ru.yandex.practicum.telemetry.collector.model.hub.ScenarioAddedEvent;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HubEventMapperTest {

    private final HubEventMapper mapper = new HubEventMapper();

    @Test
    void shouldMapDeviceAddedEventToAvro() {
        Instant timestamp = Instant.parse("2026-07-16T09:05:00Z");

        DeviceAddedEvent event = new DeviceAddedEvent();
        event.setHubId("hub-1");
        event.setTimestamp(timestamp);
        event.setId("sensor.light.1");
        event.setDeviceType(DeviceType.LIGHT_SENSOR);

        HubEventAvro result = mapper.toAvro(event);

        assertEquals("hub-1", result.getHubId());
        assertEquals(timestamp, result.getTimestamp());
        assertInstanceOf(DeviceAddedEventAvro.class, result.getPayload());

        DeviceAddedEventAvro payload =
                (DeviceAddedEventAvro) result.getPayload();

        assertEquals("sensor.light.1", payload.getId());
        assertEquals(DeviceTypeAvro.LIGHT_SENSOR, payload.getType());
    }

    @Test
    void shouldMapScenarioAddedProtoToModel() {
        Timestamp timestamp = Timestamp.newBuilder()
                .setSeconds(1_700_000_000L)
                .setNanos(123_000_000)
                .build();

        ScenarioConditionProto condition =
                ScenarioConditionProto.newBuilder()
                        .setSensorId("temperature-sensor-1")
                        .setType(ConditionTypeProto.TEMPERATURE)
                        .setOperation(
                                ConditionOperationProto.GREATER_THAN
                        )
                        .setIntValue(25)
                        .build();

        DeviceActionProto action =
                DeviceActionProto.newBuilder()
                        .setSensorId("climate-device-1")
                        .setType(ActionTypeProto.SET_VALUE)
                        .setValue(20)
                        .build();

        ScenarioAddedEventProto scenario =
                ScenarioAddedEventProto.newBuilder()
                        .setName("Комфортная температура")
                        .addCondition(condition)
                        .addAction(action)
                        .build();

        HubEventProto proto = HubEventProto.newBuilder()
                .setHubId("hub-1")
                .setTimestamp(timestamp)
                .setScenarioAdded(scenario)
                .build();

        HubEvent result = mapper.toModel(proto);

        ScenarioAddedEvent mappedEvent =
                assertInstanceOf(ScenarioAddedEvent.class, result);

        assertEquals("hub-1", mappedEvent.getHubId());
        assertEquals(
                Instant.ofEpochSecond(
                        timestamp.getSeconds(),
                        timestamp.getNanos()
                ),
                mappedEvent.getTimestamp()
        );

        assertEquals(
                "Комфортная температура",
                mappedEvent.getName()
        );

        assertNotNull(mappedEvent.getConditions());
        assertEquals(1, mappedEvent.getConditions().size());

        assertEquals(
                "temperature-sensor-1",
                mappedEvent.getConditions()
                        .getFirst()
                        .getSensorId()
        );

        assertEquals(
                25,
                mappedEvent.getConditions()
                        .getFirst()
                        .getValue()
        );

        assertNotNull(mappedEvent.getActions());
        assertEquals(1, mappedEvent.getActions().size());

        assertEquals(
                "climate-device-1",
                mappedEvent.getActions()
                        .getFirst()
                        .getSensorId()
        );

        assertEquals(
                20,
                mappedEvent.getActions()
                        .getFirst()
                        .getValue()
        );
    }

    @Test
    void shouldPreserveOptionalActionValuePresence() {
        DeviceActionProto actionWithValue =
                DeviceActionProto.newBuilder()
                        .setSensorId("device-1")
                        .setType(ActionTypeProto.SET_VALUE)
                        .setValue(18)
                        .build();

        DeviceActionProto actionWithoutValue =
                DeviceActionProto.newBuilder()
                        .setSensorId("device-2")
                        .setType(ActionTypeProto.ACTIVATE)
                        .build();

        assertTrue(actionWithValue.hasValue());
        assertEquals(18, actionWithValue.getValue());

        assertFalse(actionWithoutValue.hasValue());
        assertEquals(0, actionWithoutValue.getValue());
    }
}

