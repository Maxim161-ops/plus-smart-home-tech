package ru.yandex.practicum.commerce.shoppingstore.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.commerce.interaction.dto.shoppingstore.ProductDto;
import ru.yandex.practicum.commerce.interaction.dto.shoppingstore.ProductCategory;
import ru.yandex.practicum.commerce.interaction.dto.shoppingstore.ProductState;
import ru.yandex.practicum.commerce.interaction.dto.shoppingstore.QuantityState;
import ru.yandex.practicum.commerce.shoppingstore.model.Product;

@Component
public class ProductMapper {

    public ProductDto toDto(Product product) {
        return ProductDto.builder()
                .productId(product.getProductId())
                .productName(product.getProductName())
                .description(product.getDescription())
                .imageSrc(product.getImageSrc())
                .quantityState(
                        product.getQuantityState() == null
                                ? null
                                : QuantityState
                                .valueOf(product.getQuantityState().name())
                )
                .productState(
                        product.getProductState() == null
                                ? null
                                : ProductState
                                .valueOf(product.getProductState().name())
                )
                .productCategory(
                        product.getProductCategory() == null
                                ? null
                                : ProductCategory
                                .valueOf(product.getProductCategory().name())
                )
                .price(product.getPrice())
                .build();
    }

    public Product toEntity(ProductDto dto) {
        return Product.builder()
                .productId(dto.getProductId())
                .productName(dto.getProductName())
                .description(dto.getDescription())
                .imageSrc(dto.getImageSrc())
                .quantityState(
                        dto.getQuantityState() == null
                                ? null
                                : ru.yandex.practicum.commerce.shoppingstore.model.QuantityState
                                .valueOf(dto.getQuantityState().name())
                )
                .productState(
                        dto.getProductState() == null
                                ? null
                                : ru.yandex.practicum.commerce.shoppingstore.model.ProductState
                                .valueOf(dto.getProductState().name())
                )
                .productCategory(
                        dto.getProductCategory() == null
                                ? null
                                : ru.yandex.practicum.commerce.shoppingstore.model.ProductCategory
                                .valueOf(dto.getProductCategory().name())
                )
                .price(dto.getPrice())
                .build();
    }

    public ru.yandex.practicum.commerce.shoppingstore.model.ProductCategory toModelCategory(
            ProductCategory category
    ) {
        return category == null
                ? null
                : ru.yandex.practicum.commerce.shoppingstore.model.ProductCategory
                .valueOf(category.name());
    }

    public ru.yandex.practicum.commerce.shoppingstore.model.QuantityState toModelQuantityState(
            QuantityState quantityState
    ) {
        return quantityState == null
                ? null
                : ru.yandex.practicum.commerce.shoppingstore.model.QuantityState
                .valueOf(quantityState.name());
    }
}
