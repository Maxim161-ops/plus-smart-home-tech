package ru.yandex.practicum.commerce.delivery.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.commerce.delivery.model.DeliveryEntity;
import ru.yandex.practicum.commerce.interaction.dto.delivery.DeliveryDto;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.AddressDto;

@Component
public class DeliveryMapper {

    public DeliveryEntity toEntity(DeliveryDto deliveryDto) {
        return DeliveryEntity.builder()
                .orderId(deliveryDto.getOrderId())
                .deliveryState(deliveryDto.getDeliveryState())

                .fromCountry(deliveryDto.getFromAddress().getCountry())
                .fromCity(deliveryDto.getFromAddress().getCity())
                .fromStreet(deliveryDto.getFromAddress().getStreet())
                .fromHouse(deliveryDto.getFromAddress().getHouse())
                .fromFlat(deliveryDto.getFromAddress().getFlat())

                .toCountry(deliveryDto.getToAddress().getCountry())
                .toCity(deliveryDto.getToAddress().getCity())
                .toStreet(deliveryDto.getToAddress().getStreet())
                .toHouse(deliveryDto.getToAddress().getHouse())
                .toFlat(deliveryDto.getToAddress().getFlat())
                .build();
    }

    public DeliveryDto toDto(DeliveryEntity delivery) {

        AddressDto fromAddress = AddressDto.builder()
                .country(delivery.getFromCountry())
                .city(delivery.getFromCity())
                .street(delivery.getFromStreet())
                .house(delivery.getFromHouse())
                .flat(delivery.getFromFlat())
                .build();

        AddressDto toAddress = AddressDto.builder()
                .country(delivery.getToCountry())
                .city(delivery.getToCity())
                .street(delivery.getToStreet())
                .house(delivery.getToHouse())
                .flat(delivery.getToFlat())
                .build();

        return DeliveryDto.builder()
                .deliveryId(delivery.getDeliveryId())
                .orderId(delivery.getOrderId())
                .deliveryState(delivery.getDeliveryState())
                .fromAddress(fromAddress)
                .toAddress(toAddress)
                .build();
    }
}
