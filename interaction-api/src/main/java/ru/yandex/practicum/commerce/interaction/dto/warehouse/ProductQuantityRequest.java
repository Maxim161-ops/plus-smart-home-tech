package ru.yandex.practicum.commerce.interaction.dto.warehouse;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductQuantityRequest {

    @NotNull
    private UUID productId;

    @NotNull
    @Positive
    private Long quantity;
}
