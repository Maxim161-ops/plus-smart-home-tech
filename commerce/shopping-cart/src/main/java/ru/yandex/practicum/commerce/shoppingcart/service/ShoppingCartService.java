package ru.yandex.practicum.commerce.shoppingcart.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.interaction.client.WarehouseClient;
import ru.yandex.practicum.commerce.interaction.dto.shoppingcart.ChangeProductQuantityRequest;
import ru.yandex.practicum.commerce.interaction.dto.shoppingcart.ShoppingCartDto;
import ru.yandex.practicum.commerce.shoppingcart.exception.InvalidProductQuantityException;
import ru.yandex.practicum.commerce.shoppingcart.exception.NoProductsInShoppingCartException;
import ru.yandex.practicum.commerce.shoppingcart.exception.NotAuthorizedUserException;
import ru.yandex.practicum.commerce.shoppingcart.exception.ShoppingCartDeactivatedException;
import ru.yandex.practicum.commerce.shoppingcart.mapper.ShoppingCartMapper;
import ru.yandex.practicum.commerce.shoppingcart.model.ShoppingCartEntity;
import ru.yandex.practicum.commerce.shoppingcart.repository.ShoppingCartRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShoppingCartService {

    private final ShoppingCartRepository shoppingCartRepository;
    private final ShoppingCartMapper shoppingCartMapper;
    private final WarehouseClient warehouseClient;

    @Transactional
    public ShoppingCartDto getShoppingCart(String username) {

        validateUsername(username);

        ShoppingCartEntity shoppingCart = shoppingCartRepository.findByUsername(username)
                .orElseGet(() -> createShoppingCart(username));

        return shoppingCartMapper.toDto(shoppingCart);
    }

    @Transactional
    public ShoppingCartDto addProductsToShoppingCart(
            String username,
            Map<UUID, Long> products) {

        validateUsername(username);

        if (products == null || products.isEmpty()) {
            throw new NoProductsInShoppingCartException();
        }

        boolean hasInvalidQuantity = products.values().stream()
                .anyMatch(quantity -> quantity == null || quantity <= 0);

        if (hasInvalidQuantity) {
            throw new InvalidProductQuantityException();
        }

        ShoppingCartEntity shoppingCart = shoppingCartRepository.findByUsername(username)
                .orElseGet(() -> createShoppingCart(username));

        if (!shoppingCart.isActive()) {
            throw new ShoppingCartDeactivatedException();
        }

        products.forEach((productId, quantity) ->
                shoppingCart.getProducts()
                        .merge(productId, quantity, Long::sum)
        );

        ShoppingCartDto cartDto =
                shoppingCartMapper.toDto(shoppingCart);

        warehouseClient
                .checkProductQuantityEnoughForShoppingCart(cartDto);

        ShoppingCartEntity savedShoppingCart =
                shoppingCartRepository.save(shoppingCart);

        return shoppingCartMapper.toDto(savedShoppingCart);
    }

    @Transactional
    public ShoppingCartDto removeProductsFromShoppingCart(
            String username,
            List<UUID> productIds) {

        validateUsername(username);

        ShoppingCartEntity shoppingCart = shoppingCartRepository.findByUsername(username)
                .orElseThrow(NoProductsInShoppingCartException::new);

        if (!shoppingCart.isActive()) {
            throw new ShoppingCartDeactivatedException();
        }

        if (productIds == null || productIds.isEmpty()) {
            throw new NoProductsInShoppingCartException();
        }

        boolean removed = false;

        for (UUID productId : productIds) {
            if (shoppingCart.getProducts().containsKey(productId)) {
                shoppingCart.getProducts().remove(productId);
                removed = true;
            }
        }

        if (!removed) {
            throw new NoProductsInShoppingCartException();
        }

        ShoppingCartEntity savedShoppingCart =
                shoppingCartRepository.save(shoppingCart);

        return shoppingCartMapper.toDto(savedShoppingCart);
    }

    @Transactional
    public ShoppingCartDto changeProductQuantity(
            String username,
            ChangeProductQuantityRequest request) {

        validateUsername(username);

        ShoppingCartEntity shoppingCart = shoppingCartRepository.findByUsername(username)
                .orElseThrow(NoProductsInShoppingCartException::new);

        if (!shoppingCart.isActive()) {
            throw new ShoppingCartDeactivatedException();
        }

        UUID productId = request.getProductId();

        if (!shoppingCart.getProducts().containsKey(productId)) {
            throw new NoProductsInShoppingCartException();
        }

        shoppingCart.getProducts().put(
                productId,
                request.getNewQuantity()
        );

        ShoppingCartDto cartDto =
                shoppingCartMapper.toDto(shoppingCart);

        warehouseClient
                .checkProductQuantityEnoughForShoppingCart(cartDto);

        ShoppingCartEntity savedShoppingCart =
                shoppingCartRepository.save(shoppingCart);

        return shoppingCartMapper.toDto(savedShoppingCart);
    }

    @Transactional
    public void deactivateShoppingCart(String username) {

        validateUsername(username);

        ShoppingCartEntity shoppingCart = shoppingCartRepository.findByUsername(username)
                .orElseThrow(NoProductsInShoppingCartException::new);

        shoppingCart.setActive(false);

        shoppingCartRepository.save(shoppingCart);
    }

    private ShoppingCartEntity createShoppingCart(String username) {

        ShoppingCartEntity shoppingCart = ShoppingCartEntity.builder()
                .shoppingCartId(UUID.randomUUID())
                .username(username)
                .active(true)
                .build();

        return shoppingCartRepository.save(shoppingCart);
    }

    private void validateUsername(String username) {

        if (username == null || username.isBlank()) {
            throw new NotAuthorizedUserException();
        }
    }
}
