package com.example.api_product.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;


public record ProductRequestDTO(@NotBlank String name, @Positive @NotNull @Digits(integer = 8, fraction = 2) BigDecimal price, @PositiveOrZero @NotNull Integer stockQuantity, @NotNull Long categoryId) {}
