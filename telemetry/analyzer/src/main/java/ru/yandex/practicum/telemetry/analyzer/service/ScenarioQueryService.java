package ru.yandex.practicum.telemetry.analyzer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.telemetry.analyzer.model.Scenario;
import ru.yandex.practicum.telemetry.analyzer.repository.ScenarioRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScenarioQueryService {

    private final ScenarioRepository scenarioRepository;

    @Transactional(readOnly = true)
    public List<Scenario> findByHubId(String hubId) {
        return scenarioRepository.findByHubId(hubId);
    }
}
