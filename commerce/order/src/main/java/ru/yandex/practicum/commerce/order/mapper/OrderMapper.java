package ru.yandex.practicum.commerce.order.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.commerce.interaction.dto.delivery.DeliveryDto;
import ru.yandex.practicum.commerce.interaction.dto.delivery.DeliveryState;
import ru.yandex.practicum.commerce.interaction.dto.order.CreateNewOrderRequest;
import ru.yandex.practicum.commerce.interaction.dto.order.OrderDto;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.AddressDto;
import ru.yandex.practicum.commerce.order.model.OrderAddress;
import ru.yandex.practicum.commerce.order.model.OrderEntity;

import java.util.HashMap;

@Component
public class OrderMapper {

    public OrderDto toDto(OrderEntity order) {
        return OrderDto.builder()
                .orderId(order.getOrderId())
                .shoppingCartId(order.getShoppingCartId())
                .products(new HashMap<>(order.getProducts()))
                .paymentId(order.getPaymentId())
                .deliveryId(order.getDeliveryId())
                .state(order.getState())
                .deliveryWeight(order.getDeliveryWeight())
                .deliveryVolume(order.getDeliveryVolume())
                .fragile(order.getFragile())
                .totalPrice(order.getTotalPrice())
                .deliveryPrice(order.getDeliveryPrice())
                .productPrice(order.getProductPrice())
                .build();
    }

    public OrderEntity toEntity(CreateNewOrderRequest request) {

        OrderAddress deliveryAddress = OrderAddress.builder()
                .country(request.getDeliveryAddress().getCountry())
                .city(request.getDeliveryAddress().getCity())
                .street(request.getDeliveryAddress().getStreet())
                .house(request.getDeliveryAddress().getHouse())
                .flat(request.getDeliveryAddress().getFlat())
                .build();

        return OrderEntity.builder()
                .shoppingCartId(request.getShoppingCart().getShoppingCartId())
                .products(new HashMap<>(request.getShoppingCart().getProducts()))
                .deliveryAddress(deliveryAddress)
                .build();
    }

    public DeliveryDto toDeliveryDto(OrderEntity order, AddressDto warehouseAddress) {
        return DeliveryDto.builder()
                .fromAddress(warehouseAddress)
                .toAddress(toAddressDto(order.getDeliveryAddress()))
                .orderId(order.getOrderId())
                .deliveryState(DeliveryState.CREATED)
                .build();
    }

    private AddressDto toAddressDto(OrderAddress address) {
        return AddressDto.builder()
                .country(address.getCountry())
                .city(address.getCity())
                .street(address.getStreet())
                .house(address.getHouse())
                .flat(address.getFlat())
                .build();
    }
}
