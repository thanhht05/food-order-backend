package com.thanh.foodorder.feature.category.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.thanh.foodorder.core.response.ResultPaginationDTO;
import com.thanh.foodorder.core.util.exception.CommonException;
import com.thanh.foodorder.feature.category.domain.Category;
import com.thanh.foodorder.feature.category.repository.CategoryRepository;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Fast Food");
    }

    @Nested
    @DisplayName("checkExistsByName Tests")
    class CheckExistsByNameTests {

        @Test
        @DisplayName("Should return true when category name exists")
        void checkExistsByName_WhenExists_ShouldReturnTrue() {
            when(categoryRepository.existsByName("Fast Food")).thenReturn(true);

            boolean result = categoryService.checkExistsByName("Fast Food");

            assertTrue(result);
            verify(categoryRepository, times(1)).existsByName("Fast Food");
        }

        @Test
        @DisplayName("Should return false when category name does not exist")
        void checkExistsByName_WhenNotExists_ShouldReturnFalse() {
            when(categoryRepository.existsByName("Beverages")).thenReturn(false);

            boolean result = categoryService.checkExistsByName("Beverages");

            assertFalse(result);
            verify(categoryRepository, times(1)).existsByName("Beverages");
        }
    }

    @Nested
    @DisplayName("createCategory Tests")
    class CreateCategoryTests {

        @Test
        @DisplayName("Should create and return category when name is unique")
        void createCategory_WhenNameIsUnique_ShouldSaveAndReturnCategory() {
            when(categoryRepository.existsByName("Fast Food")).thenReturn(false);
            when(categoryRepository.save(category)).thenReturn(category);

            Category created = categoryService.createCategory(category);

            assertNotNull(created);
            assertEquals(1L, created.getId());
            assertEquals("Fast Food", created.getName());
            verify(categoryRepository, times(1)).existsByName("Fast Food");
            verify(categoryRepository, times(1)).save(category);
        }

        @Test
        @DisplayName("Should throw CommonException when category name already exists")
        void createCategory_WhenNameAlreadyExists_ShouldThrowCommonException() {
            when(categoryRepository.existsByName("Fast Food")).thenReturn(true);

            CommonException exception = assertThrows(CommonException.class, () -> {
                categoryService.createCategory(category);
            });

            assertEquals("Name Fast Food already exists", exception.getMessage());
            verify(categoryRepository, times(1)).existsByName("Fast Food");
            verify(categoryRepository, never()).save(any(Category.class));
        }
    }

    @Nested
    @DisplayName("getCategoryById Tests")
    class GetCategoryByIdTests {

        @Test
        @DisplayName("Should return category when found by ID")
        void getCategoryById_WhenFound_ShouldReturnCategory() {
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

            Category found = categoryService.getCategoryById(1L);

            assertNotNull(found);
            assertEquals(1L, found.getId());
            assertEquals("Fast Food", found.getName());
            verify(categoryRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("Should throw CommonException when category ID not found")
        void getCategoryById_WhenNotFound_ShouldThrowCommonException() {
            when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

            CommonException exception = assertThrows(CommonException.class, () -> {
                categoryService.getCategoryById(99L);
            });

            assertEquals("Category with id 99 not found", exception.getMessage());
            verify(categoryRepository, times(1)).findById(99L);
        }
    }

    @Nested
    @DisplayName("getCategoryByName Tests")
    class GetCategoryByNameTests {

        @Test
        @DisplayName("Should return category when found by name ignore case")
        void getCategoryByName_WhenFound_ShouldReturnCategory() {
            when(categoryRepository.findByNameIgnoreCase("fast food")).thenReturn(category);

            Category found = categoryService.getCategoryByName("fast food");

            assertNotNull(found);
            assertEquals("Fast Food", found.getName());
            verify(categoryRepository, times(1)).findByNameIgnoreCase("fast food");
        }

        @Test
        @DisplayName("Should throw CommonException when category name not found")
        void getCategoryByName_WhenNotFound_ShouldThrowCommonException() {
            when(categoryRepository.findByNameIgnoreCase("Unknown")).thenReturn(null);

            CommonException exception = assertThrows(CommonException.class, () -> {
                categoryService.getCategoryByName("Unknown");
            });

            assertEquals("Category with name Unknown not found", exception.getMessage());
            verify(categoryRepository, times(1)).findByNameIgnoreCase("Unknown");
        }
    }

    @Nested
    @DisplayName("deleteCategory Tests")
    class DeleteCategoryTests {

        @Test
        @DisplayName("Should delete category when category exists")
        void deleteCategory_WhenExists_ShouldDeleteCategory() {
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

            categoryService.deleteCategory(1L);

            verify(categoryRepository, times(1)).findById(1L);
            verify(categoryRepository, times(1)).delete(category);
        }

        @Test
        @DisplayName("Should throw CommonException and not delete when category ID not found")
        void deleteCategory_WhenNotFound_ShouldThrowCommonException() {
            when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

            CommonException exception = assertThrows(CommonException.class, () -> {
                categoryService.deleteCategory(99L);
            });

            assertEquals("Category with id 99 not found", exception.getMessage());
            verify(categoryRepository, times(1)).findById(99L);
            verify(categoryRepository, never()).delete(any(Category.class));
        }
    }

    @Nested
    @DisplayName("updateCategory Tests")
    class UpdateCategoryTests {

        @Test
        @DisplayName("Should update category successfully when name is unchanged")
        void updateCategory_WhenNameUnchanged_ShouldSaveAndReturnUpdated() {
            Category updatePayload = new Category();
            updatePayload.setId(1L);
            updatePayload.setName("Fast Food");

            when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
            when(categoryRepository.save(any(Category.class))).thenReturn(category);

            Category updated = categoryService.updateCategory(updatePayload);

            assertNotNull(updated);
            assertEquals("Fast Food", updated.getName());
            verify(categoryRepository, times(1)).findById(1L);
            verify(categoryRepository, never()).existsByName(any());
            verify(categoryRepository, times(1)).save(any(Category.class));
        }

        @Test
        @DisplayName("Should update category successfully when name changed and new name is available")
        void updateCategory_WhenNameChangedAndAvailable_ShouldSaveAndReturnUpdated() {
            Category updatePayload = new Category();
            updatePayload.setId(1L);
            updatePayload.setName("Healthy Food");

            when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
            when(categoryRepository.existsByName("Healthy Food")).thenReturn(false);
            when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Category updated = categoryService.updateCategory(updatePayload);

            assertNotNull(updated);
            assertEquals("Healthy Food", updated.getName());
            verify(categoryRepository, times(1)).findById(1L);
            verify(categoryRepository, times(1)).existsByName("Healthy Food");
            verify(categoryRepository, times(1)).save(any(Category.class));
        }

        @Test
        @DisplayName("Should throw CommonException when name changed but new name already exists")
        void updateCategory_WhenNameChangedAndAlreadyExists_ShouldThrowCommonException() {
            Category updatePayload = new Category();
            updatePayload.setId(1L);
            updatePayload.setName("Drinks");

            when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
            when(categoryRepository.existsByName("Drinks")).thenReturn(true);

            CommonException exception = assertThrows(CommonException.class, () -> {
                categoryService.updateCategory(updatePayload);
            });

            assertEquals("Name Drinks already exists", exception.getMessage());
            verify(categoryRepository, times(1)).findById(1L);
            verify(categoryRepository, times(1)).existsByName("Drinks");
            verify(categoryRepository, never()).save(any(Category.class));
        }

        @Test
        @DisplayName("Should throw CommonException when category to update does not exist")
        void updateCategory_WhenCategoryNotFound_ShouldThrowCommonException() {
            Category updatePayload = new Category();
            updatePayload.setId(99L);
            updatePayload.setName("Desserts");

            when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

            CommonException exception = assertThrows(CommonException.class, () -> {
                categoryService.updateCategory(updatePayload);
            });

            assertEquals("Category with id 99 not found", exception.getMessage());
            verify(categoryRepository, times(1)).findById(99L);
            verify(categoryRepository, never()).save(any(Category.class));
        }
    }

    @Nested
    @DisplayName("getAllCate Tests")
    class GetAllCateTests {

        @Test
        @DisplayName("Should return paginated result when searching by name")
        @SuppressWarnings("unchecked")
        void getAllCate_WithNameFilter_ShouldReturnPaginatedResults() {
            Pageable pageable = PageRequest.of(0, 10);
            List<Category> categoryList = List.of(category);
            Page<Category> categoryPage = new PageImpl<>(categoryList, pageable, 1);

            when(categoryRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(categoryPage);

            ResultPaginationDTO result = categoryService.getAllCate(pageable, "Fast");

            assertNotNull(result);
            assertNotNull(result.getMeta());
            assertEquals(1, result.getMeta().getPage());
            assertEquals(10, result.getMeta().getPageSize());
            assertEquals(1, result.getMeta().getPages());
            assertEquals(1L, result.getMeta().getTotalElements());
            List<Category> results = (List<Category>) result.getResults();
            assertEquals(1, results.size());
            verify(categoryRepository, times(1)).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        @DisplayName("Should return paginated result when name filter is null")
        @SuppressWarnings("unchecked")
        void getAllCate_WithoutNameFilter_ShouldReturnPaginatedResults() {
            Pageable pageable = PageRequest.of(1, 5);
            Category cat2 = new Category();
            cat2.setId(2L);
            cat2.setName("Beverages");

            List<Category> categoryList = List.of(category, cat2);
            Page<Category> categoryPage = new PageImpl<>(categoryList, pageable, 12);

            when(categoryRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(categoryPage);

            ResultPaginationDTO result = categoryService.getAllCate(pageable, null);

            assertNotNull(result);
            assertNotNull(result.getMeta());
            assertEquals(2, result.getMeta().getPage());
            assertEquals(5, result.getMeta().getPageSize());
            assertEquals(3, result.getMeta().getPages());
            assertEquals(12L, result.getMeta().getTotalElements());
            List<Category> results = (List<Category>) result.getResults();
            assertEquals(2, results.size());
            verify(categoryRepository, times(1)).findAll(any(Specification.class), eq(pageable));
        }
    }
}
