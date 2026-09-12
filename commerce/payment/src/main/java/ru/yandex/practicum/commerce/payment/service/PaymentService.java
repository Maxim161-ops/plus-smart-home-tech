package ru.yandex.practicum.commerce.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.interaction.client.OrderClient;
import ru.yandex.practicum.commerce.interaction.client.ShoppingStoreClient;
import ru.yandex.practicum.commerce.interaction.dto.order.OrderDto;
import ru.yandex.practicum.commerce.interaction.dto.payment.PaymentDto;
import ru.yandex.practicum.commerce.interaction.dto.payment.PaymentState;
import ru.yandex.practicum.commerce.interaction.dto.shoppingstore.ProductDto;
import ru.yandex.practicum.commerce.payment.exception.PaymentNotFoundException;
import ru.yandex.practicum.commerce.payment.mapper.PaymentMapper;
import ru.yandex.practicum.commerce.payment.model.PaymentEntity;
import ru.yandex.practicum.commerce.payment.repository.PaymentRepository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final BigDecimal TAX_RATE = new BigDecimal("0.10");

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final ShoppingStoreClient shoppingStoreClient;
    private final OrderClient orderClient;


    @Transactional(readOnly = true)
    public BigDecimal productCost(OrderDto order) {

        log.info("Расчёт стоимости товаров для orderId={}", order.getOrderId());

        BigDecimal productTotal = BigDecimal.ZERO;

        for (Map.Entry<UUID, Long> entry : order.getProducts().entrySet()) {

            UUID productId = entry.getKey();
            Long quantity = entry.getValue();

            ProductDto product =
                    shoppingStoreClient.getProduct(productId);

            BigDecimal productCost = product.getPrice()
                    .multiply(BigDecimal.valueOf(quantity));

            log.info(
                    "Товар productId={}, price={}, quantity={}, cost={}",
                    productId,
                    product.getPrice(),
                    quantity,
                    productCost
            );

            productTotal = productTotal.add(productCost);
        }

        log.info(
                "Итоговая стоимость товаров orderId={}, productTotal={}",
                order.getOrderId(),
                productTotal
        );

        return productTotal;
    }


    @Transactional(readOnly = true)
    public BigDecimal getTotalCost(OrderDto order) {

        BigDecimal productTotal = productCost(order);

        BigDecimal deliveryTotal = order.getDeliveryPrice() != null
                ? order.getDeliveryPrice()
                : BigDecimal.ZERO;

        BigDecimal totalPayment =
                calculateTotal(productTotal, deliveryTotal);

        log.info(
                "Общая стоимость orderId={}: productTotal={}, deliveryTotal={}, totalPayment={}",
                order.getOrderId(),
                productTotal,
                deliveryTotal,
                totalPayment
        );

        return totalPayment;
    }


    @Transactional
    public PaymentDto payment(OrderDto order) {

        log.info("Создание платежа для orderId={}", order.getOrderId());

        BigDecimal productTotal = productCost(order);

        BigDecimal deliveryTotal = order.getDeliveryPrice() != null
                ? order.getDeliveryPrice()
                : BigDecimal.ZERO;

        BigDecimal totalPayment =
                calculateTotal(productTotal, deliveryTotal);

        PaymentEntity payment = paymentMapper.toEntity(
                order,
                productTotal,
                deliveryTotal,
                totalPayment
        );

        PaymentEntity savedPayment = paymentRepository.save(payment);

        log.info(
                "Платёж создан paymentId={}, orderId={}, state={}, total={}",
                savedPayment.getPaymentId(),
                savedPayment.getOrderId(),
                savedPayment.getPaymentState(),
                savedPayment.getTotalPayment()
        );

        return paymentMapper.toDto(savedPayment);
    }

    private BigDecimal calculateTotal(
            BigDecimal productTotal,
            BigDecimal deliveryTotal
    ) {
        BigDecimal feeTotal = productTotal.multiply(TAX_RATE);

        return productTotal
                .add(deliveryTotal)
                .add(feeTotal);
    }

    @Transactional
    public void paymentSuccess(UUID paymentId) {

        log.info("Успешная оплата paymentId={}", paymentId);

        PaymentEntity payment = getPaymentOrThrow(paymentId);

        payment.setPaymentState(PaymentState.SUCCESS);

        paymentRepository.save(payment);

        log.info(
                "Payment paymentId={} -> SUCCESS, уведомляем orderId={}",
                paymentId,
                payment.getOrderId()
        );

        orderClient.payment(payment.getOrderId());
    }


    @Transactional
    public void paymentFailed(UUID paymentId) {

        log.warn("Ошибка оплаты paymentId={}", paymentId);

        PaymentEntity payment = getPaymentOrThrow(paymentId);

        payment.setPaymentState(PaymentState.FAILED);

        paymentRepository.save(payment);

        log.warn(
                "Payment paymentId={} -> FAILED, уведомляем orderId={}",
                paymentId,
                payment.getOrderId()
        );

        orderClient.paymentFailed(payment.getOrderId());
    }

    private PaymentEntity getPaymentOrThrow(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));
    }
}
