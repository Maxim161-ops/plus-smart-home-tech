package ru.yandex.practicum.commerce.shoppingcart.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NoProductsInShoppingCartException extends RuntimeException {

    public NoProductsInShoppingCartException() {
        super("В корзине нет указанных товаров");
    }
}
