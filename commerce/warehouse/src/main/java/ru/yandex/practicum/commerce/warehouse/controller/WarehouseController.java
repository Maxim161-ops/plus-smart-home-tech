package ru.yandex.practicum.commerce.warehouse.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.interaction.client.WarehouseClient;
import ru.yandex.practicum.commerce.interaction.dto.shoppingcart.ShoppingCartDto;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.*;
import ru.yandex.practicum.commerce.warehouse.service.WarehouseService;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/v1/warehouse")
@RequiredArgsConstructor
public class WarehouseController implements WarehouseClient {

    private final WarehouseService warehouseService;

    @Override
    public void newProductInWarehouse(
            @Valid NewProductInWarehouseRequest request
    ) {
        warehouseService.newProductInWarehouse(request);
    }

    @Override
    public BookedProductsDto checkProductQuantityEnoughForShoppingCart(
            @Valid ShoppingCartDto shoppingCart
    ) {
        return warehouseService
                .checkProductQuantityEnoughForShoppingCart(shoppingCart);
    }

    @Override
    public void addProductToWarehouse(
            @Valid AddProductToWarehouseRequest request
    ) {
        warehouseService.addProductToWarehouse(request);
    }

    @Override
    public AddressDto getWarehouseAddress() {
        return warehouseService.getWarehouseAddress();
    }
    @Override
    public void reserveProduct(
            @Valid @RequestBody ProductQuantityRequest request
    ) {
        warehouseService.reserveProduct(request);
    }

    @Override
    public void releaseProduct(
            @Valid @RequestBody ProductQuantityRequest request
    ) {
        warehouseService.releaseProduct(request);
    }
}
