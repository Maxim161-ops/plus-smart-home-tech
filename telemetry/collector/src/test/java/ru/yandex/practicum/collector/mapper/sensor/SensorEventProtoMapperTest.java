package ru.yandex.practicum.collector.mapper.sensor;

import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.grpc.telemetry.event.ClimateSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.LightSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.MotionSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SwitchSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.TemperatureSensorProto;
import ru.yandex.practicum.telemetry.collector.mapper.sensor.SensorEventProtoMapper;
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

class SensorEventProtoMapperTest {

    private final SensorEventProtoMapper mapper =
            new SensorEventProtoMapper();

    private final Timestamp timestamp = Timestamp.newBuilder()
            .setSeconds(1_700_000_000L)
            .setNanos(123_000_000)
            .build();

    @Test
    void shouldMapMotionSensorProtoToModel() {
        SensorEventProto proto = SensorEventProto.newBuilder()
                .setId("motion-1")
                .setHubId("hub-1")
                .setTimestamp(timestamp)
                .setMotionSensor(
                        MotionSensorProto.newBuilder()
                                .setLinkQuality(90)
                                .setMotion(true)
                                .setVoltage(220)
                                .build()
                )
                .build();

        SensorEvent result = mapper.toModel(proto);

        MotionSensorEvent event =
                assertInstanceOf(MotionSensorEvent.class, result);

        assertCommonFields(event, "motion-1", "hub-1");

        assertEquals(90, event.getLinkQuality());
        assertTrue(event.isMotion());
        assertEquals(220, event.getVoltage());
    }

    @Test
    void shouldMapTemperatureSensorProtoToModel() {
        SensorEventProto proto = SensorEventProto.newBuilder()
                .setId("temperature-1")
                .setHubId("hub-1")
                .setTimestamp(timestamp)
                .setTemperatureSensor(
                        TemperatureSensorProto.newBuilder()
                                .setTemperatureC(25)
                                .setTemperatureF(77)
                                .build()
                )
                .build();

        SensorEvent result = mapper.toModel(proto);

        TemperatureSensorEvent event =
                assertInstanceOf(TemperatureSensorEvent.class, result);

        assertCommonFields(event, "temperature-1", "hub-1");

        assertEquals(25, event.getTemperatureC());
        assertEquals(77, event.getTemperatureF());
    }

    @Test
    void shouldMapLightSensorProtoToModel() {
        SensorEventProto proto = SensorEventProto.newBuilder()
                .setId("light-1")
                .setHubId("hub-2")
                .setTimestamp(timestamp)
                .setLightSensor(
                        LightSensorProto.newBuilder()
                                .setLinkQuality(75)
                                .setLuminosity(800)
                                .build()
                )
                .build();

        SensorEvent result = mapper.toModel(proto);

        LightSensorEvent event =
                assertInstanceOf(LightSensorEvent.class, result);

        assertCommonFields(event, "light-1", "hub-2");

        assertEquals(75, event.getLinkQuality());
        assertEquals(800, event.getLuminosity());
    }

    @Test
    void shouldMapClimateSensorProtoToModel() {
        SensorEventProto proto = SensorEventProto.newBuilder()
                .setId("climate-1")
                .setHubId("hub-2")
                .setTimestamp(timestamp)
                .setClimateSensor(
                        ClimateSensorProto.newBuilder()
                                .setTemperatureC(23)
                                .setHumidity(55)
                                .setCo2Level(600)
                                .build()
                )
                .build();

        SensorEvent result = mapper.toModel(proto);

        ClimateSensorEvent event =
                assertInstanceOf(ClimateSensorEvent.class, result);

        assertCommonFields(event, "climate-1", "hub-2");

        assertEquals(23, event.getTemperatureC());
        assertEquals(55, event.getHumidity());
        assertEquals(600, event.getCo2Level());
    }

    @Test
    void shouldMapSwitchSensorProtoToModel() {
        SensorEventProto proto = SensorEventProto.newBuilder()
                .setId("switch-1")
                .setHubId("hub-3")
                .setTimestamp(timestamp)
                .setSwitchSensor(
                        SwitchSensorProto.newBuilder()
                                .setState(true)
                                .build()
                )
                .build();

        SensorEvent result = mapper.toModel(proto);

        SwitchSensorEvent event =
                assertInstanceOf(SwitchSensorEvent.class, result);

        assertCommonFields(event, "switch-1", "hub-3");

        assertTrue(event.isState());
    }

    @Test
    void shouldMapFalseSwitchState() {
        SensorEventProto proto = SensorEventProto.newBuilder()
                .setId("switch-2")
                .setHubId("hub-3")
                .setTimestamp(timestamp)
                .setSwitchSensor(
                        SwitchSensorProto.newBuilder()
                                .setState(false)
                                .build()
                )
                .build();

        SensorEvent result = mapper.toModel(proto);

        SwitchSensorEvent event =
                assertInstanceOf(SwitchSensorEvent.class, result);

        assertFalse(event.isState());
    }

    @Test
    void shouldThrowExceptionWhenPayloadIsNotSet() {
        SensorEventProto proto = SensorEventProto.newBuilder()
                .setId("sensor-1")
                .setHubId("hub-1")
                .setTimestamp(timestamp)
                .build();

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> mapper.toModel(proto)
                );

        assertEquals(
                "Payload события датчика не задан",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowExceptionWhenEventIsNull() {
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> mapper.toModel(null)
                );

        assertEquals(
                "Событие датчика для преобразования не должно быть null",
                exception.getMessage()
        );
    }

    private void assertCommonFields(
            SensorEvent event,
            String expectedId,
            String expectedHubId
    ) {
        assertEquals(expectedId, event.getId());
        assertEquals(expectedHubId, event.getHubId());

        assertEquals(
                Instant.ofEpochSecond(
                        timestamp.getSeconds(),
                        timestamp.getNanos()
                ),
                event.getTimestamp()
        );
    }
}
