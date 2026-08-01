package ru.yandex.practicum.telemetry.collector.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.telemetry.collector.mapper.hub.HubEventAvroMapper;
import ru.yandex.practicum.telemetry.collector.mapper.sensor.SensorEventAvroMapper;
import ru.yandex.practicum.telemetry.collector.model.hub.HubEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.SensorEvent;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollectorServiceImpl implements CollectorService {

    @Value("${kafka.topics.sensor-events}")
    private String sensorEventsTopic;

    @Value("${kafka.topics.hub-events}")
    private String hubEventsTopic;

    private final Producer<String, SpecificRecordBase> kafkaProducer;
    private final SensorEventAvroMapper sensorEventAvroMapper;
    private final HubEventAvroMapper hubEventAvroMapper;

    @Override
    public void collectSensorEvent(SensorEvent event) {
        SensorEventAvro avroEvent = sensorEventAvroMapper.toAvro(event);

        ProducerRecord<String, SpecificRecordBase> record =
                new ProducerRecord<>(
                        sensorEventsTopic,
                        event.getHubId(),
                        avroEvent
                );

        kafkaProducer.send(record, (metadata, exception) -> {
            if (exception != null) {
                log.error(
                        "Не удалось отправить событие датчика в Kafka: id={}, hubId={}, type={}",
                        event.getId(),
                        event.getHubId(),
                        event.getType(),
                        exception
                );
                return;
            }

            log.info(
                    "Событие датчика отправлено в Kafka: topic={}, partition={}, offset={}, id={}, type={}",
                    metadata.topic(),
                    metadata.partition(),
                    metadata.offset(),
                    event.getId(),
                    event.getType()
            );
        });
    }

    @Override
    public void collectHubEvent(HubEvent event) {
        HubEventAvro avroEvent = hubEventAvroMapper.toAvro(event);

        ProducerRecord<String, SpecificRecordBase> record =
                new ProducerRecord<>(
                        hubEventsTopic,
                        event.getHubId(),
                        avroEvent
                );

        kafkaProducer.send(record, (metadata, exception) -> {
            if (exception != null) {
                log.error(
                        "Не удалось отправить событие хаба в Kafka: hubId={}, type={}",
                        event.getHubId(),
                        event.getType(),
                        exception
                );
                return;
            }

            log.info(
                    "Событие хаба отправлено в Kafka: topic={}, partition={}, offset={}, hubId={}, type={}",
                    metadata.topic(),
                    metadata.partition(),
                    metadata.offset(),
                    event.getHubId(),
                    event.getType()
            );
        });
    }
}

