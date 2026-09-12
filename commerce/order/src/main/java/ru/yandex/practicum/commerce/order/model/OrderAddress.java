package ru.yandex.practicum.commerce.order.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderAddress {

    @Column(name = "delivery_country")
    private String country;

    @Column(name = "delivery_city")
    private String city;

    @Column(name = "delivery_street")
    private String street;

    @Column(name = "delivery_house")
    private String house;

    @Column(name = "delivery_flat")
    private String flat;
}
