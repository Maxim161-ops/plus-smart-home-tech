package ru.yandex.practicum.commerce.interaction.client;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.commerce.interaction.dto.shoppingcart.ChangeProductQuantityRequest;
import ru.yandex.practicum.commerce.interaction.dto.shoppingcart.ShoppingCartDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@FeignClient(
        name = "shopping-cart",
        path = "/api/v1/shopping-cart"
)
public interface ShoppingCartClient {

    @GetMapping
    ShoppingCartDto getShoppingCart(
            @RequestParam("username") String username
    );

    @PutMapping
    ShoppingCartDto addProductsToShoppingCart(
            @RequestParam("username") String username,
            @RequestBody Map<UUID, Long> products
    );

    @DeleteMapping
    void deactivateShoppingCart(
            @RequestParam("username") String username
    );

    @PostMapping("/remove")
    ShoppingCartDto removeProductsFromShoppingCart(
            @RequestParam("username") String username,
            @RequestBody List<UUID> productIds
    );

    @PostMapping("/change-quantity")
    ShoppingCartDto changeProductQuantity(
            @RequestParam("username") String username,
            @Valid @RequestBody ChangeProductQuantityRequest request
    );
}
