package ru.yandex.practicum.telemetry.aggregator.starter;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.telemetry.aggregator.service.SnapshotService;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter {

    private static final Duration POLL_TIMEOUT = Duration.ofSeconds(1);

    private final KafkaConsumer<String, SensorEventAvro> consumer;
    private final KafkaProducer<String, SensorsSnapshotAvro> producer;
    private final SnapshotService snapshotService;

    @Value("${kafka.topics.sensors}")
    private String sensorEventsTopic;

    @Value("${kafka.topics.snapshots}")
    private String snapshotsTopic;

    public void start() {
        Runtime.getRuntime().addShutdownHook(
                new Thread(
                        consumer::wakeup,
                        "aggregator-shutdown-hook"
                )
        );

        try {
            consumer.subscribe(List.of(sensorEventsTopic));

            log.info(
                    "Aggregator подписан на топик {}",
                    sensorEventsTopic
            );

            while (true) {
                ConsumerRecords<String, SensorEventAvro> records =
                        consumer.poll(POLL_TIMEOUT);

                for (ConsumerRecord<String, SensorEventAvro> record : records) {
                    processRecord(record);
                }

                /*
                 * Сначала гарантируем отправку сформированных снапшотов,
                 * затем фиксируем смещения обработанных событий.
                 */
                if (!records.isEmpty()) {
                    producer.flush();
                    consumer.commitSync();
                }
            }

        } catch (WakeupException ignored) {
            log.info("Получен сигнал завершения работы Aggregator");

        } catch (Exception e) {
            log.error(
                    "Ошибка во время обработки событий от датчиков",
                    e
            );

        } finally {
            closeResources();
        }
    }

    private void processRecord(
            ConsumerRecord<String, SensorEventAvro> record
    ) {
        SensorEventAvro event = record.value();

        if (event == null) {
            log.warn(
                    "Получено пустое событие: topic={}, partition={}, offset={}",
                    record.topic(),
                    record.partition(),
                    record.offset()
            );
            return;
        }

        log.debug(
                "Получено событие датчика: hubId={}, sensorId={}, " +
                        "partition={}, offset={}",
                event.getHubId(),
                event.getId(),
                record.partition(),
                record.offset()
        );

        snapshotService.updateState(event)
                .ifPresent(snapshot -> sendSnapshot(event, snapshot));
    }

    private void sendSnapshot(
            SensorEventAvro event,
            SensorsSnapshotAvro snapshot
    ) {
        ProducerRecord<String, SensorsSnapshotAvro> producerRecord =
                new ProducerRecord<>(
                        snapshotsTopic,
                        event.getHubId().toString(),
                        snapshot
                );

        producer.send(
                producerRecord,
                (metadata, exception) -> {
                    if (exception != null) {
                        log.error(
                                "Не удалось отправить снапшот хаба {}",
                                snapshot.getHubId(),
                                exception
                        );
                        return;
                    }

                    log.debug(
                            "Снапшот хаба {} отправлен: topic={}, " +
                                    "partition={}, offset={}",
                            snapshot.getHubId(),
                            metadata.topic(),
                            metadata.partition(),
                            metadata.offset()
                    );
                }
        );
    }

    @PreDestroy
    public void stop() {
        log.info("Останавливаем Aggregator");
        consumer.wakeup();
    }

    private void closeResources() {
        try {
            log.info("Сбрасываем сообщения из буфера продюсера");
            producer.flush();

            log.info("Фиксируем смещения обработанных сообщений");
            consumer.commitSync();

        } catch (Exception e) {
            log.error(
                    "Ошибка при завершении работы Kafka-клиентов",
                    e
            );

        } finally {
            try {
                log.info("Закрываем консьюмер");
                consumer.close();
            } finally {
                log.info("Закрываем продюсер");
                producer.close();
            }
        }
    }
}
