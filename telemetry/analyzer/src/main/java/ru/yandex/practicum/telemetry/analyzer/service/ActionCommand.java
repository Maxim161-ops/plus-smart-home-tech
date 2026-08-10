package ru.yandex.practicum.telemetry.analyzer.service;

import ru.yandex.practicum.telemetry.analyzer.model.Action;

public record ActionCommand(
        String hubId,
        String scenarioName,
        String sensorId,
        Action action
) {
}
