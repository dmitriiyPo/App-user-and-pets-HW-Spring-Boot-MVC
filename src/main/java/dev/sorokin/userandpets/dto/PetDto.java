package dev.sorokin.userandpets.dto;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotBlank;

public record PetDto (

        Long id,

        @NotBlank
        String name,

        @Nonnull
        Long userId
) {
}
