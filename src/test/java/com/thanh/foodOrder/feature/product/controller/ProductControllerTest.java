package com.thanh.foodorder.feature.product.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thanh.foodorder.core.response.ResultPaginationDTO;
import com.thanh.foodorder.core.util.exception.CommonException;
import com.thanh.foodorder.feature.category.domain.Category;
import com.thanh.foodorder.feature.product.domain.Product;
import com.thanh.foodorder.feature.product.dto.ProductRequestDTO;
import com.thanh.foodorder.feature.product.dto.ProductUpdateRequestDTO;
import com.thanh.foodorder.feature.product.dto.ResponseProductDTO;
import com.thanh.foodorder.feature.product.enums.ProductStatus;
import com.thanh.foodorder.feature.product.service.ProductService;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    private Product product;
    private ResponseProductDTO responseProductDTO;

    @BeforeEach
    void setUp() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Pizza");

        product = new Product();
        product.setId(1L);
        product.setCategory(category);
        product.setName("cafe");
        product.setPrice(new BigDecimal("100000"));
        product.setQuantity(99);
        product.setStatus(ProductStatus.ACTIVE);

        ResponseProductDTO.ProductCate cate = ResponseProductDTO.ProductCate.builder()
                .id(category.getId())
                .name(category.getName())
                .build();

        ResponseProductDTO.ProductImage image = ResponseProductDTO.ProductImage.builder()
                .name("cafe.jpg")
                .build();

        responseProductDTO = ResponseProductDTO.builder()
                .id(1L)
                .name("cafe")
                .lstImg(List.of(image))
                .productCate(cate)
                .price(new BigDecimal("100000"))
                .productStatus(ProductStatus.ACTIVE)
                .quantity(99)
                .sold(0)
                .build();
    }

    @Nested
    @DisplayName("POST /api/v1/products Tests")
    class CreateProductTests {

        @Test
        @DisplayName("POST /api/v1/products - Success")
        void handleCreateProduct_Success() throws Exception {
            ProductRequestDTO req = ProductRequestDTO.builder()
                    .name("cafe")
                    .categoryName("Pizza")
                    .description("Delicious coffee")
                    .price(new BigDecimal("100000"))
                    .quantity(99)
                    .sold(0)
                    .lstImg(List.of("cafe.jpg"))
                    .build();

            when(productService.createProduct(any(ProductRequestDTO.class))).thenReturn(responseProductDTO);

            mockMvc.perform(post("/api/v1/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.name").value("cafe"))
                    .andExpect(jsonPath("$.data.price").value(100000));

            verify(productService, times(1)).createProduct(any(ProductRequestDTO.class));
        }

        @Test
        @DisplayName("POST /api/v1/products - Failure when category not found")
        void handleCreateProduct_CategoryNotFound_ShouldReturnBadRequest() throws Exception {
            ProductRequestDTO req = ProductRequestDTO.builder()
                    .name("cafe")
                    .categoryName("NonExistentCategory")
                    .price(new BigDecimal("100000"))
                    .build();

            when(productService.createProduct(any(ProductRequestDTO.class)))
                    .thenThrow(new CommonException("Category not found"));

            mockMvc.perform(post("/api/v1/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Category not found"));

            verify(productService, times(1)).createProduct(any(ProductRequestDTO.class));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/products Tests")
    class UpdateProductTests {

        @Test
        @DisplayName("PUT /api/v1/products - Success")
        void handleUpdateProduct_Success() throws Exception {
            ProductUpdateRequestDTO updateReq = new ProductUpdateRequestDTO();
            updateReq.setId(1L);
            updateReq.setName("Updated Cafe");
            updateReq.setPrice(new BigDecimal("120000"));
            updateReq.setQuantity(50);

            ResponseProductDTO updatedResponse = ResponseProductDTO.builder()
                    .id(1L)
                    .name("Updated Cafe")
                    .price(new BigDecimal("120000"))
                    .quantity(50)
                    .productStatus(ProductStatus.ACTIVE)
                    .build();

            when(productService.updateProduct(any(ProductUpdateRequestDTO.class))).thenReturn(updatedResponse);

            mockMvc.perform(put("/api/v1/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateReq)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.name").value("Updated Cafe"))
                    .andExpect(jsonPath("$.data.price").value(120000));

            verify(productService, times(1)).updateProduct(any(ProductUpdateRequestDTO.class));
        }

        @Test
        @DisplayName("PUT /api/v1/products - Failure when product ID not found")
        void handleUpdateProduct_NotFound_ShouldReturnBadRequest() throws Exception {
            ProductUpdateRequestDTO updateReq = new ProductUpdateRequestDTO();
            updateReq.setId(99L);
            updateReq.setName("Unknown");

            when(productService.updateProduct(any(ProductUpdateRequestDTO.class)))
                    .thenThrow(new CommonException("Product with id 99 not found"));

            mockMvc.perform(put("/api/v1/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateReq)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Product with id 99 not found"));

            verify(productService, times(1)).updateProduct(any(ProductUpdateRequestDTO.class));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/products/{productId}/status Tests")
    class UpdateProductStatusTests {

        @Test
        @DisplayName("PUT /api/v1/products/{productId}/status - Success")
        void putMethodName_Success() throws Exception {
            doNothing().when(productService).updateProductStatus(1L);

            mockMvc.perform(put("/api/v1/products/1/status"))
                    .andExpect(status().isOk());

            verify(productService, times(1)).updateProductStatus(1L);
        }

        @Test
        @DisplayName("PUT /api/v1/products/{productId}/status - Failure when product not found")
        void putMethodName_NotFound_ShouldReturnBadRequest() throws Exception {
            doThrow(new CommonException("Product with id 99 not found"))
                    .when(productService).updateProductStatus(99L);

            mockMvc.perform(put("/api/v1/products/99/status"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Product with id 99 not found"));

            verify(productService, times(1)).updateProductStatus(99L);
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/products/{id} Tests")
    class DeleteProductTests {

        @Test
        @DisplayName("DELETE /api/v1/products/{id} - Success")
        void handleDeletProduct_Success() throws Exception {
            doNothing().when(productService).handleDeleteProduct(1L);

            mockMvc.perform(delete("/api/v1/products/1"))
                    .andExpect(status().isOk());

            verify(productService, times(1)).handleDeleteProduct(1L);
        }

        @Test
        @DisplayName("DELETE /api/v1/products/{id} - Failure when product not found")
        void handleDeletProduct_NotFound_ShouldReturnBadRequest() throws Exception {
            doThrow(new CommonException("Product with id 99 not found"))
                    .when(productService).handleDeleteProduct(99L);

            mockMvc.perform(delete("/api/v1/products/99"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Product with id 99 not found"));

            verify(productService, times(1)).handleDeleteProduct(99L);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/products/{id} Tests")
    class GetProductByIdTests {

        @Test
        @DisplayName("GET /api/v1/products/{id} - Success")
        void handelGetProductById_Success() throws Exception {
            when(productService.getProductById(1L)).thenReturn(product);
            when(productService.convertToProductDTO(product)).thenReturn(responseProductDTO);

            mockMvc.perform(get("/api/v1/products/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.name").value("cafe"));

            verify(productService, times(1)).getProductById(1L);
            verify(productService, times(1)).convertToProductDTO(product);
        }

        @Test
        @DisplayName("GET /api/v1/products/{id} - Failure when product not found")
        void handelGetProductById_NotFound_ShouldReturnBadRequest() throws Exception {
            when(productService.getProductById(99L))
                    .thenThrow(new CommonException("Product with id 99 not found"));

            mockMvc.perform(get("/api/v1/products/99"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Product with id 99 not found"));

            verify(productService, times(1)).getProductById(99L);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/products (Search & Pagination) Tests")
    class GetHomePageTests {

        @Test
        @DisplayName("GET /api/v1/products - Success with all query parameters")
        void getHomePage_WithAllParams_Success() throws Exception {
            ResultPaginationDTO paginationDTO = new ResultPaginationDTO();
            ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
            meta.setPage(2);
            meta.setPageSize(5);
            meta.setPages(3);
            meta.setTotalElements(15L);
            paginationDTO.setMeta(meta);
            paginationDTO.setResults(List.of(responseProductDTO));

            when(productService.search(
                    eq("cafe"),
                    eq(List.of("Pizza", "Drink")),
                    eq(new BigDecimal("10000")),
                    eq(new BigDecimal("50000")),
                    eq(2),
                    eq(5),
                    eq("price,asc"))).thenReturn(paginationDTO);

            mockMvc.perform(get("/api/v1/products")
                    .param("keyword", "cafe")
                    .param("category", "Pizza", "Drink")
                    .param("from", "10000")
                    .param("to", "50000")
                    .param("page", "2")
                    .param("size", "5")
                    .param("sort", "price,asc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.meta.page").value(2))
                    .andExpect(jsonPath("$.data.meta.pageSize").value(5))
                    .andExpect(jsonPath("$.data.meta.totalElements").value(15))
                    .andExpect(jsonPath("$.data.results[0].name").value("cafe"));

            verify(productService, times(1)).search(
                    eq("cafe"),
                    eq(List.of("Pizza", "Drink")),
                    eq(new BigDecimal("10000")),
                    eq(new BigDecimal("50000")),
                    eq(2),
                    eq(5),
                    eq("price,asc"));
        }

        @Test
        @DisplayName("GET /api/v1/products - Success with default parameters")
        void getHomePage_WithDefaultParams_Success() throws Exception {
            ResultPaginationDTO paginationDTO = new ResultPaginationDTO();
            ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
            meta.setPage(1);
            meta.setPageSize(10);
            meta.setPages(1);
            meta.setTotalElements(1L);
            paginationDTO.setMeta(meta);
            paginationDTO.setResults(List.of(responseProductDTO));

            when(productService.search(eq(null), eq(null), eq(null), eq(null), eq(1), eq(10), eq(null)))
                    .thenReturn(paginationDTO);

            mockMvc.perform(get("/api/v1/products"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.meta.page").value(1))
                    .andExpect(jsonPath("$.data.meta.pageSize").value(10))
                    .andExpect(jsonPath("$.data.results[0].name").value("cafe"));

            verify(productService, times(1)).search(eq(null), eq(null), eq(null), eq(null), eq(1), eq(10), eq(null));
        }

        @Test
        @DisplayName("GET /api/v1/products - Success filtering by keyword only")
        void getHomePage_WithKeywordOnly_Success() throws Exception {
            ResultPaginationDTO paginationDTO = new ResultPaginationDTO();
            ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
            meta.setPage(1);
            meta.setPageSize(10);
            meta.setPages(1);
            meta.setTotalElements(1L);
            paginationDTO.setMeta(meta);
            paginationDTO.setResults(List.of(responseProductDTO));

            when(productService.search(eq("pizza"), eq(null), eq(null), eq(null), eq(1), eq(10), eq(null)))
                    .thenReturn(paginationDTO);

            mockMvc.perform(get("/api/v1/products")
                    .param("keyword", "pizza"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.meta.page").value(1))
                    .andExpect(jsonPath("$.data.results[0].name").value("cafe"));

            verify(productService, times(1)).search(eq("pizza"), eq(null), eq(null), eq(null), eq(1), eq(10), eq(null));
        }

        @Test
        @DisplayName("GET /api/v1/products - Success filtering by price range only")
        void getHomePage_WithPriceRangeOnly_Success() throws Exception {
            ResultPaginationDTO paginationDTO = new ResultPaginationDTO();
            ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
            meta.setPage(1);
            meta.setPageSize(10);
            meta.setPages(1);
            meta.setTotalElements(1L);
            paginationDTO.setMeta(meta);
            paginationDTO.setResults(List.of(responseProductDTO));

            when(productService.search(
                    eq(null),
                    eq(null),
                    eq(new BigDecimal("50000")),
                    eq(new BigDecimal("100000")),
                    eq(1),
                    eq(10),
                    eq(null))).thenReturn(paginationDTO);

            mockMvc.perform(get("/api/v1/products")
                    .param("from", "50000")
                    .param("to", "100000"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.meta.page").value(1));

            verify(productService, times(1)).search(
                    eq(null),
                    eq(null),
                    eq(new BigDecimal("50000")),
                    eq(new BigDecimal("100000")),
                    eq(1),
                    eq(10),
                    eq(null));
        }
    }
}
