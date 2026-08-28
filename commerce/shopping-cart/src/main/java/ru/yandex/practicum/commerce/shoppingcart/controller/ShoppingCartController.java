package ru.yandex.practicum.commerce.shoppingcart.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.interaction.client.ShoppingCartClient;
import ru.yandex.practicum.commerce.interaction.dto.shoppingcart.ChangeProductQuantityRequest;
import ru.yandex.practicum.commerce.interaction.dto.shoppingcart.ShoppingCartDto;
import ru.yandex.practicum.commerce.shoppingcart.service.ShoppingCartService;


import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shopping-cart")
@RequiredArgsConstructor
public class ShoppingCartController implements ShoppingCartClient {

    private final ShoppingCartService shoppingCartService;

    @Override
    @GetMapping
    public ShoppingCartDto getShoppingCart(@RequestParam String username) {

        return shoppingCartService.getShoppingCart(username);
    }

    @PutMapping
    public ShoppingCartDto addProductsToShoppingCart(
            @RequestParam String username,
            @RequestBody Map<UUID, Long> products) {

        return shoppingCartService.addProductsToShoppingCart(
                username,
                products
        );
    }

    @DeleteMapping
    public void deactivateShoppingCart(@RequestParam String username) {

        shoppingCartService.deactivateShoppingCart(username);
    }

    @PostMapping("/remove")
    public ShoppingCartDto removeProductsFromShoppingCart(
            @RequestParam String username,
            @RequestBody List<UUID> productIds) {

        return shoppingCartService.removeProductsFromShoppingCart(
                username,
                productIds
        );
    }

    @PostMapping("/change-quantity")
    public ShoppingCartDto changeProductQuantity(
            @RequestParam String username,
            @Valid @RequestBody ChangeProductQuantityRequest request) {

        return shoppingCartService.changeProductQuantity(
                username,
                request
        );
    }
}
