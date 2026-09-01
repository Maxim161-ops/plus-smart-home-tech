package ru.yandex.practicum.commerce.warehouse.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NoSpecifiedProductInWarehouseException extends RuntimeException {

    public NoSpecifiedProductInWarehouseException(UUID productId) {
        super("Товар не найден на складе: " + productId);
    }
}
