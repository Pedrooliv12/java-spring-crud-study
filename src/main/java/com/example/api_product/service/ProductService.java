package com.example.api_product.service;

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

    public List<Product> findAll() {return productRepository.findAll();}

    public Optional<Product> findById(Long id) {return productRepository.findById(id);}

    public Product save(Product product) {return productRepository.save(product);}

    public void deleteById(Long id) {productRepository.deleteById(id);}
}
