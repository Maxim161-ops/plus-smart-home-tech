package ru.yandex.practicum.telemetry.analyzer.configuration;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.telemetry.analyzer.kafka.deserializer.HubEventDeserializer;
import ru.yandex.practicum.telemetry.analyzer.kafka.deserializer.SensorsSnapshotDeserializer;

import java.util.Properties;

@Configuration
public class KafkaConfig {

    @Bean
    public KafkaConsumer<String, HubEventAvro> hubEventConsumer(
            @Value("${kafka.bootstrap-servers}")
            String bootstrapServers,

            @Value("${kafka.consumers.hub-events.group-id}")
            String groupId,

            @Value("${kafka.consumers.hub-events.auto-offset-reset}")
            String autoOffsetReset,

            @Value("${kafka.consumers.hub-events.enable-auto-commit}")
            boolean enableAutoCommit
    ) {
        Properties properties = createCommonProperties(
                bootstrapServers,
                groupId,
                autoOffsetReset,
                enableAutoCommit
        );

        properties.put(
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                HubEventDeserializer.class
        );

        return new KafkaConsumer<>(properties);
    }

    @Bean
    public KafkaConsumer<String, SensorsSnapshotAvro> snapshotConsumer(
            @Value("${kafka.bootstrap-servers}")
            String bootstrapServers,

            @Value("${kafka.consumers.snapshots.group-id}")
            String groupId,

            @Value("${kafka.consumers.snapshots.auto-offset-reset}")
            String autoOffsetReset,

            @Value("${kafka.consumers.snapshots.enable-auto-commit}")
            boolean enableAutoCommit
    ) {
        Properties properties = createCommonProperties(
                bootstrapServers,
                groupId,
                autoOffsetReset,
                enableAutoCommit
        );

        properties.put(
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                SensorsSnapshotDeserializer.class
        );

        return new KafkaConsumer<>(properties);
    }

    private Properties createCommonProperties(
            String bootstrapServers,
            String groupId,
            String autoOffsetReset,
            boolean enableAutoCommit
    ) {
        Properties properties = new Properties();

        properties.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers
        );

        properties.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                groupId
        );

        properties.put(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class
        );

        properties.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                autoOffsetReset
        );

        properties.put(
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
                enableAutoCommit
        );

        return properties;
    }
}
