package ru.yandex.practicum.commerce.interaction.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.interaction.dto.shoppingstore.ProductCategory;
import ru.yandex.practicum.commerce.interaction.dto.shoppingstore.ProductDto;
import ru.yandex.practicum.commerce.interaction.dto.shoppingstore.QuantityState;

import java.util.List;
import java.util.UUID;

@FeignClient(
        name = "shopping-store",
        path = "/api/v1/shopping-store"
)
public interface ShoppingStoreClient {

    @GetMapping
    Page<ProductDto> getProducts(
            @RequestParam("category") ProductCategory category,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sort", required = false) List<String> sort
    );

    @PutMapping
    ProductDto createNewProduct(
            @RequestBody ProductDto productDto
    );

    @PostMapping
    ProductDto updateProduct(
            @RequestBody ProductDto productDto
    );

    @PostMapping("/removeProductFromStore")
    boolean removeProductFromStore(
            @RequestBody UUID productId
    );

    @PostMapping("/quantityState")
    boolean setProductQuantityState(
            @RequestParam("productId") UUID productId,
            @RequestParam("quantityState") QuantityState quantityState
    );

    @GetMapping("/{productId}")
    ProductDto getProduct(
            @PathVariable("productId") UUID productId
    );
}