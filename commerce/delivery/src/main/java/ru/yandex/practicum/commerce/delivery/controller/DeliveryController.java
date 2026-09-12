package ru.yandex.practicum.commerce.delivery.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.delivery.service.DeliveryService;
import ru.yandex.practicum.commerce.interaction.client.DeliveryClient;
import ru.yandex.practicum.commerce.interaction.dto.delivery.DeliveryDto;
import ru.yandex.practicum.commerce.interaction.dto.order.OrderDto;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/delivery")
@RequiredArgsConstructor
public class DeliveryController implements DeliveryClient {

    private final DeliveryService deliveryService;

    @Override
    public DeliveryDto planDelivery(DeliveryDto delivery) {
        return deliveryService.planDelivery(delivery);
    }

    @Override
    public void deliverySuccessful(UUID orderId) {
        deliveryService.deliverySuccessful(orderId);
    }

    @Override
    public void deliveryPicked(UUID orderId) {
        deliveryService.deliveryPicked(orderId);
    }

    @Override
    public void deliveryFailed(UUID orderId) {
        deliveryService.deliveryFailed(orderId);
    }

    @Override
    public BigDecimal deliveryCost(OrderDto order) {
        return deliveryService.deliveryCost(order);
    }
}
