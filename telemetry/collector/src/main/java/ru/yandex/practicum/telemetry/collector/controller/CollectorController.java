package ru.yandex.practicum.telemetry.collector.controller;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.telemetry.collector.mapper.HubEventMapper;
import ru.yandex.practicum.telemetry.collector.mapper.SensorEventMapper;
import ru.yandex.practicum.telemetry.collector.model.hub.HubEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.SensorEvent;
import ru.yandex.practicum.telemetry.collector.service.CollectorService;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc.CollectorControllerImplBase;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class CollectorController extends CollectorControllerImplBase {

    private final CollectorService collectorService;
    private final SensorEventMapper sensorEventMapper;
    private final HubEventMapper hubEventMapper;

    @Override
    public void collectSensorEvent(
            SensorEventProto request,
            StreamObserver<Empty> responseObserver
    ) {
        try {
            switch (request.getPayloadCase()) {
                case MOTION_SENSOR:
                    log.info("Получено событие датчика движения");
                    break;

                case TEMPERATURE_SENSOR:
                    log.info("Получено событие датчика температуры");
                    break;

                case LIGHT_SENSOR:
                    log.info("Получено событие датчика освещённости");
                    break;

                case CLIMATE_SENSOR:
                    log.info("Получено событие климатического датчика");
                    break;

                case SWITCH_SENSOR:
                    log.info("Получено событие переключателя");
                    break;

                case PAYLOAD_NOT_SET:
                default:
                    throw new IllegalArgumentException(
                            "Неизвестный тип события: " + request.getPayloadCase()
                    );
            }

            SensorEvent sensorEvent = sensorEventMapper.toModel(request);
            collectorService.collectSensorEvent(sensorEvent);

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();

        } catch (Exception e) {
            responseObserver.onError(
                    new StatusRuntimeException(
                            Status.INTERNAL
                                    .withDescription(e.getLocalizedMessage())
                                    .withCause(e)
                    )
            );
        }
    }

    @Override
    public void collectHubEvent(
            HubEventProto request,
            StreamObserver<Empty> responseObserver
    ) {
        try {
            log.info(
                    "Получено событие хаба: hubId={}, type={}",
                    request.getHubId(),
                    request.getPayloadCase()
            );

            HubEvent hubEvent = hubEventMapper.toModel(request);
            collectorService.collectHubEvent(hubEvent);

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error(
                    "Ошибка при обработке события хаба: hubId={}, type={}",
                    request.getHubId(),
                    request.getPayloadCase(),
                    e
            );

            responseObserver.onError(
                    new StatusRuntimeException(
                            Status.INTERNAL
                                    .withDescription(e.getLocalizedMessage())
                                    .withCause(e)
                    )
            );
        }
    }
}
