package ru.yandex.practicum.commerce.shoppingcart.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.interaction.client.ShoppingStoreClient;
import ru.yandex.practicum.commerce.interaction.client.WarehouseClient;
import ru.yandex.practicum.commerce.interaction.dto.shoppingcart.ChangeProductQuantityRequest;
import ru.yandex.practicum.commerce.interaction.dto.shoppingcart.ShoppingCartDto;
import ru.yandex.practicum.commerce.interaction.dto.shoppingstore.ProductDto;
import ru.yandex.practicum.commerce.interaction.dto.shoppingstore.ProductState;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.ProductQuantityRequest;
import ru.yandex.practicum.commerce.shoppingcart.exception.*;
import ru.yandex.practicum.commerce.shoppingcart.mapper.ShoppingCartMapper;
import ru.yandex.practicum.commerce.shoppingcart.model.ShoppingCartEntity;
import ru.yandex.practicum.commerce.shoppingcart.repository.ShoppingCartRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShoppingCartService {

    private final ShoppingCartRepository shoppingCartRepository;
    private final ShoppingCartMapper shoppingCartMapper;
    private final WarehouseClient warehouseClient;
    private final ShoppingStoreClient shoppingStoreClient;

    @Transactional
    public ShoppingCartDto getShoppingCart(String username) {

        validateUsername(username);

        ShoppingCartEntity shoppingCart = shoppingCartRepository.findByUsername(username)
                .orElseGet(() -> createShoppingCart(username));

        return shoppingCartMapper.toDto(shoppingCart);
    }

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

        for (UUID productId : products.keySet()) {
            checkProductInShoppingStore(productId);
        }

        Map<UUID, Long> reservedProducts = new HashMap<>();

        try {
            for (Map.Entry<UUID, Long> entry : products.entrySet()) {

                ProductQuantityRequest request =
                        ProductQuantityRequest.builder()
                                .productId(entry.getKey())
                                .quantity(entry.getValue())
                                .build();

                warehouseClient.reserveProduct(request);

                // Запоминаем только успешно созданные резервы
                reservedProducts.put(
                        entry.getKey(),
                        entry.getValue()
                );
            }

            products.forEach((productId, quantity) ->
                    shoppingCart.getProducts()
                            .merge(productId, quantity, Long::sum)
            );

            ShoppingCartEntity savedShoppingCart =
                    shoppingCartRepository.save(shoppingCart);

            return shoppingCartMapper.toDto(savedShoppingCart);

        } catch (Exception e) {

            //  Компенсация уже созданных резервов
            releaseReservedProducts(reservedProducts);

            if (e instanceof FeignException.NotFound) {
                throw new ShoppingCartProcessingException(
                        "Складская запись не найдена"
                );
            }

            if (e instanceof FeignException.Conflict) {
                throw new ShoppingCartProcessingException(
                        "Недостаточно товара на складе"
                );
            }

            if (e instanceof ShoppingCartProcessingException processingException) {
                throw processingException;
            }

            throw new ShoppingCartProcessingException(
                    "Не удалось добавить товары в корзину"
            );
        }
    }

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

        // Товары которые действительно есть в корзине
        Map<UUID, Long> productsToRemove = new HashMap<>();

        for (UUID productId : productIds) {

            Long quantity = shoppingCart.getProducts().get(productId);

            if (quantity != null) {
                productsToRemove.put(productId, quantity);
            }
        }

        if (productsToRemove.isEmpty()) {
            throw new NoProductsInShoppingCartException();
        }

        // Сюда записываем только те резервы,
        // которые удалось успешно снять
        Map<UUID, Long> releasedProducts = new HashMap<>();

        try {

            for (Map.Entry<UUID, Long> entry : productsToRemove.entrySet()) {

                ProductQuantityRequest request =
                        ProductQuantityRequest.builder()
                                .productId(entry.getKey())
                                .quantity(entry.getValue())
                                .build();

                warehouseClient.releaseProduct(request);

                releasedProducts.put(
                        entry.getKey(),
                        entry.getValue()
                );
            }

            // Склад успешно обработан — теперь меняем корзину
            for (UUID productId : productsToRemove.keySet()) {
                shoppingCart.getProducts().remove(productId);
            }

            ShoppingCartEntity savedShoppingCart =
                    shoppingCartRepository.save(shoppingCart);

            return shoppingCartMapper.toDto(savedShoppingCart);

        } catch (FeignException.NotFound e) {

            restoreReleasedProducts(releasedProducts);

            throw new ShoppingCartProcessingException(
                    "Складская запись не найдена"
            );

        } catch (FeignException.BadRequest e) {

            restoreReleasedProducts(releasedProducts);

            throw new ShoppingCartProcessingException(
                    "Не удалось снять резерв товара"
            );

        } catch (Exception e) {

            restoreReleasedProducts(releasedProducts);

            throw new ShoppingCartProcessingException(
                    "Не удалось удалить товары из корзины"
            );
        }
    }

    public ShoppingCartDto changeProductQuantity(String username, ChangeProductQuantityRequest request) {

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

        Long oldQuantity = shoppingCart.getProducts().get(productId);
        Long newQuantity = request.getNewQuantity();

        if (newQuantity == null || newQuantity <= 0) {
            throw new InvalidProductQuantityException();
        }

        long difference = newQuantity - oldQuantity;

        // Количество вообще не изменилось
        if (difference == 0) {
            return shoppingCartMapper.toDto(shoppingCart);
        }

        ProductQuantityRequest warehouseRequest =
                ProductQuantityRequest.builder()
                        .productId(productId)
                        .quantity(Math.abs(difference))
                        .build();

        boolean warehouseChanged = false;

        try {
            if (difference > 0) {
                warehouseClient.reserveProduct(warehouseRequest);
            } else {
                warehouseClient.releaseProduct(warehouseRequest);
            }

            warehouseChanged = true;

            shoppingCart.getProducts().put(
                    productId,
                    newQuantity
            );

            ShoppingCartEntity savedShoppingCart =
                    shoppingCartRepository.save(shoppingCart);

            return shoppingCartMapper.toDto(savedShoppingCart);

        } catch (FeignException.NotFound e) {

            compensateQuantityChange(
                    warehouseRequest,
                    difference,
                    warehouseChanged
            );

            throw new ShoppingCartProcessingException(
                    "Складская запись не найдена"
            );

        } catch (FeignException.Conflict e) {

            compensateQuantityChange(
                    warehouseRequest,
                    difference,
                    warehouseChanged
            );

            throw new ShoppingCartProcessingException(
                    "Недостаточно товара на складе"
            );

        } catch (FeignException.BadRequest e) {

            compensateQuantityChange(
                    warehouseRequest,
                    difference,
                    warehouseChanged
            );

            throw new ShoppingCartProcessingException(
                    "Не удалось изменить резерв товара"
            );

        } catch (Exception e) {

            compensateQuantityChange(
                    warehouseRequest,
                    difference,
                    warehouseChanged
            );

            throw new ShoppingCartProcessingException(
                    "Не удалось изменить количество товара"
            );
        }
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

    private void checkProductInShoppingStore(UUID productId) {

        try {
            ProductDto product =
                    shoppingStoreClient.getProduct(productId);

            if (product.getProductState() != ProductState.ACTIVE) {
                throw new ShoppingCartProcessingException(
                        "Товар недоступен для продажи: " + productId
                );
            }

        } catch (FeignException.NotFound e) {
            throw new ProductNotFoundException(productId);

        } catch (FeignException e) {
            throw new ShoppingCartProcessingException(
                    "Ошибка при обращении к сервису товаров"
            );
        }
    }

    private void releaseReservedProducts(Map<UUID, Long> reservedProducts) {

        for (Map.Entry<UUID, Long> entry : reservedProducts.entrySet()) {

            ProductQuantityRequest request =
                    ProductQuantityRequest.builder()
                            .productId(entry.getKey())
                            .quantity(entry.getValue())
                            .build();

            try {
                warehouseClient.releaseProduct(request);

            } catch (FeignException e) {
                log.error(
                        "Не удалось снять резерв товара {} в количестве {}",
                        entry.getKey(),
                        entry.getValue(),
                        e
                );
            }
        }
    }

    private void compensateQuantityChange(
            ProductQuantityRequest request,
            long difference,
            boolean warehouseChanged) {

        if (!warehouseChanged) {
            return;
        }

        try {
            if (difference > 0) {
                warehouseClient.releaseProduct(request);
            } else {
                warehouseClient.reserveProduct(request);
            }

        } catch (FeignException e) {
            log.error(
                    "Не удалось выполнить компенсацию для товара {}",
                    request.getProductId(),
                    e
            );
        }
    }

    private void restoreReleasedProducts(Map<UUID, Long> releasedProducts) {

        for (Map.Entry<UUID, Long> entry : releasedProducts.entrySet()) {

            ProductQuantityRequest request =
                    ProductQuantityRequest.builder()
                            .productId(entry.getKey())
                            .quantity(entry.getValue())
                            .build();

            try {
                warehouseClient.reserveProduct(request);

            } catch (FeignException e) {
                log.error(
                        "Не удалось восстановить резерв товара {} в количестве {}",
                        entry.getKey(),
                        entry.getValue(),
                        e
                );
            }
        }
    }
}
