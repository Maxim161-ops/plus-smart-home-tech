package ru.yandex.practicum.collector;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.telemetry.collector.model.sensor.MotionSensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.SensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.SensorEventType;

import static org.junit.jupiter.api.Assertions.*;

class SensorEventTest {

    @Test
    void shouldDeserializeMotionSensorEvent() throws Exception {

        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

        String json = """
                {
                  "id": "sensor-1",
                  "hubId": "hub-1",
                  "timestamp": "2026-07-15T15:00:00Z",
                  "type": "MOTION_SENSOR_EVENT",
                  "linkQuality": 95,
                  "motion": true,
                  "voltage": 220
                }
                """;

        SensorEvent event = mapper.readValue(json, SensorEvent.class);

        assertInstanceOf(MotionSensorEvent.class, event);

        MotionSensorEvent motion = (MotionSensorEvent) event;

        assertEquals("sensor-1", motion.getId());
        assertEquals("hub-1", motion.getHubId());
        assertEquals(95, motion.getLinkQuality());
        assertTrue(motion.isMotion());
        assertEquals(220, motion.getVoltage());
        assertEquals(SensorEventType.MOTION_SENSOR_EVENT, motion.getType());
    }
}
