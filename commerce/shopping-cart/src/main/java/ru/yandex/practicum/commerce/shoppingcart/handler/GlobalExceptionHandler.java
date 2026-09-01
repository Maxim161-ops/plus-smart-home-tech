package ru.yandex.practicum.commerce.shoppingcart.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.commerce.shoppingcart.exception.ShoppingCartProcessingException;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ShoppingCartProcessingException.class)
    public ResponseEntity<Map<String, String>> handleShoppingCartProcessingException(
            ShoppingCartProcessingException exception) {

        Map<String, String> response = Map.of(
                "error", exception.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(response);
    }
}
