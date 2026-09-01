package ru.yandex.practicum.commerce.shoppingcart.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class ProductNotFoundException extends ShoppingCartProcessingException {

    public ProductNotFoundException(UUID productId) {
        super("Товар не найден: " + productId);
    }
}
