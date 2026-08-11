package ru.yandex.practicum.telemetry.analyzer.processor;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.telemetry.analyzer.service.ScenarioAnalyzerService;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SnapshotProcessor {

    private static final Duration POLL_TIMEOUT = Duration.ofSeconds(1);

    private final KafkaConsumer<String, SensorsSnapshotAvro> snapshotConsumer;
    private final ScenarioAnalyzerService scenarioAnalyzerService;

    @Value("${kafka.topics.snapshots}")
    private String snapshotsTopic;

    public void start() {
        Runtime.getRuntime().addShutdownHook(
                new Thread(
                        snapshotConsumer::wakeup,
                        "snapshot-processor-shutdown-hook"
                )
        );

        try {
            snapshotConsumer.subscribe(
                    List.of(snapshotsTopic)
            );

            log.info(
                    "SnapshotProcessor подписан на топик {}",
                    snapshotsTopic
            );

            while (true) {
                ConsumerRecords<String, SensorsSnapshotAvro> records =
                        snapshotConsumer.poll(POLL_TIMEOUT);

                for (ConsumerRecord<String, SensorsSnapshotAvro> record : records) {
                    try {
                        processRecord(record);

                        commitRecord(record);

                    } catch (Exception e) {
                        log.error(
                                "Ошибка обработки снапшота: topic={}, partition={}, offset={}",
                                record.topic(),
                                record.partition(),
                                record.offset(),
                                e
                        );

                        TopicPartition partition =
                                new TopicPartition(
                                        record.topic(),
                                        record.partition()
                                );

                        snapshotConsumer.seek(
                                partition,
                                record.offset()
                        );

                        log.warn(
                                "Consumer возвращён на offset {} partition {} для повторной обработки",
                                record.offset(),
                                record.partition()
                        );

                        break;
                    }
                }
            }

        } catch (WakeupException e) {
            log.info(
                    "SnapshotProcessor получил сигнал завершения"
            );

        } catch (Exception e) {
            log.error(
                    "Фатальная ошибка Kafka Consumer снапшотов",
                    e
            );

        } finally {
            closeConsumer();
        }
    }

    private void processRecord(
            ConsumerRecord<String, SensorsSnapshotAvro> record
    ) {
        SensorsSnapshotAvro snapshot = record.value();

        if (snapshot == null) {
            log.warn(
                    "Получен пустой снапшот: topic={}, partition={}, offset={}",
                    record.topic(),
                    record.partition(),
                    record.offset()
            );

            return;
        }

        log.debug(
                "Получен снапшот: hubId={}, partition={}, offset={}",
                snapshot.getHubId(),
                record.partition(),
                record.offset()
        );

        scenarioAnalyzerService.analyze(snapshot);
    }

    private void commitRecord(
            ConsumerRecord<String, SensorsSnapshotAvro> record
    ) {
        TopicPartition partition =
                new TopicPartition(
                        record.topic(),
                        record.partition()
                );

        OffsetAndMetadata offset =
                new OffsetAndMetadata(
                        record.offset() + 1
                );

        snapshotConsumer.commitSync(
                Map.of(partition, offset)
        );

        log.debug(
                "Зафиксирован offset {} для partition {}",
                record.offset() + 1,
                record.partition()
        );
    }

    @PreDestroy
    public void stop() {
        log.info(
                "Останавливаем SnapshotProcessor"
        );

        snapshotConsumer.wakeup();
    }

    private void closeConsumer() {
        try {
            snapshotConsumer.close();

            log.info(
                    "Kafka Consumer снапшотов закрыт"
            );

        } catch (Exception e) {
            log.error(
                    "Ошибка при закрытии Kafka Consumer снапшотов",
                    e
            );
        }
    }
}
