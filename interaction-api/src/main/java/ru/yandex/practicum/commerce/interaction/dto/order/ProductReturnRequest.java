package ru.yandex.practicum.commerce.interaction.dto.order;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductReturnRequest {

    private UUID orderId;

    @NotNull
    private Map<UUID, Long> products;
}
