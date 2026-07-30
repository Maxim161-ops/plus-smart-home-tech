package ru.yandex.practicum.telemetry.collector.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro;
import ru.yandex.practicum.telemetry.collector.model.sensor.ClimateSensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.LightSensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.MotionSensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.SensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.SwitchSensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.TemperatureSensorEvent;
import java.time.Instant;

@Component
public class SensorEventMapper {

    /**
     * Преобразует внутреннюю модель события датчика
     * в Avro-модель для отправки в Kafka.
     */
    public SensorEventAvro toAvro(SensorEvent event) {
        if (event == null) {
            throw new IllegalArgumentException(
                    "Событие датчика для преобразования не должно быть null"
            );
        }

        return SensorEventAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp())
                .setPayload(mapPayload(event))
                .build();
    }

    /**
     * Выбирает Avro-модель payload в зависимости от конкретного
     * класса входящего события.
     */
    private Object mapPayload(SensorEvent event) {
        return switch (event) {
            case MotionSensorEvent motionEvent ->
                    mapMotionSensorEvent(motionEvent);

            case LightSensorEvent lightEvent ->
                    mapLightSensorEvent(lightEvent);

            case ClimateSensorEvent climateEvent ->
                    mapClimateSensorEvent(climateEvent);

            case SwitchSensorEvent switchEvent ->
                    mapSwitchSensorEvent(switchEvent);

            case TemperatureSensorEvent temperatureEvent ->
                    mapTemperatureSensorEvent(temperatureEvent);

            default -> throw new IllegalArgumentException(
                    "Неизвестный тип события датчика: "
                            + event.getClass().getName()
            );
        };
    }

    public SensorEvent toModel(SensorEventProto event) {
        if (event == null) {
            throw new IllegalArgumentException(
                    "Событие датчика для преобразования не должно быть null"
            );
        }

        return switch (event.getPayloadCase()) {
            case MOTION_SENSOR -> mapMotionSensor(event);
            case TEMPERATURE_SENSOR -> mapTemperatureSensor(event);
            case LIGHT_SENSOR -> mapLightSensor(event);
            case CLIMATE_SENSOR -> mapClimateSensor(event);
            case SWITCH_SENSOR -> mapSwitchSensor(event);

            case PAYLOAD_NOT_SET -> throw new IllegalArgumentException(
                    "Payload события датчика не задан"
            );
        };
    }

    private TemperatureSensorEvent mapTemperatureSensor(
            SensorEventProto event
    ) {
        TemperatureSensorEvent result = new TemperatureSensorEvent();

        result.setId(event.getId());
        result.setHubId(event.getHubId());
        result.setTimestamp(
                Instant.ofEpochSecond(
                        event.getTimestamp().getSeconds(),
                        event.getTimestamp().getNanos()
                )
        );

        result.setTemperatureC(
                event.getTemperatureSensor().getTemperatureC()
        );
        result.setTemperatureF(
                event.getTemperatureSensor().getTemperatureF()
        );

        return result;
    }

    private LightSensorEvent mapLightSensor(SensorEventProto event) {
        LightSensorEvent result = new LightSensorEvent();

        result.setId(event.getId());
        result.setHubId(event.getHubId());
        result.setTimestamp(
                Instant.ofEpochSecond(
                        event.getTimestamp().getSeconds(),
                        event.getTimestamp().getNanos()
                )
        );

        result.setLinkQuality(
                event.getLightSensor().getLinkQuality()
        );
        result.setLuminosity(
                event.getLightSensor().getLuminosity()
        );

        return result;
    }

    private ClimateSensorEvent mapClimateSensor(SensorEventProto event) {
        ClimateSensorEvent result = new ClimateSensorEvent();

        result.setId(event.getId());
        result.setHubId(event.getHubId());
        result.setTimestamp(
                Instant.ofEpochSecond(
                        event.getTimestamp().getSeconds(),
                        event.getTimestamp().getNanos()
                )
        );

        result.setTemperatureC(
                event.getClimateSensor().getTemperatureC()
        );
        result.setHumidity(
                event.getClimateSensor().getHumidity()
        );
        result.setCo2Level(
                event.getClimateSensor().getCo2Level()
        );

        return result;
    }

    private SwitchSensorEvent mapSwitchSensor(SensorEventProto event) {
        SwitchSensorEvent result = new SwitchSensorEvent();

        result.setId(event.getId());
        result.setHubId(event.getHubId());
        result.setTimestamp(
                Instant.ofEpochSecond(
                        event.getTimestamp().getSeconds(),
                        event.getTimestamp().getNanos()
                )
        );

        result.setState(
                event.getSwitchSensor().getState()
        );

        return result;
    }

    private MotionSensorEvent mapMotionSensor(SensorEventProto event) {
        MotionSensorEvent result = new MotionSensorEvent();

        result.setId(event.getId());
        result.setHubId(event.getHubId());

        result.setTimestamp(
                Instant.ofEpochSecond(
                        event.getTimestamp().getSeconds(),
                        event.getTimestamp().getNanos()
                )
        );

        result.setLinkQuality(event.getMotionSensor().getLinkQuality());
        result.setMotion(event.getMotionSensor().getMotion());
        result.setVoltage(event.getMotionSensor().getVoltage());

        return result;
    }

    private MotionSensorAvro mapMotionSensorEvent(MotionSensorEvent event) {
        return MotionSensorAvro.newBuilder()
                .setLinkQuality(event.getLinkQuality())
                .setMotion(event.isMotion())
                .setVoltage(event.getVoltage())
                .build();
    }

    private LightSensorAvro mapLightSensorEvent(LightSensorEvent event) {
        return LightSensorAvro.newBuilder()
                .setLinkQuality(event.getLinkQuality())
                .setLuminosity(event.getLuminosity())
                .build();
    }

    private ClimateSensorAvro mapClimateSensorEvent(ClimateSensorEvent event) {
        return ClimateSensorAvro.newBuilder()
                .setTemperatureC(event.getTemperatureC())
                .setHumidity(event.getHumidity())
                .setCo2Level(event.getCo2Level())
                .build();
    }

    private SwitchSensorAvro mapSwitchSensorEvent(SwitchSensorEvent event) {
        return SwitchSensorAvro.newBuilder()
                .setState(event.isState())
                .build();
    }

    private TemperatureSensorAvro mapTemperatureSensorEvent(
            TemperatureSensorEvent event
    ) {
        return TemperatureSensorAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp())
                .setTemperatureC(event.getTemperatureC())
                .setTemperatureF(event.getTemperatureF())
                .build();
    }
}
