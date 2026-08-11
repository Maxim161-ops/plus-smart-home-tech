package ru.yandex.practicum.telemetry.collector.mapper.sensor;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.telemetry.collector.model.sensor.ClimateSensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.LightSensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.MotionSensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.SensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.SwitchSensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.TemperatureSensorEvent;

import java.time.Instant;

@Component
public class SensorEventProtoMapper {

    /**
     * Преобразует полученное по gRPC событие датчика
     * во внутреннюю модель приложения.
     */
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

    private MotionSensorEvent mapMotionSensor(SensorEventProto event) {
        MotionSensorEvent result = new MotionSensorEvent();

        setCommonFields(result, event);

        result.setLinkQuality(
                event.getMotionSensor().getLinkQuality()
        );
        result.setMotion(
                event.getMotionSensor().getMotion()
        );
        result.setVoltage(
                event.getMotionSensor().getVoltage()
        );

        return result;
    }

    private TemperatureSensorEvent mapTemperatureSensor(
            SensorEventProto event
    ) {
        TemperatureSensorEvent result = new TemperatureSensorEvent();

        setCommonFields(result, event);

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

        setCommonFields(result, event);

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

        setCommonFields(result, event);

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

        setCommonFields(result, event);

        result.setState(
                event.getSwitchSensor().getState()
        );

        return result;
    }

    /**
     * Заполняет общие для всех датчиков поля:
     * id, hubId и timestamp.
     */
    private void setCommonFields(
            SensorEvent result,
            SensorEventProto source
    ) {
        result.setId(source.getId());
        result.setHubId(source.getHubId());

        result.setTimestamp(
                Instant.ofEpochSecond(
                        source.getTimestamp().getSeconds(),
                        source.getTimestamp().getNanos()
                )
        );
    }
}
