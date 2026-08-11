package ru.yandex.practicum.telemetry.analyzer.processor;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.telemetry.analyzer.service.HubEventService;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class HubEventProcessor implements Runnable {

    private static final Duration POLL_TIMEOUT = Duration.ofSeconds(1);

    private final KafkaConsumer<String, HubEventAvro> hubEventConsumer;
    private final HubEventService hubEventService;

    @Value("${kafka.topics.hub-events}")
    private String hubEventsTopic;

    @Override
    public void run() {
        Runtime.getRuntime().addShutdownHook(
                new Thread(
                        hubEventConsumer::wakeup,
                        "hub-event-processor-shutdown-hook"
                )
        );

        try {
            hubEventConsumer.subscribe(
                    List.of(hubEventsTopic)
            );

            log.info(
                    "HubEventProcessor подписан на топик {}",
                    hubEventsTopic
            );

            while (true) {
                ConsumerRecords<String, HubEventAvro> records =
                        hubEventConsumer.poll(POLL_TIMEOUT);

                boolean batchProcessedSuccessfully = true;

                for (ConsumerRecord<String, HubEventAvro> record : records) {
                    try {
                        processRecord(record);

                    } catch (Exception e) {
                        batchProcessedSuccessfully = false;

                        log.error(
                                "Ошибка обработки события хаба: topic={}, partition={}, offset={}",
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

                        hubEventConsumer.seek(
                                partition,
                                record.offset()
                        );

                        log.warn(
                                "Consumer событий хаба возвращён на offset {} partition {}",
                                record.offset(),
                                record.partition()
                        );

                        break;
                    }
                }

                if (!records.isEmpty() && batchProcessedSuccessfully) {
                    hubEventConsumer.commitSync();

                    log.debug(
                            "Успешно обработана и зафиксирована пачка из {} событий хаба",
                            records.count()
                    );
                }
            }

        } catch (WakeupException e) {
            log.info(
                    "HubEventProcessor получил сигнал завершения"
            );

        } catch (Exception e) {
            log.error(
                    "Фатальная ошибка Kafka Consumer событий хаба",
                    e
            );

        } finally {
            closeConsumer();
        }
    }

    private void processRecord(
            ConsumerRecord<String, HubEventAvro> record
    ) {
        HubEventAvro event = record.value();

        if (event == null) {
            log.warn(
                    "Получено пустое событие хаба: topic={}, partition={}, offset={}",
                    record.topic(),
                    record.partition(),
                    record.offset()
            );

            return;
        }

        log.debug(
                "Получено событие хаба: hubId={}, type={}, partition={}, offset={}",
                event.getHubId(),
                event.getPayload() != null
                        ? event.getPayload().getClass().getSimpleName()
                        : "null",
                record.partition(),
                record.offset()
        );

        hubEventService.handle(event);
    }

    @PreDestroy
    public void stop() {
        log.info(
                "Останавливаем HubEventProcessor"
        );

        hubEventConsumer.wakeup();
    }

    private void closeConsumer() {
        try {
            hubEventConsumer.close();

            log.info(
                    "Kafka Consumer событий хаба закрыт"
            );

        } catch (Exception e) {
            log.error(
                    "Ошибка при закрытии Kafka Consumer событий хаба",
                    e
            );
        }
    }
}
