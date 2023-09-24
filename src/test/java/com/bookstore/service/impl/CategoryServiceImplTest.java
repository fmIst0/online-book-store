package com.bookstore.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.bookstore.dto.category.CategoryDto;
import com.bookstore.exception.DataBaseConflictException;
import com.bookstore.exception.EntityNotFoundException;
import com.bookstore.mapper.CategoryMapper;
import com.bookstore.model.Category;
import com.bookstore.repository.category.CategoryRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private CategoryMapper categoryMapper;
    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    @DisplayName("Verify findAll() method works")
    void findAll_ValidPageable_ReturnsAllCategories() {
        //Given
        Pageable pageable = PageRequest.of(0, 10);

        Category category = getCategory();
        List<Category> categories = List.of(category);

        CategoryDto categoryDto = getCategoryDtoFromCategory(category);
        List<CategoryDto> expected = List.of(categoryDto);

        Page<Category> categoryPage = new PageImpl<>(categories, pageable, categories.size());

        when(categoryRepository.findAll(pageable)).thenReturn(categoryPage);
        when(categoryMapper.toDto(category)).thenReturn(categoryDto);

        //When
        List<CategoryDto> actual = categoryService.findAll(pageable);

        //Then
        assertEquals(expected.size(), actual.size());
        assertThat(actual.get(0)).isEqualTo(categoryDto);

        verify(categoryRepository, times(1)).findAll(pageable);
        verify(categoryMapper, times(1)).toDto(category);
        verifyNoMoreInteractions(categoryRepository, categoryMapper);
    }

    @Test
    @DisplayName("Verify getById() method works with valid id")
    void getById_ValidId_ReturnsCategoryDto() {
        //Given
        Category category = getCategory();

        CategoryDto expected = getCategoryDtoFromCategory(category);

        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));
        when(categoryMapper.toDto(category)).thenReturn(expected);

        //When
        CategoryDto actual = categoryService.getById(category.getId());

        //Then
        assertEquals(expected, actual);

        verify(categoryRepository, times(1)).findById(anyLong());
        verify(categoryMapper, times(1)).toDto(category);
        verifyNoMoreInteractions(categoryRepository, categoryMapper);
    }

    @Test
    @DisplayName("Verify getById() method throws EntityNotFoundException with invalid id")
    void getById_InvalidId_ThrowsEntityNotFoundException() {
        //Given
        Long categoryId = -1L;
        String expected = "Can't find a category in DB by id: " + categoryId;

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        //When
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> categoryService.getById(categoryId));

        //Then
        String actual = exception.getMessage();
        assertEquals(EntityNotFoundException.class, exception.getClass());
        assertEquals(expected, actual);
        verify(categoryRepository, times(1)).findById(categoryId);
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    @DisplayName("Verify save() method works")
    void save_ValidCategoryRequestDto_ReturnsCategoryDto() {
        CategoryDto requestDto = getCategoryRequestDto();

        Category category = getCategoryFromCategoryDto(requestDto);

        CategoryDto expected = getCategoryDtoFromCategory(category);

        when(categoryMapper.toEntity(requestDto)).thenReturn(category);
        when(categoryRepository.save(category)).thenReturn(category);
        when(categoryMapper.toDto(category)).thenReturn(expected);

        //When
        CategoryDto actual = categoryService.save(requestDto);

        //Then
        assertEquals(expected, actual);
        verify(categoryRepository, times(1)).findCategoryByName(requestDto.getName());
        verify(categoryMapper, times(1)).toEntity(requestDto);
        verify(categoryRepository, times(1)).save(category);
        verify(categoryMapper, times(1)).toDto(category);
        verifyNoMoreInteractions(categoryMapper, categoryRepository);
    }

    @Test
    @DisplayName("Verify save() method works")
    void save_invalidCategoryRequestDtoNonUniqueName_ThrowsDataBaseConflictException() {
        CategoryDto requestDto = getCategoryRequestDto();

        String expected = "Category with name -" + requestDto.getName() + "- already exists.";

        when(categoryRepository.findCategoryByName(requestDto.getName()))
                .thenReturn(Optional.of(new Category()));

        //When
        DataBaseConflictException exception = assertThrows(DataBaseConflictException.class,
                () -> categoryService.save(requestDto));

        //Then
        String actual = exception.getMessage();

        assertEquals(DataBaseConflictException.class, exception.getClass());
        assertEquals(expected, actual);
        verify(categoryRepository, times(1)).findCategoryByName(requestDto.getName());
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    @DisplayName("Verify update() method works")
    void update_ValidId_ReturnsCategoryDto() {
        //Given
        CategoryDto requestDro = getCategoryRequestDto();

        Category category = getCategoryFromCategoryDto(requestDro);

        CategoryDto expected = getCategoryDtoFromCategory(category);

        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));
        when(categoryRepository.save(category)).thenReturn(category);
        when(categoryMapper.toDto(category)).thenReturn(expected);

        //When
        CategoryDto actual = categoryService.update(anyLong(), requestDro);

        //Then
        assertEquals(expected, actual);
        verify(categoryRepository, times(1)).findById(anyLong());
        verify(categoryRepository, times(1)).save(category);
        verify(categoryMapper, times(1)).toDto(category);
        verifyNoMoreInteractions(categoryRepository, categoryMapper);
    }

    @Test
    @DisplayName("Verify EntityNotFoundException was thrown with invalid id")
    void update_InvalidId_ThrowsEntityNotFoundException() {
        //Given
        Long categoryId = -1L;
        CategoryDto requestDto = getCategoryRequestDto();

        String expected = "Can't find a category in DB by id: " + categoryId;

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        //When
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> categoryService.update(categoryId, requestDto));

        //Then
        String actual = exception.getMessage();

        assertEquals(EntityNotFoundException.class, exception.getClass());
        assertEquals(expected, actual);
        verify(categoryRepository, times(1)).findById(categoryId);
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    @DisplayName("Verify deleteById() method works")
    void deleteById_ValidId_DoesNotThrowEntityNotFoundException() {
        //Given
        Long categoryId = 1L;

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(new Category()));

        //When
        categoryService.deleteById(categoryId);

        //Then
        verify(categoryRepository, times(1)).findById(categoryId);
        verify(categoryRepository, times(1)).deleteById(categoryId);
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    @DisplayName("Verify deleteById() method throws EntityNotFoundException with invalid id")
    void deleteById_InvalidId_ThrowsEntityNotFoundException() {
        //Given
        Long categoryId = -1L;

        String expected = "Can't delete a category from DB with id: " + categoryId;

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        //When
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> categoryService.deleteById(categoryId));

        //Then
        String actual = exception.getMessage();

        assertEquals(EntityNotFoundException.class, exception.getClass());
        assertEquals(expected, actual);
        verify(categoryRepository, times(1)).findById(categoryId);
        verifyNoMoreInteractions(categoryRepository);
    }

    private Category getCategory() {
        return new Category()
                .setId(1L)
                .setName("Test Category")
                .setDescription("Test Category Description");
    }

    private CategoryDto getCategoryRequestDto() {
        return new CategoryDto()
                .setName("Test Category")
                .setDescription("Test Category Description");
    }

    private CategoryDto getCategoryDtoFromCategory(Category category) {
        return new CategoryDto()
                .setName(category.getName())
                .setDescription(category.getDescription());
    }

    private Category getCategoryFromCategoryDto(CategoryDto categoryDto) {
        return new Category()
                .setName(categoryDto.getName())
                .setDescription(categoryDto.getDescription());
    }
}
