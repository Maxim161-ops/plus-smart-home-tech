package ru.yandex.practicum.commerce.shoppingstore.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.interaction.dto.shoppingstore.ProductCategory;
import ru.yandex.practicum.commerce.interaction.dto.shoppingstore.ProductDto;
import ru.yandex.practicum.commerce.interaction.dto.shoppingstore.QuantityState;
import ru.yandex.practicum.commerce.shoppingstore.exception.ProductNotFoundException;
import ru.yandex.practicum.commerce.shoppingstore.mapper.ProductMapper;
import ru.yandex.practicum.commerce.shoppingstore.model.Product;
import ru.yandex.practicum.commerce.shoppingstore.model.ProductState;
import ru.yandex.practicum.commerce.shoppingstore.repository.ProductRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Transactional(readOnly = true)
    public Page<ProductDto> getProducts(
            ProductCategory category,
            int page,
            int size,
            List<String> sort
    ) {

        Sort sorting = Sort.unsorted();

        if (sort != null && !sort.isEmpty()) {

            List<String> parts = sort.stream()
                    .flatMap(value -> Arrays.stream(value.split(",")))
                    .toList();

            List<Sort.Order> orders = new ArrayList<>();

            for (int i = 0; i < parts.size(); i += 2) {

                String property = parts.get(i);

                Sort.Direction direction = Sort.Direction.ASC;

                if (i + 1 < parts.size()
                        && parts.get(i + 1).equalsIgnoreCase("desc")) {
                    direction = Sort.Direction.DESC;
                }

                orders.add(new Sort.Order(direction, property));
            }

            sorting = Sort.by(orders);
        }

        Pageable pageable = PageRequest.of(page, size, sorting);

        Page<Product> products = productRepository
                .findAllByProductCategory(
                        productMapper.toModelCategory(category),
                        pageable
                );

        return products.map(productMapper::toDto);
    }

    @Transactional(readOnly = true)
    public ProductDto getProduct(UUID productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException(productId)
                );

        return productMapper.toDto(product);
    }

    @Transactional
    public ProductDto createProduct(ProductDto productDto) {

        Product product = productMapper.toEntity(productDto);

        product.setProductId(UUID.randomUUID());

        Product savedProduct = productRepository.save(product);

        return productMapper.toDto(savedProduct);
    }

    @Transactional
    public ProductDto updateProduct(ProductDto productDto) {

        Product product = productRepository.findById(productDto.getProductId())
                .orElseThrow(() ->
                        new ProductNotFoundException(productDto.getProductId())
                );

        product.setProductName(productDto.getProductName());
        product.setDescription(productDto.getDescription());
        product.setImageSrc(productDto.getImageSrc());

        product.setProductCategory(
                productMapper.toModelCategory(productDto.getProductCategory())
        );

        product.setPrice(productDto.getPrice());

        product.setQuantityState(
                productMapper.toModelQuantityState(productDto.getQuantityState())
        );

        Product savedProduct = productRepository.save(product);

        return productMapper.toDto(savedProduct);
    }

    @Transactional
    public boolean removeProductFromStore(UUID productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException(productId)
                );

        product.setProductState(ProductState.DEACTIVATE);

        productRepository.save(product);

        return true;
    }

    @Transactional
    public boolean setProductQuantityState(
            UUID productId,
            QuantityState quantityState
    ) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException(productId)
                );

        product.setQuantityState(
                productMapper.toModelQuantityState(quantityState)
        );

        productRepository.save(product);

        return true;
    }
}
