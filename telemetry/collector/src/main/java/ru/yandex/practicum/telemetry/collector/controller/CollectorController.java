package ru.yandex.practicum.telemetry.collector.controller;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.telemetry.collector.service.CollectorService;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc.CollectorControllerImplBase;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class CollectorController extends CollectorControllerImplBase {

    private final CollectorService collectorService;

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
}
