package ru.yandex.practicum.collector.mapper.sensor;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro;
import ru.yandex.practicum.telemetry.collector.mapper.sensor.SensorEventAvroMapper;
import ru.yandex.practicum.telemetry.collector.model.sensor.ClimateSensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.LightSensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.MotionSensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.SensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.SwitchSensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.TemperatureSensorEvent;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SensorEventAvroMapperTest {

    private final SensorEventAvroMapper mapper =
            new SensorEventAvroMapper();

    private final Instant timestamp =
            Instant.parse("2026-07-31T15:30:45.123Z");

    @Test
    void shouldMapMotionSensorEventToAvro() {
        MotionSensorEvent event = new MotionSensorEvent();
        event.setId("motion-1");
        event.setHubId("hub-1");
        event.setTimestamp(timestamp);
        event.setLinkQuality(90);
        event.setMotion(true);
        event.setVoltage(220);

        SensorEventAvro result = mapper.toAvro(event);

        assertCommonFields(result, "motion-1", "hub-1");

        MotionSensorAvro payload =
                assertInstanceOf(
                        MotionSensorAvro.class,
                        result.getPayload()
                );

        assertEquals(90, payload.getLinkQuality());
        assertTrue(payload.getMotion());
        assertEquals(220, payload.getVoltage());
    }

    @Test
    void shouldMapTemperatureSensorEventToAvro() {
        TemperatureSensorEvent event = new TemperatureSensorEvent();
        event.setId("temperature-1");
        event.setHubId("hub-1");
        event.setTimestamp(timestamp);
        event.setTemperatureC(25);
        event.setTemperatureF(77);

        SensorEventAvro result = mapper.toAvro(event);

        assertCommonFields(
                result,
                "temperature-1",
                "hub-1"
        );

        TemperatureSensorAvro payload =
                assertInstanceOf(
                        TemperatureSensorAvro.class,
                        result.getPayload()
                );

        assertEquals("temperature-1", payload.getId());
        assertEquals("hub-1", payload.getHubId());
        assertEquals(timestamp, payload.getTimestamp());
        assertEquals(25, payload.getTemperatureC());
        assertEquals(77, payload.getTemperatureF());
    }

    @Test
    void shouldMapLightSensorEventToAvro() {
        LightSensorEvent event = new LightSensorEvent();
        event.setId("light-1");
        event.setHubId("hub-2");
        event.setTimestamp(timestamp);
        event.setLinkQuality(75);
        event.setLuminosity(800);

        SensorEventAvro result = mapper.toAvro(event);

        assertCommonFields(result, "light-1", "hub-2");

        LightSensorAvro payload =
                assertInstanceOf(
                        LightSensorAvro.class,
                        result.getPayload()
                );

        assertEquals(75, payload.getLinkQuality());
        assertEquals(800, payload.getLuminosity());
    }

    @Test
    void shouldMapClimateSensorEventToAvro() {
        ClimateSensorEvent event = new ClimateSensorEvent();
        event.setId("climate-1");
        event.setHubId("hub-2");
        event.setTimestamp(timestamp);
        event.setTemperatureC(23);
        event.setHumidity(55);
        event.setCo2Level(600);

        SensorEventAvro result = mapper.toAvro(event);

        assertCommonFields(result, "climate-1", "hub-2");

        ClimateSensorAvro payload =
                assertInstanceOf(
                        ClimateSensorAvro.class,
                        result.getPayload()
                );

        assertEquals(23, payload.getTemperatureC());
        assertEquals(55, payload.getHumidity());
        assertEquals(600, payload.getCo2Level());
    }

    @Test
    void shouldMapSwitchSensorEventToAvro() {
        SwitchSensorEvent event = new SwitchSensorEvent();
        event.setId("switch-1");
        event.setHubId("hub-3");
        event.setTimestamp(timestamp);
        event.setState(true);

        SensorEventAvro result = mapper.toAvro(event);

        assertCommonFields(result, "switch-1", "hub-3");

        SwitchSensorAvro payload =
                assertInstanceOf(
                        SwitchSensorAvro.class,
                        result.getPayload()
                );

        assertTrue(payload.getState());
    }

    @Test
    void shouldMapFalseSwitchStateToAvro() {
        SwitchSensorEvent event = new SwitchSensorEvent();
        event.setId("switch-2");
        event.setHubId("hub-3");
        event.setTimestamp(timestamp);
        event.setState(false);

        SensorEventAvro result = mapper.toAvro(event);

        SwitchSensorAvro payload =
                assertInstanceOf(
                        SwitchSensorAvro.class,
                        result.getPayload()
                );

        assertFalse(payload.getState());
    }

    @Test
    void shouldThrowExceptionWhenEventIsNull() {
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> mapper.toAvro(null)
                );

        assertEquals(
                "Событие датчика для преобразования не должно быть null",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowExceptionForUnknownSensorEventType() {
        SensorEvent unknownEvent = new SensorEvent() {
            @Override
            public ru.yandex.practicum.telemetry.collector.model.sensor.SensorEventType getType() {
                return null;
            }
        };

        unknownEvent.setId("unknown-1");
        unknownEvent.setHubId("hub-unknown");
        unknownEvent.setTimestamp(timestamp);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> mapper.toAvro(unknownEvent)
                );

        assertTrue(
                exception.getMessage()
                        .startsWith("Неизвестный тип события датчика:")
        );
    }

    private void assertCommonFields(
            SensorEventAvro result,
            String expectedId,
            String expectedHubId
    ) {
        assertEquals(expectedId, result.getId());
        assertEquals(expectedHubId, result.getHubId());
        assertEquals(timestamp, result.getTimestamp());
    }
}