package ru.yandex.practicum.commerce.warehouse.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.interaction.dto.shoppingcart.ShoppingCartDto;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.*;
import ru.yandex.practicum.commerce.warehouse.mapper.WarehouseMapper;
import ru.yandex.practicum.commerce.warehouse.model.OrderBooking;
import ru.yandex.practicum.commerce.warehouse.model.WarehouseProduct;
import ru.yandex.practicum.commerce.warehouse.repository.OrderBookingRepository;
import ru.yandex.practicum.commerce.warehouse.repository.WarehouseRepository;
import ru.yandex.practicum.commerce.warehouse.exception.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.commerce.warehouse.exception.ProductInShoppingCartLowQuantityInWarehouseException;
import ru.yandex.practicum.commerce.warehouse.exception.SpecifiedProductAlreadyInWarehouseException;

import java.security.SecureRandom;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WarehouseService {

    private static final String[] ADDRESSES =
            new String[]{"ADDRESS_1", "ADDRESS_2"};

    private static final String CURRENT_ADDRESS =
            ADDRESSES[new SecureRandom().nextInt(ADDRESSES.length)];

    private final WarehouseRepository warehouseRepository;
    private final WarehouseMapper warehouseMapper;
    private final OrderBookingRepository orderBookingRepository;

    @Transactional
    public void newProductInWarehouse(NewProductInWarehouseRequest request) {

        if (warehouseRepository.existsById(request.getProductId())) {
            throw new SpecifiedProductAlreadyInWarehouseException(
                    request.getProductId()
            );
        }

        WarehouseProduct product = warehouseMapper.toEntity(request);

        warehouseRepository.save(product);
    }

    @Transactional
    public void addProductToWarehouse(AddProductToWarehouseRequest request) {

        WarehouseProduct product =
                getProductOrThrow(request.getProductId());

        product.setQuantity(
                product.getQuantity() + request.getQuantity()
        );

        warehouseRepository.save(product);
    }

    @Transactional(readOnly = true)
    public BookedProductsDto checkProductQuantityEnoughForShoppingCart(
            ShoppingCartDto shoppingCart
    ) {

        double deliveryWeight = 0.0;
        double deliveryVolume = 0.0;
        boolean fragile = false;

        for (Map.Entry<UUID, Long> entry
                : shoppingCart.getProducts().entrySet()) {

            UUID productId = entry.getKey();
            Long requestedQuantity = entry.getValue();

            WarehouseProduct product =
                    getProductOrThrow(productId);

            if (product.getQuantity() < requestedQuantity) {
                throw new ProductInShoppingCartLowQuantityInWarehouseException(
                        productId
                );
            }

            deliveryWeight +=
                    product.getWeight() * requestedQuantity;

            deliveryVolume +=
                    product.getWidth()
                            * product.getHeight()
                            * product.getDepth()
                            * requestedQuantity;

            if (Boolean.TRUE.equals(product.getFragile())) {
                fragile = true;
            }
        }

        return BookedProductsDto.builder()
                .deliveryWeight(deliveryWeight)
                .deliveryVolume(deliveryVolume)
                .fragile(fragile)
                .build();
    }

    @Transactional(readOnly = true)
    public AddressDto getWarehouseAddress() {

        return AddressDto.builder()
                .country(CURRENT_ADDRESS)
                .city(CURRENT_ADDRESS)
                .street(CURRENT_ADDRESS)
                .house(CURRENT_ADDRESS)
                .flat(CURRENT_ADDRESS)
                .build();
    }

    private WarehouseProduct getProductOrThrow(UUID productId) {
        return warehouseRepository.findById(productId)
                .orElseThrow(() ->
                        new NoSpecifiedProductInWarehouseException(productId)
                );
    }

    @Transactional
    public BookedProductsDto assemblyProductsForOrder(
            AssemblyProductsForOrderRequest request
    ) {
        log.info(
                "Начало сборки заказа. orderId={}, products={}",
                request.getOrderId(),
                request.getProducts()
        );

        double deliveryWeight = 0.0;
        double deliveryVolume = 0.0;
        boolean fragile = false;

        for (Map.Entry<UUID, Long> entry : request.getProducts().entrySet()) {

            UUID productId = entry.getKey();
            Long requestedQuantity = entry.getValue();

            log.info(
                    "Проверяем товар productId={}, requestedQuantity={}",
                    productId,
                    requestedQuantity
            );

            WarehouseProduct product;

            try {
                product = getProductOrThrow(productId);
            } catch (Exception e) {
                log.error(
                        "Не удалось получить товар со склада productId={}",
                        productId,
                        e
                );
                throw e;
            }

            log.info(
                    "Товар найден productId={}, warehouseQuantity={}",
                    productId,
                    product.getQuantity()
            );

            if (product.getQuantity() < requestedQuantity) {

                log.error(
                        "Недостаточно товара productId={}, requested={}, available={}",
                        productId,
                        requestedQuantity,
                        product.getQuantity()
                );

                throw new ProductInShoppingCartLowQuantityInWarehouseException(
                        productId
                );
            }

            deliveryWeight += product.getWeight() * requestedQuantity;

            deliveryVolume += product.getWidth()
                    * product.getHeight()
                    * product.getDepth()
                    * requestedQuantity;

            if (Boolean.TRUE.equals(product.getFragile())) {
                fragile = true;
            }

            product.setQuantity(
                    product.getQuantity() - requestedQuantity
            );

            warehouseRepository.save(product);

            log.info(
                    "Товар списан productId={}, осталось={}",
                    productId,
                    product.getQuantity()
            );
        }

        OrderBooking orderBooking = OrderBooking.builder()
                .orderId(request.getOrderId())
                .products(request.getProducts())
                .build();

        orderBookingRepository.save(orderBooking);

        log.info(
                "Сборка заказа завершена. orderId={}, weight={}, volume={}, fragile={}",
                request.getOrderId(),
                deliveryWeight,
                deliveryVolume,
                fragile
        );

        return BookedProductsDto.builder()
                .deliveryWeight(deliveryWeight)
                .deliveryVolume(deliveryVolume)
                .fragile(fragile)
                .build();
    }

    @Transactional
    public void acceptReturn(Map<UUID, Long> products) {

        for (Map.Entry<UUID, Long> entry : products.entrySet()) {

            WarehouseProduct product =
                    getProductOrThrow(entry.getKey());

            product.setQuantity(
                    product.getQuantity() + entry.getValue()
            );

            warehouseRepository.save(product);
        }
    }

    @Transactional
    public void shippedToDelivery(
            ShippedToDeliveryRequest request
    ) {
        OrderBooking orderBooking = orderBookingRepository
                .findByOrderId(request.getOrderId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Order booking not found: "
                                        + request.getOrderId()
                        )
                );

        orderBooking.setDeliveryId(request.getDeliveryId());

        orderBookingRepository.save(orderBooking);
    }
}
