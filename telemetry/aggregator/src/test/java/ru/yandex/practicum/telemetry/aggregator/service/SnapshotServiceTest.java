package ru.yandex.practicum.telemetry.aggregator.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SnapshotServiceTest {

    private SnapshotService snapshotService;

    @BeforeEach
    void setUp() {
        snapshotService = new SnapshotService();
    }

    @Test
    void updateStateShouldCreateSnapshotForFirstEvent() {
        SensorEventAvro event = createMotionEvent(
                "sensor-1",
                "hub-1",
                Instant.parse("2026-08-01T10:00:00Z"),
                true
        );

        Optional<SensorsSnapshotAvro> result =
                snapshotService.updateState(event);

        assertTrue(result.isPresent());

        SensorsSnapshotAvro snapshot = result.get();

        assertEquals("hub-1", snapshot.getHubId().toString());
        assertEquals(event.getTimestamp(), snapshot.getTimestamp());
        assertEquals(1, snapshot.getSensorsState().size());
        assertTrue(snapshot.getSensorsState().containsKey("sensor-1"));

        SensorStateAvro state =
                snapshot.getSensorsState().get("sensor-1");

        assertEquals(event.getTimestamp(), state.getTimestamp());
        assertEquals(event.getPayload(), state.getData());
    }

    @Test
    void updateStateShouldAddSecondSensorToExistingSnapshot() {
        SensorEventAvro firstEvent = createMotionEvent(
                "sensor-1",
                "hub-1",
                Instant.parse("2026-08-01T10:00:00Z"),
                true
        );

        SensorEventAvro secondEvent = createMotionEvent(
                "sensor-2",
                "hub-1",
                Instant.parse("2026-08-01T10:01:00Z"),
                false
        );

        snapshotService.updateState(firstEvent);

        Optional<SensorsSnapshotAvro> result =
                snapshotService.updateState(secondEvent);

        assertTrue(result.isPresent());

        SensorsSnapshotAvro snapshot = result.get();

        assertEquals(2, snapshot.getSensorsState().size());
        assertTrue(snapshot.getSensorsState().containsKey("sensor-1"));
        assertTrue(snapshot.getSensorsState().containsKey("sensor-2"));
        assertEquals(secondEvent.getTimestamp(), snapshot.getTimestamp());
    }

    @Test
    void updateStateShouldIgnoreDuplicateData() {
        SensorEventAvro firstEvent = createMotionEvent(
                "sensor-1",
                "hub-1",
                Instant.parse("2026-08-01T10:00:00Z"),
                true
        );

        SensorEventAvro duplicateEvent = createMotionEvent(
                "sensor-1",
                "hub-1",
                Instant.parse("2026-08-01T10:01:00Z"),
                true
        );

        snapshotService.updateState(firstEvent);

        Optional<SensorsSnapshotAvro> result =
                snapshotService.updateState(duplicateEvent);

        assertTrue(result.isEmpty());
    }

    @Test
    void updateStateShouldIgnoreOlderEvent() {
        SensorEventAvro currentEvent = createMotionEvent(
                "sensor-1",
                "hub-1",
                Instant.parse("2026-08-01T10:10:00Z"),
                true
        );

        SensorEventAvro olderEvent = createMotionEvent(
                "sensor-1",
                "hub-1",
                Instant.parse("2026-08-01T10:05:00Z"),
                false
        );

        snapshotService.updateState(currentEvent);

        Optional<SensorsSnapshotAvro> result =
                snapshotService.updateState(olderEvent);

        assertTrue(result.isEmpty());
    }

    @Test
    void updateStateShouldReplaceStateWhenNewDataArrives() {
        SensorEventAvro firstEvent = createMotionEvent(
                "sensor-1",
                "hub-1",
                Instant.parse("2026-08-01T10:00:00Z"),
                false
        );

        SensorEventAvro updatedEvent = createMotionEvent(
                "sensor-1",
                "hub-1",
                Instant.parse("2026-08-01T10:05:00Z"),
                true
        );

        snapshotService.updateState(firstEvent);

        Optional<SensorsSnapshotAvro> result =
                snapshotService.updateState(updatedEvent);

        assertTrue(result.isPresent());

        SensorStateAvro state = result.get()
                .getSensorsState()
                .get("sensor-1");

        assertEquals(updatedEvent.getTimestamp(), state.getTimestamp());
        assertEquals(updatedEvent.getPayload(), state.getData());
        assertEquals(1, result.get().getSensorsState().size());
    }

    @Test
    void updateStateShouldCreateSeparateSnapshotsForDifferentHubs() {
        SensorEventAvro firstHubEvent = createMotionEvent(
                "sensor-1",
                "hub-1",
                Instant.parse("2026-08-01T10:00:00Z"),
                true
        );

        SensorEventAvro secondHubEvent = createMotionEvent(
                "sensor-1",
                "hub-2",
                Instant.parse("2026-08-01T10:01:00Z"),
                false
        );

        Optional<SensorsSnapshotAvro> firstResult =
                snapshotService.updateState(firstHubEvent);

        Optional<SensorsSnapshotAvro> secondResult =
                snapshotService.updateState(secondHubEvent);

        assertTrue(firstResult.isPresent());
        assertTrue(secondResult.isPresent());

        assertEquals("hub-1", firstResult.get().getHubId().toString());
        assertEquals("hub-2", secondResult.get().getHubId().toString());
        assertNotSame(firstResult.get(), secondResult.get());
    }

    private SensorEventAvro createMotionEvent(
            String sensorId,
            String hubId,
            Instant timestamp,
            boolean motion
    ) {
        MotionSensorAvro payload = MotionSensorAvro.newBuilder()
                .setLinkQuality(90)
                .setMotion(motion)
                .setVoltage(220)
                .build();

        return SensorEventAvro.newBuilder()
                .setId(sensorId)
                .setHubId(hubId)
                .setTimestamp(timestamp)
                .setPayload(payload)
                .build();
    }
}
