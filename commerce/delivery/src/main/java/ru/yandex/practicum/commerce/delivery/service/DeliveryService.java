package ru.yandex.practicum.commerce.delivery.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.delivery.exception.NoDeliveryFoundException;
import ru.yandex.practicum.commerce.delivery.mapper.DeliveryMapper;
import ru.yandex.practicum.commerce.delivery.model.DeliveryEntity;
import ru.yandex.practicum.commerce.delivery.repository.DeliveryRepository;
import ru.yandex.practicum.commerce.interaction.client.OrderClient;
import ru.yandex.practicum.commerce.interaction.client.WarehouseClient;
import ru.yandex.practicum.commerce.interaction.dto.delivery.DeliveryDto;
import ru.yandex.practicum.commerce.interaction.dto.delivery.DeliveryState;
import ru.yandex.practicum.commerce.interaction.dto.order.OrderDto;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.ShippedToDeliveryRequest;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final DeliveryMapper deliveryMapper;
    private final WarehouseClient warehouseClient;
    private final OrderClient orderClient;

    @Transactional
    public DeliveryDto planDelivery(DeliveryDto deliveryDto) {

        log.info(
                "Планирование доставки для orderId={}",
                deliveryDto.getOrderId()
        );

        DeliveryEntity delivery =
                deliveryMapper.toEntity(deliveryDto);

        delivery.setDeliveryState(DeliveryState.CREATED);

        DeliveryEntity savedDelivery =
                deliveryRepository.save(delivery);

        log.info(
                "Доставка создана deliveryId={}, orderId={}, state={}",
                savedDelivery.getDeliveryId(),
                savedDelivery.getOrderId(),
                savedDelivery.getDeliveryState()
        );

        return deliveryMapper.toDto(savedDelivery);
    }

    @Transactional
    public void deliveryPicked(UUID orderId) {

        log.info("Передача заказа в доставку orderId={}", orderId);

        DeliveryEntity delivery =
                getDeliveryByOrderIdOrThrow(orderId);

        delivery.setDeliveryState(DeliveryState.IN_PROGRESS);

        deliveryRepository.save(delivery);

        log.info(
                "Доставка deliveryId={} для orderId={} переведена в state={}",
                delivery.getDeliveryId(),
                orderId,
                delivery.getDeliveryState()
        );

        ShippedToDeliveryRequest request =
                ShippedToDeliveryRequest.builder()
                        .orderId(orderId)
                        .deliveryId(delivery.getDeliveryId())
                        .build();

        warehouseClient.shippedToDelivery(request);

        log.info(
                "Склад уведомлён о передаче заказа orderId={} в deliveryId={}",
                orderId,
                delivery.getDeliveryId()
        );
    }

    @Transactional
    public void deliverySuccessful(UUID orderId) {

        log.info("Успешное завершение доставки orderId={}", orderId);

        DeliveryEntity delivery =
                getDeliveryByOrderIdOrThrow(orderId);

        delivery.setDeliveryState(DeliveryState.DELIVERED);

        deliveryRepository.save(delivery);

        log.info(
                "Доставка deliveryId={} переведена в DELIVERED, уведомляем orderId={}",
                delivery.getDeliveryId(),
                orderId
        );

        orderClient.delivery(orderId);
    }

    @Transactional
    public void deliveryFailed(UUID orderId) {

        log.warn("Ошибка доставки orderId={}", orderId);

        DeliveryEntity delivery =
                getDeliveryByOrderIdOrThrow(orderId);

        delivery.setDeliveryState(DeliveryState.FAILED);

        deliveryRepository.save(delivery);

        log.warn(
                "Доставка deliveryId={} переведена в FAILED, уведомляем orderId={}",
                delivery.getDeliveryId(),
                orderId
        );

        orderClient.deliveryFailed(orderId);
    }

    @Transactional(readOnly = true)
    public BigDecimal deliveryCost(OrderDto order) {

        log.info("Расчёт стоимости доставки orderId={}", order.getOrderId());

        DeliveryEntity delivery =
                getDeliveryByOrderIdOrThrow(order.getOrderId());

        double cost = 5.0;

        log.info("Базовая стоимость доставки orderId={} = {}", order.getOrderId(), cost);

        double warehouseCoefficient =
                getWarehouseAddressCoefficient(delivery);

        cost += cost * warehouseCoefficient;

        log.info(
                "После коэффициента склада orderId={}, coefficient={}, cost={}",
                order.getOrderId(),
                warehouseCoefficient,
                cost
        );

        if (Boolean.TRUE.equals(order.getFragile())) {
            cost += cost * 0.2;

            log.info(
                    "Добавлена надбавка за хрупкость orderId={}, cost={}",
                    order.getOrderId(),
                    cost
            );
        }

        if (order.getDeliveryWeight() != null) {
            cost += order.getDeliveryWeight() * 0.3;

            log.info(
                    "Учтён вес orderId={}, weight={}, cost={}",
                    order.getOrderId(),
                    order.getDeliveryWeight(),
                    cost
            );
        }

        if (order.getDeliveryVolume() != null) {
            cost += order.getDeliveryVolume() * 0.2;

            log.info(
                    "Учтён объём orderId={}, volume={}, cost={}",
                    order.getOrderId(),
                    order.getDeliveryVolume(),
                    cost
            );
        }

        if (delivery.getFromStreet() != null
                && delivery.getToStreet() != null
                && !delivery.getFromStreet().equals(delivery.getToStreet())) {

            cost += cost * 0.2;

            log.info(
                    "Добавлена надбавка за другой адрес orderId={}, cost={}",
                    order.getOrderId(),
                    cost
            );
        }

        BigDecimal finalCost = BigDecimal.valueOf(cost);

        log.info(
                "Итоговая стоимость доставки orderId={}, deliveryCost={}",
                order.getOrderId(),
                finalCost
        );

        return finalCost;
    }

    private double getWarehouseAddressCoefficient(DeliveryEntity delivery) {

        String warehouseStreet = delivery.getFromStreet();

        if (warehouseStreet == null) {
            return 0;
        }

        if (warehouseStreet.contains("ADDRESS_1")) {
            return 1;
        }

        if (warehouseStreet.contains("ADDRESS_2")) {
            return 2;
        }

        return 0;
    }

    private DeliveryEntity getDeliveryByOrderIdOrThrow(UUID orderId) {
        return deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NoDeliveryFoundException(orderId));
    }
}
