package com.example.api_product.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequestDTO (@NotBlank String name) {}
