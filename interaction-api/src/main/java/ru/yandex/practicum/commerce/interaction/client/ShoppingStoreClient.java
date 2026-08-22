package ru.yandex.practicum.commerce.interaction.client;

import org.springframework.data.domain.Page;
import ru.yandex.practicum.commerce.interaction.dto.shoppingstore.ProductDto;
import ru.yandex.practicum.commerce.interaction.dto.shoppingstore.ProductCategory;
import ru.yandex.practicum.commerce.interaction.dto.shoppingstore.SetProductQuantityStateRequest;

import java.util.List;
import java.util.UUID;

public interface ShoppingStoreClient {

    Page<ProductDto> getProducts(
            ProductCategory category,
            int page,
            int size,
            List<String> sort
    );

    ProductDto createNewProduct(ProductDto productDto);

    ProductDto updateProduct(ProductDto productDto);

    boolean removeProductFromStore(UUID productId);

    boolean setProductQuantityState(
            SetProductQuantityStateRequest request
    );

    ProductDto getProduct(UUID productId);
}