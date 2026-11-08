package com.example.api_product.repository;

import com.example.api_product.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // Load the category in the same query (JOIN) to avoid the N+1 problem.
    @Override
    @EntityGraph(attributePaths = "category")
    Page<Product> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "category")
    Optional<Product> findById(Long id);
}
