package ru.yandex.practicum.commerce.payment.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.commerce.interaction.dto.order.OrderDto;
import ru.yandex.practicum.commerce.interaction.dto.payment.PaymentDto;
import ru.yandex.practicum.commerce.interaction.dto.payment.PaymentState;
import ru.yandex.practicum.commerce.payment.model.PaymentEntity;

import java.math.BigDecimal;

@Component
public class PaymentMapper {

    public PaymentDto toDto(PaymentEntity payment) {

        BigDecimal feeTotal = payment.getTotalPayment()
                .subtract(payment.getProductTotal())
                .subtract(payment.getDeliveryTotal());

        return PaymentDto.builder()
                .paymentId(payment.getPaymentId())
                .totalPayment(payment.getTotalPayment())
                .deliveryTotal(payment.getDeliveryTotal())
                .feeTotal(feeTotal)
                .build();
    }

    public PaymentEntity toEntity(
            OrderDto order,
            BigDecimal productTotal,
            BigDecimal deliveryTotal,
            BigDecimal totalPayment
    ) {
        return PaymentEntity.builder()
                .orderId(order.getOrderId())
                .productTotal(productTotal)
                .deliveryTotal(deliveryTotal)
                .totalPayment(totalPayment)
                .paymentState(PaymentState.PENDING)
                .build();
    }
}