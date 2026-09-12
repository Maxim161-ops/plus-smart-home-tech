package ru.yandex.practicum.commerce.interaction.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.commerce.interaction.dto.order.OrderDto;
import ru.yandex.practicum.commerce.interaction.dto.payment.PaymentDto;

import java.math.BigDecimal;
import java.util.UUID;

@FeignClient(
        name = "payment",
        path = "/api/v1/payment"
)
public interface PaymentClient {

    @PostMapping
    PaymentDto payment(
            @RequestBody OrderDto order
    );

    @PostMapping("/totalCost")
    BigDecimal getTotalCost(
            @RequestBody OrderDto order
    );

    @PostMapping("/productCost")
    BigDecimal productCost(
            @RequestBody OrderDto order
    );

    @PostMapping("/refund")
    void paymentSuccess(
            @RequestBody UUID paymentId
    );

    @PostMapping("/failed")
    void paymentFailed(
            @RequestBody UUID paymentId
    );
}
