package ru.yandex.practicum.commerce.interaction.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.commerce.interaction.dto.shoppingcart.ShoppingCartDto;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.AddressDto;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateNewOrderRequest {

    @Valid
    @NotNull
    private ShoppingCartDto shoppingCart;

    @Valid
    @NotNull
    private AddressDto deliveryAddress;
}