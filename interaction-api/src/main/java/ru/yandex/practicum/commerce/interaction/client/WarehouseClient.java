package ru.yandex.practicum.commerce.interaction.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.commerce.interaction.dto.shoppingcart.ShoppingCartDto;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.AddressDto;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.NewProductInWarehouseRequest;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.ShippedToDeliveryRequest;

import java.util.Map;
import java.util.UUID;

@FeignClient(
        name = "warehouse",
        path = "/api/v1/warehouse"
)
public interface WarehouseClient {

    @PutMapping
    void newProductInWarehouse(
            @RequestBody NewProductInWarehouseRequest request
    );

    @PostMapping("/check")
    BookedProductsDto checkProductQuantityEnoughForShoppingCart(
            @RequestBody ShoppingCartDto shoppingCart
    );

    @PostMapping("/add")
    void addProductToWarehouse(
            @RequestBody AddProductToWarehouseRequest request
    );

    @GetMapping("/address")
    AddressDto getWarehouseAddress();

    @PostMapping("/assembly")
    BookedProductsDto assemblyProductsForOrder(
            @RequestBody AssemblyProductsForOrderRequest request
    );

    @PostMapping("/shipped")
    void shippedToDelivery(
            @RequestBody ShippedToDeliveryRequest request
    );

    @PostMapping("/return")
    void acceptReturn(
            @RequestBody Map<UUID, Long> products
    );
}
