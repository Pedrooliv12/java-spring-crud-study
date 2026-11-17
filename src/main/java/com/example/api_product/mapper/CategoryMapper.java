package com.example.api_product.mapper;

import com.example.api_product.dto.CategoryResponseDTO;
import com.example.api_product.model.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryResponseDTO toCategoryResponseDTO(Category category);
}
