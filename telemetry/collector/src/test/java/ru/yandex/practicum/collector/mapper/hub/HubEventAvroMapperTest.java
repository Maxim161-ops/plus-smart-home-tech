package ru.yandex.practicum.collector.mapper.hub;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.telemetry.collector.mapper.hub.HubEventAvroMapper;
import ru.yandex.practicum.telemetry.collector.model.hub.DeviceAddedEvent;
import ru.yandex.practicum.telemetry.collector.model.hub.DeviceType;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class HubEventAvroMapperTest {

    private final HubEventAvroMapper mapper =
            new HubEventAvroMapper();

    @Test
    void shouldMapDeviceAddedEventToAvro() {
        Instant timestamp =
                Instant.parse("2026-07-16T09:05:00Z");

        DeviceAddedEvent event = new DeviceAddedEvent();
        event.setHubId("hub-1");
        event.setTimestamp(timestamp);
        event.setId("sensor.light.1");
        event.setDeviceType(DeviceType.LIGHT_SENSOR);

        HubEventAvro result = mapper.toAvro(event);

        assertEquals("hub-1", result.getHubId());
        assertEquals(timestamp, result.getTimestamp());

        DeviceAddedEventAvro payload =
                assertInstanceOf(
                        DeviceAddedEventAvro.class,
                        result.getPayload()
                );

        assertEquals("sensor.light.1", payload.getId());
        assertEquals(
                DeviceTypeAvro.LIGHT_SENSOR,
                payload.getType()
        );
    }
}
