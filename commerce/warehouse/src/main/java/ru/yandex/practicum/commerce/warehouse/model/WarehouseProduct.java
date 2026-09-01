package ru.yandex.practicum.commerce.warehouse.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "products", schema = "warehouse")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseProduct {

    @Id
    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "quantity", nullable = false)
    private Long quantity;

    @Builder.Default
    @Column(name = "reserved_quantity", nullable = false)
    private Long reservedQuantity = 0L;

    @Column(name = "width", nullable = false)
    private Double width;

    @Column(name = "height", nullable = false)
    private Double height;

    @Column(name = "depth", nullable = false)
    private Double depth;

    @Column(name = "weight", nullable = false)
    private Double weight;

    @Column(name = "fragile", nullable = false)
    private Boolean fragile;
}
