package ru.yandex.practicum.commerce.payment.exception;

import java.util.UUID;

public class PaymentNotFoundException extends RuntimeException {

    public PaymentNotFoundException(UUID paymentId) {
        super("Оплата не найдена: " + paymentId);
    }
}