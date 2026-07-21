package ru.yandex.practicum.collector.hub;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.telemetry.collector.model.hub.*;

import static org.junit.jupiter.api.Assertions.*;

class HubEventTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        /*
         * findAndRegisterModules() подключает поддержку java.time.Instant.
         * Без модуля JavaTimeModule Jackson не сможет обработать timestamp.
         */
        objectMapper = new ObjectMapper().findAndRegisterModules();
    }

    @Test
    void shouldDeserializeDeviceAddedEvent() throws Exception {
        String json = """
                {
                  "hubId": "hub.12345",
                  "timestamp": "2024-08-06T15:11:24.157Z",
                  "type": "DEVICE_ADDED",
                  "id": "sensor.light.3",
                  "deviceType": "LIGHT_SENSOR"
                }
                """;

        HubEvent event = objectMapper.readValue(json, HubEvent.class);

        assertInstanceOf(DeviceAddedEvent.class, event);

        DeviceAddedEvent deviceAddedEvent = (DeviceAddedEvent) event;

        assertEquals("hub.12345", deviceAddedEvent.getHubId());
        assertEquals("sensor.light.3", deviceAddedEvent.getId());
        assertEquals(DeviceType.LIGHT_SENSOR, deviceAddedEvent.getDeviceType());
        assertEquals(HubEventType.DEVICE_ADDED, deviceAddedEvent.getType());
        assertNotNull(deviceAddedEvent.getTimestamp());
    }

    @Test
    void shouldDeserializeDeviceRemovedEvent() throws Exception {
        String json = """
                {
                  "hubId": "hub.12345",
                  "timestamp": "2024-08-06T15:15:24.157Z",
                  "type": "DEVICE_REMOVED",
                  "id": "sensor.light.3"
                }
                """;

        HubEvent event = objectMapper.readValue(json, HubEvent.class);

        assertInstanceOf(DeviceRemovedEvent.class, event);

        DeviceRemovedEvent deviceRemovedEvent = (DeviceRemovedEvent) event;

        assertEquals("hub.12345", deviceRemovedEvent.getHubId());
        assertEquals("sensor.light.3", deviceRemovedEvent.getId());
        assertEquals(HubEventType.DEVICE_REMOVED, deviceRemovedEvent.getType());
        assertNotNull(deviceRemovedEvent.getTimestamp());
    }

    @Test
    void shouldDeserializeScenarioAddedEvent() throws Exception {
        String json = """
                {
                  "hubId": "hub.12345",
                  "timestamp": "2024-08-06T16:00:00.000Z",
                  "type": "SCENARIO_ADDED",
                  "name": "Evening light",
                  "conditions": [
                    {
                      "sensorId": "sensor.light.3",
                      "type": "LUMINOSITY",
                      "operation": "LOWER_THAN",
                      "value": 50
                    }
                  ],
                  "actions": [
                    {
                      "sensorId": "sensor.switch.1",
                      "type": "ACTIVATE",
                      "value": null
                    }
                  ]
                }
                """;

        HubEvent event = objectMapper.readValue(json, HubEvent.class);

        assertInstanceOf(ScenarioAddedEvent.class, event);

        ScenarioAddedEvent scenarioAddedEvent = (ScenarioAddedEvent) event;

        assertEquals("hub.12345", scenarioAddedEvent.getHubId());
        assertEquals("Evening light", scenarioAddedEvent.getName());
        assertEquals(HubEventType.SCENARIO_ADDED, scenarioAddedEvent.getType());

        assertNotNull(scenarioAddedEvent.getConditions());
        assertEquals(1, scenarioAddedEvent.getConditions().size());

        ScenarioCondition condition = scenarioAddedEvent.getConditions().getFirst();

        assertEquals("sensor.light.3", condition.getSensorId());
        assertEquals(ConditionType.LUMINOSITY, condition.getType());
        assertEquals(ConditionOperation.LOWER_THAN, condition.getOperation());
        assertEquals(50, condition.getValue());

        assertNotNull(scenarioAddedEvent.getActions());
        assertEquals(1, scenarioAddedEvent.getActions().size());

        DeviceAction action = scenarioAddedEvent.getActions().getFirst();

        assertEquals("sensor.switch.1", action.getSensorId());
        assertEquals(ActionType.ACTIVATE, action.getType());
        assertNull(action.getValue());
    }

    @Test
    void shouldDeserializeScenarioRemovedEvent() throws Exception {
        String json = """
                {
                  "hubId": "hub.12345",
                  "timestamp": "2024-08-06T17:00:00.000Z",
                  "type": "SCENARIO_REMOVED",
                  "name": "Evening light"
                }
                """;

        HubEvent event = objectMapper.readValue(json, HubEvent.class);

        assertInstanceOf(ScenarioRemovedEvent.class, event);

        ScenarioRemovedEvent scenarioRemovedEvent = (ScenarioRemovedEvent) event;

        assertEquals("hub.12345", scenarioRemovedEvent.getHubId());
        assertEquals("Evening light", scenarioRemovedEvent.getName());
        assertEquals(HubEventType.SCENARIO_REMOVED, scenarioRemovedEvent.getType());
        assertNotNull(scenarioRemovedEvent.getTimestamp());
    }

    @Test
    void shouldRejectUnknownHubEventType() {
        String json = """
                {
                  "hubId": "hub.12345",
                  "timestamp": "2024-08-06T17:00:00.000Z",
                  "type": "UNKNOWN_EVENT"
                }
                """;

        assertThrows(
                Exception.class,
                () -> objectMapper.readValue(json, HubEvent.class)
        );
    }
}
