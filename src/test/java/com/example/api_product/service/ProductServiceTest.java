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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Spy
    private ProductMapper productMapper = Mappers.getMapper(ProductMapper.class);

    @InjectMocks
    private ProductService productService;

    private Category peripherals;

    @BeforeEach
    void setUp() {
        peripherals = new Category("Peripherals");
        peripherals.setId(1L);
    }

    @Test
    void shouldReturnProductWhenIdExists() {
        Product product = new Product(1L, "Keyboard", new BigDecimal("49.99"), 10);
        product.setCategory(peripherals);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductResponseDTO response = productService.findById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Keyboard");
        assertThat(response.price()).isEqualByComparingTo("49.99");
        assertThat(response.category().name()).isEqualTo("Peripherals");
    }

    @Test
    void shouldThrowWhenProductNotFound() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(999L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("Product not found with id: 999");
    }

    @Test
    void shouldSaveProductWithRequestData() {
        ProductRequestDTO dto = new ProductRequestDTO("Keyboard", new BigDecimal("49.99"), 10, 1L);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(peripherals));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponseDTO response = productService.save(dto);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());
        Product savedProduct = productCaptor.getValue();
        assertThat(savedProduct.getName()).isEqualTo("Keyboard");
        assertThat(savedProduct.getPrice()).isEqualByComparingTo("49.99");
        assertThat(savedProduct.getStockQuantity()).isEqualTo(10);
        assertThat(savedProduct.getCategory()).isSameAs(peripherals);
        assertThat(response.name()).isEqualTo("Keyboard");
        assertThat(response.category().name()).isEqualTo("Peripherals");
    }

    @Test
    void shouldThrowAndNotSaveWhenCategoryDoesNotExist() {
        ProductRequestDTO dto = new ProductRequestDTO("Keyboard", new BigDecimal("49.99"), 10, 999L);
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.save(dto))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessage("Category not found with id: 999");

        verify(productRepository, never()).save(any());
    }

    @Test
    void shouldUpdateExistingProduct() {
        Product existing = new Product(1L, "Keyboard", new BigDecimal("49.99"), 10);
        Category accessories = new Category("Accessories");
        accessories.setId(2L);
        existing.setCategory(peripherals);
        ProductRequestDTO dto = new ProductRequestDTO("MousePad", new BigDecimal("9.99"), 1, 2L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(accessories));
        when(productRepository.save(existing)).thenReturn(existing);

        ProductResponseDTO response = productService.update(1L, dto);

        verify(productRepository).save(existing);
        assertThat(existing.getName()).isEqualTo("MousePad");
        assertThat(existing.getPrice()).isEqualByComparingTo("9.99");
        assertThat(existing.getStockQuantity()).isEqualTo(1);
        assertThat(existing.getCategory()).isSameAs(accessories);
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("MousePad");
        assertThat(response.category().name()).isEqualTo("Accessories");
    }

    @Test
    void shouldThrowAndNotSaveWhenUpdatingNonExistentProduct() {
        ProductRequestDTO dto = new ProductRequestDTO("MousePad", new BigDecimal("9.99"), 1, 1L);
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.update(999L, dto))
                .isInstanceOf(ProductNotFoundException.class);

        verify(productRepository, never()).save(any());
    }

    @Test
    void shouldThrowAndNotSaveWhenUpdatingWithNonExistentCategory() {
        Product existing = new Product(1L, "Keyboard", new BigDecimal("49.99"), 10);
        existing.setCategory(peripherals);
        ProductRequestDTO dto = new ProductRequestDTO("MousePad", new BigDecimal("9.99"), 1, 999L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.update(1L, dto))
                .isInstanceOf(CategoryNotFoundException.class);

        verify(productRepository, never()).save(any());
    }

    @Test
    void shouldDeleteExistingProduct() {
        Product product = new Product(1L, "Keyboard", new BigDecimal("49.99"), 10);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.deleteById(1L);

        verify(productRepository).delete(product);
    }

    @Test
    void shouldThrowAndNotDeleteWhenProductDoesNotExist() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.deleteById(999L))
                .isInstanceOf(ProductNotFoundException.class);

        verify(productRepository, never()).delete(any());
    }
}
