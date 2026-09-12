package ru.yandex.practicum.commerce.payment.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.yandex.practicum.commerce.interaction.dto.payment.PaymentState;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "payments", schema = "payment")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "payment_id", nullable = false)
    private UUID paymentId;

    @Column(name = "order_id", nullable = false, unique = true)
    private UUID orderId;

    @Column(name = "product_total", nullable = false)
    private BigDecimal productTotal;

    @Column(name = "delivery_total", nullable = false)
    private BigDecimal deliveryTotal;

    @Column(name = "total_payment", nullable = false)
    private BigDecimal totalPayment;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_state", nullable = false)
    private PaymentState paymentState;
}
