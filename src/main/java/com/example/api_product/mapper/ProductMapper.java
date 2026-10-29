package com.example.api_product.mapper;

import com.example.api_product.dto.ProductResponseDTO;
import com.example.api_product.model.Product;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductResponseDTO toProductResponseDTO(Product product);
}
