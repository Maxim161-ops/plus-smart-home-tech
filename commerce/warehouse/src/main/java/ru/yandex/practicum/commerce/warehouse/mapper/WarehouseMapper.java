package ru.yandex.practicum.commerce.warehouse.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.NewProductInWarehouseRequest;
import ru.yandex.practicum.commerce.warehouse.model.WarehouseProduct;

@Component
public class WarehouseMapper {

    public WarehouseProduct toEntity(NewProductInWarehouseRequest request) {
        return WarehouseProduct.builder()
                .productId(request.getProductId())
                .quantity(0L)
                .width(request.getDimension().getWidth())
                .height(request.getDimension().getHeight())
                .depth(request.getDimension().getDepth())
                .weight(request.getWeight())
                .fragile(Boolean.TRUE.equals(request.getFragile()))
                .build();
    }
}
