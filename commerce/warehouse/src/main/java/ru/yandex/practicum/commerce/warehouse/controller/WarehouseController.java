package ru.yandex.practicum.commerce.warehouse.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.interaction.client.WarehouseClient;
import ru.yandex.practicum.commerce.interaction.dto.shoppingcart.ShoppingCartDto;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.AddressDto;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.NewProductInWarehouseRequest;
import ru.yandex.practicum.commerce.warehouse.service.WarehouseService;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.ShippedToDeliveryRequest;

import java.util.Map;
import java.util.UUID;

@Slf4j
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
    public BookedProductsDto assemblyProductsForOrder(
            AssemblyProductsForOrderRequest request
    ) {
        log.info(
                "Получен запрос на сборку заказа: orderId={}, products={}",
                request.getOrderId(),
                request.getProducts()
        );

        return warehouseService.assemblyProductsForOrder(request);
    }

    @Override
    public void shippedToDelivery(
            @Valid ShippedToDeliveryRequest request
    ) {
        warehouseService.shippedToDelivery(request);
    }

    @Override
    public void acceptReturn(
            Map<UUID, Long> products
    ) {
        warehouseService.acceptReturn(products);
    }
}
