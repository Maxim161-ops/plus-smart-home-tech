package ru.yandex.practicum.commerce.interaction.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.commerce.interaction.dto.delivery.DeliveryDto;
import ru.yandex.practicum.commerce.interaction.dto.order.OrderDto;

import java.math.BigDecimal;
import java.util.UUID;

@FeignClient(
        name = "delivery",
        path = "/api/v1/delivery"
)
public interface DeliveryClient {

    @PutMapping
    DeliveryDto planDelivery(
            @RequestBody DeliveryDto delivery
    );

    @PostMapping("/successful")
    void deliverySuccessful(
            @RequestBody UUID orderId
    );

    @PostMapping("/picked")
    void deliveryPicked(
            @RequestBody UUID orderId
    );

    @PostMapping("/failed")
    void deliveryFailed(
            @RequestBody UUID orderId
    );

    @PostMapping("/cost")
    BigDecimal deliveryCost(
            @RequestBody OrderDto order
    );
}
