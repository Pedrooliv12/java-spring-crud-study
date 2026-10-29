package com.example.api_product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;


public record ProductRequestDTO(@NotBlank String name, @Positive Double price, @PositiveOrZero Integer stockQuantity) {}
