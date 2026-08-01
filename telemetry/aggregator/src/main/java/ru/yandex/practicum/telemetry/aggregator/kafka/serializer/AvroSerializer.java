package ru.yandex.practicum.telemetry.aggregator.kafka.serializer;

import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.io.ByteArrayOutputStream;

public class AvroSerializer implements Serializer<SpecificRecordBase> {

    private final EncoderFactory encoderFactory = EncoderFactory.get();

    @Override
    public byte[] serialize(String topic, SpecificRecordBase data) {
        if (data == null) {
            return null;
        }

        try (ByteArrayOutputStream outputStream =
                     new ByteArrayOutputStream()) {

            BinaryEncoder encoder =
                    encoderFactory.binaryEncoder(outputStream, null);

            SpecificDatumWriter<SpecificRecordBase> writer =
                    new SpecificDatumWriter<>(data.getSchema());

            writer.write(data, encoder);
            encoder.flush();

            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new SerializationException(
                    "Не удалось сериализовать Avro-сообщение для топика "
                            + topic,
                    e
            );
        }
    }
}
