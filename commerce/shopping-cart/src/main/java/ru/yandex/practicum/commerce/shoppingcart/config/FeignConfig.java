package ru.yandex.practicum.commerce.shoppingcart.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor sourceServiceHeaderInterceptor() {
        return requestTemplate ->
                requestTemplate.header(
                        "X-Source-Service",
                        "shopping-cart"
                );
    }
}
