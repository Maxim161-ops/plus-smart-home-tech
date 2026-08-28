package ru.yandex.practicum.commerce.warehouse.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class SpecifiedProductAlreadyInWarehouseException extends RuntimeException {

    public SpecifiedProductAlreadyInWarehouseException(UUID productId) {
        super("Товар уже зарегистрирован на складе: " + productId);
    }
}
