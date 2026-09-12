package ru.yandex.practicum.commerce.interaction.client;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.commerce.interaction.dto.order.CreateNewOrderRequest;
import ru.yandex.practicum.commerce.interaction.dto.order.OrderDto;
import ru.yandex.practicum.commerce.interaction.dto.order.ProductReturnRequest;

import java.util.List;
import java.util.UUID;

@FeignClient(
        name = "order",
        path = "/api/v1/order"
)
public interface OrderClient {

    @GetMapping
    List<OrderDto> getClientOrders(
            @RequestParam("username") String username
    );

    @PutMapping
    OrderDto createNewOrder(
            @Valid
            @RequestBody CreateNewOrderRequest request
    );

    @PostMapping("/return")
    OrderDto productReturn(
            @Valid
            @RequestBody ProductReturnRequest request
    );

    @PostMapping("/payment")
    OrderDto payment(
            @RequestBody UUID orderId
    );

    @PostMapping("/payment/failed")
    OrderDto paymentFailed(
            @RequestBody UUID orderId
    );

    @PostMapping("/delivery")
    OrderDto delivery(
            @RequestBody UUID orderId
    );

    @PostMapping("/delivery/failed")
    OrderDto deliveryFailed(
            @RequestBody UUID orderId
    );

    @PostMapping("/completed")
    OrderDto complete(
            @RequestBody UUID orderId
    );

    @PostMapping("/calculate/total")
    OrderDto calculateTotalCost(
            @RequestBody UUID orderId
    );

    @PostMapping("/calculate/delivery")
    OrderDto calculateDeliveryCost(
            @RequestBody UUID orderId
    );

    @PostMapping("/assembly")
    OrderDto assembly(
            @RequestBody UUID orderId
    );

    @PostMapping("/assembly/failed")
    OrderDto assemblyFailed(
            @RequestBody UUID orderId
    );
}
