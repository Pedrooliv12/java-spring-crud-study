package com.example.api_product.service;

import com.example.api_product.dto.ProductRequestDTO;
import com.example.api_product.dto.ProductResponseDTO;
import com.example.api_product.exception.ProductNotFoundException;
import com.example.api_product.mapper.ProductMapper;
import com.example.api_product.model.Product;
import com.example.api_product.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductService(ProductRepository productRepository,  ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    public List<ProductResponseDTO> findAll() {
        return productRepository.findAll().stream().map(p -> productMapper.toProductResponseDTO(p)).toList();
    }

    public ProductResponseDTO findById(Long id) {
        return productRepository.findById(id).map(p -> productMapper.toProductResponseDTO(p))
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    // MapStruct
    public ProductResponseDTO save(ProductRequestDTO dto) {
        Product product = new Product();
        product.setName(dto.name());
        product.setPrice(dto.price());
        product.setStockQuantity(dto.stockQuantity());
        Product saved = productRepository.save(product);
        return productMapper.toProductResponseDTO(saved);
    }

    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }

    public ProductResponseDTO update(Long id, ProductRequestDTO dto) {
        return productRepository.findById(id).map(existing ->  {
            existing.setName(dto.name());
            existing.setPrice(dto.price());
            existing.setStockQuantity(dto.stockQuantity());
            Product saved = productRepository.save(existing);
            return productMapper.toProductResponseDTO(saved);
        }).orElseThrow(() -> new ProductNotFoundException(id));
    }
}
