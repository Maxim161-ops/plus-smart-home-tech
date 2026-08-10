package ru.yandex.practicum.telemetry.analyzer.service;

import com.google.protobuf.Timestamp;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc.HubRouterControllerBlockingStub;
import ru.yandex.practicum.telemetry.analyzer.model.Action;

import java.time.Instant;

@Slf4j
@Service
public class HubRouterService {

    private final HubRouterControllerBlockingStub hubRouterClient;

    public HubRouterService(
            @GrpcClient("hub-router")
            HubRouterControllerBlockingStub hubRouterClient
    ) {
        this.hubRouterClient = hubRouterClient;
    }

    public void sendAction(
            String hubId,
            String scenarioName,
            String sensorId,
            Action action
    ) {
        DeviceActionProto actionProto =
                DeviceActionProto.newBuilder()
                        .setSensorId(sensorId)
                        .setType(
                                ActionTypeProto.valueOf(
                                        action.getType().name()
                                )
                        )
                        .build();

        if (action.getValue() != null) {
            actionProto = actionProto.toBuilder()
                    .setValue(action.getValue())
                    .build();
        }

        Instant now = Instant.now();

        Timestamp timestamp =
                Timestamp.newBuilder()
                        .setSeconds(now.getEpochSecond())
                        .setNanos(now.getNano())
                        .build();

        DeviceActionRequest request =
                DeviceActionRequest.newBuilder()
                        .setHubId(hubId)
                        .setScenarioName(scenarioName)
                        .setAction(actionProto)
                        .setTimestamp(timestamp)
                        .build();

        log.info(
                "Отправляем действие в Hub Router: hubId={}, scenario={}, sensorId={}, type={}, value={}",
                hubId,
                scenarioName,
                sensorId,
                action.getType(),
                action.getValue()
        );

        hubRouterClient.handleDeviceAction(request);
    }
}