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
import ru.yandex.practicum.commerce.interaction.dto.shoppingstore.SetProductQuantityStateRequest;
import ru.yandex.practicum.commerce.shoppingstore.exception.ProductNotFoundException;
import ru.yandex.practicum.commerce.shoppingstore.mapper.ProductMapper;
import ru.yandex.practicum.commerce.shoppingstore.model.Product;
import ru.yandex.practicum.commerce.shoppingstore.model.ProductState;
import ru.yandex.practicum.commerce.shoppingstore.repository.ProductRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Transactional(readOnly = true)
    public Page<ProductDto> getProducts(ProductCategory category, int page, int size, List<String> sort) {

        Sort sorting = Sort.unsorted();

        if (sort != null && !sort.isEmpty()) {
            List<Sort.Order> orders = sort.stream()
                    .map(value -> {
                        String[] parts = value.split(",");

                        String property = parts[0];

                        if (parts.length > 1 && parts[1].equalsIgnoreCase("desc")) {
                            return Sort.Order.desc(property);
                        }

                        return Sort.Order.asc(property);
                    })
                    .toList();

            sorting = Sort.by(orders);
        }

        Pageable pageable = PageRequest.of(page, size, sorting);

        Page<Product> products = productRepository
                .findAllByProductCategoryAndProductState(
                        productMapper.toModelCategory(category),
                        ProductState.ACTIVE,
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
        product.setProductState(ProductState.ACTIVE);

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
    public boolean setProductQuantityState(SetProductQuantityStateRequest request) {

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() ->
                        new ProductNotFoundException(request.getProductId())
                );

        product.setQuantityState(
                productMapper.toModelQuantityState(request.getQuantityState())
        );

        productRepository.save(product);

        return true;
    }
}
