package ru.yandex.practicum.telemetry.collector.model.sensor;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString(callSuper = true)
public class MotionSensorEvent extends SensorEvent {

    private int linkQuality;

    private boolean motion;

    private int voltage;

    @Override
    public SensorEventType getType() {
        return SensorEventType.MOTION_SENSOR_EVENT;
    }

}
// MotionSensorEvent наследуется от SensorEvent,
// потому что содержит общие поля id, hubId и timestamp.
// Добавляет только поля, специфичные для датчика движения.