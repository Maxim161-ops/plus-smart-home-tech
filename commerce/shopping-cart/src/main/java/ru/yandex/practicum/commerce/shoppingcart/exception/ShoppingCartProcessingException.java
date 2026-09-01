package ru.yandex.practicum.commerce.shoppingcart.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class ShoppingCartProcessingException extends RuntimeException {

    public ShoppingCartProcessingException(String message) {
        super(message);
    }
}
