package com.thanh.foodorder.feature.category.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thanh.foodorder.core.response.ResultPaginationDTO;
import com.thanh.foodorder.feature.category.domain.Category;
import com.thanh.foodorder.feature.category.service.CategoryService;

@WebMvcTest(CategoryController.class)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;
    @Autowired
    private ObjectMapper objectMapper;
    private Category category;

    @BeforeEach
    void setUp() {

        objectMapper = new ObjectMapper();

        category = new Category();
        category.setId(1L);
        category.setName("Pizza");
    }

    @Test
    @DisplayName("POST /api/v1/categories - Success")
    void handleCreateCategory_Success() throws Exception {
        when(categoryService.createCategory(any(Category.class))).thenReturn(category);

        mockMvc.perform(post("/api/v1/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(category)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Pizza"));

        verify(categoryService, times(1)).createCategory(any(Category.class));
    }

    @Test
    @DisplayName("PUT /api/v1/categories - Success")
    void handleUpdateCategory_Success() throws Exception {
        when(categoryService.updateCategory(any(Category.class))).thenReturn(category);

        mockMvc.perform(put("/api/v1/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(category)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Pizza"));

        verify(categoryService, times(1)).updateCategory(any(Category.class));
    }

    @Test
    @DisplayName("DELETE /api/v1/categories/{id} - Success")
    void handleDeleteCategory_Success() throws Exception {
        doNothing().when(categoryService).deleteCategory(1L);

        mockMvc.perform(delete("/api/v1/categories/1"))
                .andExpect(status().isOk());

        verify(categoryService, times(1)).deleteCategory(1L);
    }

    @Test
    @DisplayName("GET /api/v1/categories/{id} - Success")
    void handleGetCategoryById_Success() throws Exception {
        when(categoryService.getCategoryById(1L)).thenReturn(category);

        mockMvc.perform(get("/api/v1/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Pizza"));

        verify(categoryService, times(1)).getCategoryById(1L);
    }

    @Test
    @DisplayName("GET /api/v1/categories - Success with pagination")
    void handleGetAllCategories_Success() throws Exception {
        ResultPaginationDTO paginationDTO = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(1);
        meta.setPageSize(10);
        meta.setPages(1);
        meta.setTotalElements(1L);
        paginationDTO.setMeta(meta);
        paginationDTO.setResults(List.of(category));

        when(categoryService.getAllCate(any(Pageable.class), eq("Pizza"))).thenReturn(paginationDTO);

        mockMvc.perform(get("/api/v1/categories")
                .param("name", "Pizza"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.meta.page").value(1))
                .andExpect(jsonPath("$.data.meta.pageSize").value(10))
                .andExpect(jsonPath("$.data.results[0].name").value("Pizza"));

        verify(categoryService, times(1)).getAllCate(any(Pageable.class), eq("Pizza"));
    }
}
