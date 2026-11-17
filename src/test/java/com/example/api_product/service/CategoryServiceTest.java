package com.example.api_product.service;

import com.example.api_product.dto.CategoryRequestDTO;
import com.example.api_product.dto.CategoryResponseDTO;
import com.example.api_product.exception.CategoryAlreadyExistsException;
import com.example.api_product.exception.CategoryNotFoundException;
import com.example.api_product.mapper.CategoryMapper;
import com.example.api_product.model.Category;
import com.example.api_product.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Spy
    private CategoryMapper categoryMapper = Mappers.getMapper(CategoryMapper.class);

    @InjectMocks
    private CategoryService categoryService;

    private Category peripherals;

    @BeforeEach
    void setUp() {
        peripherals = new Category("Peripherals");
        peripherals.setId(1L);
    }

    @Test
    void shouldReturnAllCategories() {
        Category audio = new Category("Audio");
        audio.setId(2L);
        when(categoryRepository.findAll()).thenReturn(List.of(peripherals, audio));

        List<CategoryResponseDTO> response = categoryService.findAll();

        assertThat(response).hasSize(2);
        assertThat(response.get(0).name()).isEqualTo("Peripherals");
        assertThat(response.get(1).name()).isEqualTo("Audio");
    }

    @Test
    void shouldReturnCategoryWhenIdExists() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(peripherals));

        CategoryResponseDTO response = categoryService.findById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Peripherals");
    }

    @Test
    void shouldThrowWhenCategoryNotFound() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.findById(999L))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessage("Category not found with id: 999");
    }

    @Test
    void shouldSaveCategoryWhenNameDoesNotExist() {
        CategoryRequestDTO dto = new CategoryRequestDTO("Audio");
        when(categoryRepository.existsByName("Audio")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category c = invocation.getArgument(0);
            c.setId(2L);
            return c;
        });

        CategoryResponseDTO response = categoryService.save(dto);

        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Audio");
        assertThat(response.id()).isEqualTo(2L);
        assertThat(response.name()).isEqualTo("Audio");
    }

    @Test
    void shouldThrowAndNotSaveWhenNameAlreadyExists() {
        CategoryRequestDTO dto = new CategoryRequestDTO("Peripherals");
        when(categoryRepository.existsByName("Peripherals")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.save(dto))
                .isInstanceOf(CategoryAlreadyExistsException.class);

        verify(categoryRepository, never()).save(any());
    }

    @Test
    void shouldUpdateCategoryName() {
        CategoryRequestDTO dto = new CategoryRequestDTO("Audio");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(peripherals));
        when(categoryRepository.existsByName("Audio")).thenReturn(false);
        when(categoryRepository.save(peripherals)).thenReturn(peripherals);

        CategoryResponseDTO response = categoryService.update(1L, dto);

        verify(categoryRepository).save(peripherals);
        assertThat(peripherals.getName()).isEqualTo("Audio");
        assertThat(response.name()).isEqualTo("Audio");
    }

    @Test
    void shouldNotThrowWhenUpdatingWithSameName() {
        CategoryRequestDTO dto = new CategoryRequestDTO("Peripherals");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(peripherals));
        when(categoryRepository.save(peripherals)).thenReturn(peripherals);

        CategoryResponseDTO response = categoryService.update(1L, dto);

        verify(categoryRepository, never()).existsByName(any());
        assertThat(response.name()).isEqualTo("Peripherals");
    }

    @Test
    void shouldThrowWhenUpdatingWithExistingName() {
        CategoryRequestDTO dto = new CategoryRequestDTO("Audio");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(peripherals));
        when(categoryRepository.existsByName("Audio")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.update(1L, dto))
                .isInstanceOf(CategoryAlreadyExistsException.class);

        verify(categoryRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenUpdatingNonExistentCategory() {
        CategoryRequestDTO dto = new CategoryRequestDTO("Audio");
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.update(999L, dto))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessage("Category not found with id: 999");

        verify(categoryRepository, never()).save(any());
    }

    @Test
    void shouldDeleteExistingCategory() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(peripherals));

        categoryService.deleteById(1L);

        verify(categoryRepository).delete(peripherals);
    }

    @Test
    void shouldThrowAndNotDeleteWhenCategoryDoesNotExist() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.deleteById(999L))
                .isInstanceOf(CategoryNotFoundException.class);

        verify(categoryRepository, never()).delete(any());
    }
}
