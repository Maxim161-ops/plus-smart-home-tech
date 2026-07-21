package ru.yandex.practicum.telemetry.collector.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.telemetry.collector.model.hub.HubEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.SensorEvent;
import ru.yandex.practicum.telemetry.collector.service.CollectorService;

@Slf4j
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class CollectorController {

    private final CollectorService collectorService;

    @PostMapping("/sensors")
    public ResponseEntity<Void> collectSensorEvent(
            @Valid @RequestBody SensorEvent event
    ) {
        log.info(
                "Получено событие датчика: id={}, hubId={}, type={}, timestamp={}",
                event.getId(),
                event.getHubId(),
                event.getType(),
                event.getTimestamp()
        );

        collectorService.collectSensorEvent(event);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/hubs")
    public ResponseEntity<Void> collectHubEvent(
            @Valid @RequestBody HubEvent event
    ) {
        log.info(
                "Получено событие хаба: hubId={}, type={}, timestamp={}",
                event.getHubId(),
                event.getType(),
                event.getTimestamp()
        );

        collectorService.collectHubEvent(event);

        return ResponseEntity.ok().build();
    }
}
