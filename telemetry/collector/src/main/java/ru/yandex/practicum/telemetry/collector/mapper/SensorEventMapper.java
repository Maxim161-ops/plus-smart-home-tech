package ru.yandex.practicum.telemetry.collector.mapper;

import org.springframework.stereotype.Component;
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

@Component
public class SensorEventMapper {

    /**
     * Преобразует HTTP-модель события датчика в Avro-модель,
     * которая затем будет отправлена в Kafka.
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
