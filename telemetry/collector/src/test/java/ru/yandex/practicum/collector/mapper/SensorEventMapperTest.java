package ru.yandex.practicum.collector.mapper;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.telemetry.collector.mapper.SensorEventMapper;
import ru.yandex.practicum.telemetry.collector.model.sensor.MotionSensorEvent;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SensorEventMapperTest {

    private final SensorEventMapper mapper = new SensorEventMapper();

    @Test
    void shouldMapMotionSensorEventToAvro() {
        Instant timestamp = Instant.parse("2026-07-16T09:00:00Z");

        MotionSensorEvent event = new MotionSensorEvent();
        event.setId("sensor.motion.1");
        event.setHubId("hub-1");
        event.setTimestamp(timestamp);
        event.setLinkQuality(95);
        event.setMotion(true);
        event.setVoltage(3000);

        SensorEventAvro result = mapper.toAvro(event);

        assertEquals("sensor.motion.1", result.getId());
        assertEquals("hub-1", result.getHubId());
        assertEquals(timestamp, result.getTimestamp());

        assertInstanceOf(MotionSensorAvro.class, result.getPayload());

        MotionSensorAvro payload =
                (MotionSensorAvro) result.getPayload();

        assertEquals(95, payload.getLinkQuality());
        assertTrue(payload.getMotion());
        assertEquals(3000, payload.getVoltage());
    }
}
