package ru.yandex.practicum.commerce.shoppingcart.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.commerce.interaction.dto.shoppingcart.ShoppingCartDto;
import ru.yandex.practicum.commerce.shoppingcart.model.ShoppingCartEntity;

import java.util.HashMap;

@Component
public class ShoppingCartMapper {

    public ShoppingCartDto toDto(ShoppingCartEntity entity) {
        return ShoppingCartDto.builder()
                .shoppingCartId(entity.getShoppingCartId())
                .products(new HashMap<>(entity.getProducts()))
                .build();
    }
}
