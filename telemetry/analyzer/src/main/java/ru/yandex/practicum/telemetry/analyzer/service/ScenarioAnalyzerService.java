package ru.yandex.practicum.telemetry.analyzer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.List;


@Service
@RequiredArgsConstructor
public class ScenarioAnalyzerService {

    private final ScenarioActionFinder scenarioActionFinder;
    private final HubRouterService hubRouterService;

    public void analyze(SensorsSnapshotAvro snapshot) {
        if (snapshot == null) {
            throw new IllegalArgumentException(
                    "Снапшот не должен быть null"
            );
        }

        List<ActionCommand> commands =
                scenarioActionFinder.findActions(snapshot);

        for (ActionCommand command : commands) {
            hubRouterService.sendAction(
                    command.hubId(),
                    command.scenarioName(),
                    command.sensorId(),
                    command.action()
            );
        }
    }
}
