package ru.yandex.practicum.commerce.warehouse.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.interaction.dto.shoppingcart.ShoppingCartDto;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.AddressDto;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.NewProductInWarehouseRequest;
import ru.yandex.practicum.commerce.warehouse.mapper.WarehouseMapper;
import ru.yandex.practicum.commerce.warehouse.model.WarehouseProduct;
import ru.yandex.practicum.commerce.warehouse.repository.WarehouseRepository;
import ru.yandex.practicum.commerce.warehouse.exception.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.commerce.warehouse.exception.ProductInShoppingCartLowQuantityInWarehouseException;
import ru.yandex.practicum.commerce.warehouse.exception.SpecifiedProductAlreadyInWarehouseException;

import java.security.SecureRandom;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WarehouseService {

    private static final String[] ADDRESSES =
            new String[]{"ADDRESS_1", "ADDRESS_2"};

    private static final String CURRENT_ADDRESS =
            ADDRESSES[new SecureRandom().nextInt(ADDRESSES.length)];

    private final WarehouseRepository warehouseRepository;
    private final WarehouseMapper warehouseMapper;

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
}
