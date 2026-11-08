package com.example.api_product.dto;

import java.math.BigDecimal;

public record ProductResponseDTO(Long id, String name, BigDecimal price, CategoryResponseDTO category) {}
