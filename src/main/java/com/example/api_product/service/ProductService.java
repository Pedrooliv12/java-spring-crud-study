package com.example.api_product.service;

import com.example.api_product.dto.ProductRequestDTO;
import com.example.api_product.dto.ProductResponseDTO;
import com.example.api_product.model.Product;
import com.example.api_product.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductResponseDTO> findAll() {
        return productRepository.findAll().stream().map(p -> new ProductResponseDTO(p.getId(), p.getName(), p.getPrice())).toList();
    }

    public Optional<ProductResponseDTO> findById(Long id) {
        return productRepository.findById(id).map(p -> new ProductResponseDTO(p.getId(), p.getName(), p.getPrice()));
    }

    public ProductResponseDTO save(ProductRequestDTO dto) {
        Product product = new Product();
        product.setName(dto.getName());
        product.setPrice(dto.getPrice());
        product.setStockQuantity(dto.getStockQuantity());
        Product saved = productRepository.save(product);
        return new ProductResponseDTO(saved.getId(),  saved.getName(), saved.getPrice());
    }

    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }

    public Optional<ProductResponseDTO> update(Long id, ProductRequestDTO dto) {
        return productRepository.findById(id).map(existing ->  {
            existing.setName(dto.getName());
            existing.setPrice(dto.getPrice());
            existing.setStockQuantity(dto.getStockQuantity());
            Product saved = productRepository.save(existing);
            return new ProductResponseDTO(saved.getId(), saved.getName(), saved.getPrice());
        });
    }
}
