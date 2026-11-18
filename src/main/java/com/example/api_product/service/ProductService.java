package com.example.api_product.service;

import com.example.api_product.dto.ProductRequestDTO;
import com.example.api_product.dto.ProductResponseDTO;
import com.example.api_product.exception.CategoryNotFoundException;
import com.example.api_product.exception.ProductNotFoundException;
import com.example.api_product.mapper.ProductMapper;
import com.example.api_product.model.Category;
import com.example.api_product.model.Product;
import com.example.api_product.repository.CategoryRepository;
import com.example.api_product.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productMapper = productMapper;
    }

    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> findAll(String search, Long categoryId, Boolean lowStock, Pageable pageable) {
        Specification<Product> spec = (root, query, cb) -> cb.conjunction();

        if (search != null && !search.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("name")), "%" + search.toLowerCase() + "%"));
        }
        if (categoryId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("category").get("id"), categoryId));
        }
        if (Boolean.TRUE.equals(lowStock)) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("stockQuantity"), 5));
        }

        return productRepository.findAll(spec, pageable).map(productMapper::toProductResponseDTO);
    }

    @Transactional(readOnly = true)
    public ProductResponseDTO findById(Long id) {
        return productRepository.findById(id).map(productMapper::toProductResponseDTO)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Transactional
    public ProductResponseDTO save(ProductRequestDTO dto) {
        Category category = findCategory(dto.categoryId());
        Product product = new Product();
        product.setName(dto.name());
        product.setPrice(dto.price());
        product.setStockQuantity(dto.stockQuantity());
        product.setCategory(category);
        Product saved = productRepository.save(product);
        return productMapper.toProductResponseDTO(saved);
    }

    @Transactional
    public ProductResponseDTO update(Long id, ProductRequestDTO dto) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        Category category = findCategory(dto.categoryId());
        existing.setName(dto.name());
        existing.setPrice(dto.price());
        existing.setStockQuantity(dto.stockQuantity());
        existing.setCategory(category);
        Product saved = productRepository.save(existing);
        return productMapper.toProductResponseDTO(saved);
    }

    @Transactional
    public void deleteById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        product.setDeletedAt(Instant.now());
        productRepository.save(product);
    }

    private Category findCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));
    }
}
