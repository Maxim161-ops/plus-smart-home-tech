package ru.yandex.practicum.commerce.shoppingstore.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.commerce.interaction.dto.shoppingstore.ProductDto;
import ru.yandex.practicum.commerce.shoppingstore.mapper.ProductMapper;
import ru.yandex.practicum.commerce.shoppingstore.model.Product;
import ru.yandex.practicum.commerce.shoppingstore.exception.ProductNotFoundException;
import ru.yandex.practicum.commerce.shoppingstore.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    @Test
    void getProduct_shouldReturnProductDto() {
        UUID productId = UUID.randomUUID();

        Product product = Product.builder()
                .productId(productId)
                .productName("Smart Lamp")
                .description("Умная лампа")
                .price(new BigDecimal("123.45"))
                .build();

        ProductDto productDto = ProductDto.builder()
                .productId(productId)
                .productName("Smart Lamp")
                .description("Умная лампа")
                .price(new BigDecimal("123.45"))
                .build();

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        when(productMapper.toDto(product))
                .thenReturn(productDto);

        ProductDto result = productService.getProduct(productId);

        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo(productId);
        assertThat(result.getProductName()).isEqualTo("Smart Lamp");
        assertThat(result.getDescription()).isEqualTo("Умная лампа");
        assertThat(result.getPrice())
                .isEqualByComparingTo(new BigDecimal("123.45"));
    }

    @Test
    void getProduct_shouldThrowExceptionWhenProductNotFound() {
        UUID productId = UUID.randomUUID();

        when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProduct(productId))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("Товар не найден: " + productId);
    }

    @Test
    void updateProduct_shouldUpdateProduct() {
        UUID productId = UUID.randomUUID();

        Product product = Product.builder()
                .productId(productId)
                .productName("Smart Lamp")
                .description("Умная лампа")
                .price(BigDecimal.valueOf(100.00))
                .build();

        ProductDto productDto = ProductDto.builder()
                .productId(productId)
                .productName("Smart Lamp")
                .description("Умная лампа")
                .price(BigDecimal.valueOf(123.45))
                .build();

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        when(productRepository.save(product))
                .thenReturn(product);

        when(productMapper.toDto(product))
                .thenReturn(productDto);

        ProductDto result = productService.updateProduct(productDto);

        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo(productId);
        assertThat(result.getProductName()).isEqualTo("Smart Lamp");
        assertThat(result.getDescription()).isEqualTo("Умная лампа");
        assertThat(result.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(123.45));
    }

    @Test
    void updateProduct_shouldThrowExceptionWhenProductNotFound() {
        UUID productId = UUID.randomUUID();

        ProductDto productDto = ProductDto.builder()
                .productId(productId)
                .productName("Smart Lamp")
                .description("Умная лампа")
                .price(BigDecimal.valueOf(123.45))
                .build();

        when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProduct(productDto))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("Товар не найден: " + productId);
    }
}