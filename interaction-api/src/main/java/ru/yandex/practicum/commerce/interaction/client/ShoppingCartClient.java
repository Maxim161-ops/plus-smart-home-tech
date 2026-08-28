package ru.yandex.practicum.commerce.interaction.client;

import ru.yandex.practicum.commerce.interaction.dto.shoppingcart.ChangeProductQuantityRequest;
import ru.yandex.practicum.commerce.interaction.dto.shoppingcart.ShoppingCartDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ShoppingCartClient {

    ShoppingCartDto getShoppingCart(String username);

    ShoppingCartDto addProductsToShoppingCart(
            String username,
            Map<UUID, Long> products
    );

    ShoppingCartDto removeProductsFromShoppingCart(
            String username,
            List<UUID> productIds
    );

    ShoppingCartDto changeProductQuantity(
            String username,
            ChangeProductQuantityRequest request
    );

    void deactivateShoppingCart(String username);
}
