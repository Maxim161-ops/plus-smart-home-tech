package ru.yandex.practicum.collector.mapper;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.telemetry.collector.mapper.HubEventMapper;
import ru.yandex.practicum.telemetry.collector.model.hub.DeviceAddedEvent;
import ru.yandex.practicum.telemetry.collector.model.hub.DeviceType;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

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
}
