package ru.yandex.practicum.telemetry.aggregator.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
public class SnapshotService {

    private final Map<String, SensorsSnapshotAvro> snapshots = new HashMap<>();

    /**
     * @param event новое событие датчика
     * @return обновлённый снапшот, если состояние изменилось;
     *         Optional.empty(), если событие нужно проигнорировать
     */
    public Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event) {
        Objects.requireNonNull(event, "Событие датчика не должно быть null");

        String hubId = event.getHubId().toString();

        SensorsSnapshotAvro snapshot = snapshots.computeIfAbsent(
                hubId,
                ignored -> createSnapshot(event)
        );

        SensorStateAvro oldState = snapshot.getSensorsState().get(event.getId());

        if (oldState != null) {
            boolean oldStateIsNewer =
                    oldState.getTimestamp().isAfter(event.getTimestamp());

            boolean dataHasNotChanged =
                    Objects.equals(oldState.getData(), event.getPayload());

            /*
             * Игнорируем событие, если:
             * 1. В снапшоте уже лежит более новое состояние датчика.
             * 2. Показания датчика не изменились.
             */
            if (oldStateIsNewer || dataHasNotChanged) {
                return Optional.empty();
            }
        }

        SensorStateAvro newState = SensorStateAvro.newBuilder()
                .setTimestamp(event.getTimestamp())
                .setData(event.getPayload())
                .build();

        snapshot.getSensorsState().put(event.getId(), newState);
        snapshot.setTimestamp(event.getTimestamp());

        return Optional.of(snapshot);
    }

    private SensorsSnapshotAvro createSnapshot(SensorEventAvro event) {
        return SensorsSnapshotAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp())
                .setSensorsState(new HashMap<>())
                .build();
    }
}
