package ru.yandex.practicum.commerce.interaction.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.commerce.interaction.dto.shoppingcart.ShoppingCartDto;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.*;

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

    @PostMapping("/reserve")
    void reserveProduct(
            @RequestBody ProductQuantityRequest request
    );

    @PostMapping("/release")
    void releaseProduct(
            @RequestBody ProductQuantityRequest request
    );
}
