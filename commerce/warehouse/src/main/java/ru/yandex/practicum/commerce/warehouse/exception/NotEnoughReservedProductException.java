package ru.yandex.practicum.commerce.warehouse.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NotEnoughReservedProductException extends RuntimeException {

    public NotEnoughReservedProductException(UUID productId) {
        super("Недостаточно зарезервированного товара: " + productId);
    }
}
