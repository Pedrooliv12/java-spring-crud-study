package com.example.api_product.service;

import com.example.api_product.dto.CategoryRequestDTO;
import com.example.api_product.dto.CategoryResponseDTO;
import com.example.api_product.exception.CategoryAlreadyExistsException;
import com.example.api_product.exception.CategoryNotFoundException;
import com.example.api_product.mapper.CategoryMapper;
import com.example.api_product.model.Category;
import com.example.api_product.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> findAll() {
        return categoryRepository.findAll()
                .stream()
                .map(categoryMapper::toCategoryResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponseDTO findById(Long id) {
        return categoryRepository.findById(id).map(categoryMapper::toCategoryResponseDTO)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }

    @Transactional
    public CategoryResponseDTO save(CategoryRequestDTO dto) {
        if (categoryRepository.existsByName(dto.name())) {
            throw new CategoryAlreadyExistsException(dto.name());
        }

        Category category = new Category();
        category.setName(dto.name());
        Category saved = categoryRepository.save(category);
        return categoryMapper.toCategoryResponseDTO(saved);
    }

    @Transactional
    public CategoryResponseDTO update(Long id, CategoryRequestDTO dto) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));

        if (!existing.getName().equals(dto.name()) && categoryRepository.existsByName(dto.name())) {
            throw new CategoryAlreadyExistsException(dto.name());
        }

        existing.setName(dto.name());
        Category saved = categoryRepository.save(existing);
        return categoryMapper.toCategoryResponseDTO(saved);
    }

    @Transactional
    public void deleteById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
        categoryRepository.delete(category);
    }
}
