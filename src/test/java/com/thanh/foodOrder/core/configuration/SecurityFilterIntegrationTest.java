package com.thanh.foodorder.core.configuration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.thanh.foodorder.feature.category.service.CategoryService;
import com.thanh.foodorder.feature.product.service.ProductService;
import com.thanh.foodorder.feature.voucher.service.VoucherService;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityFilterIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private VoucherService voucherService;

    @Test
    @DisplayName("GET /api/v1/products - Should be permitted for anonymous visitors")
    void testGetProducts_PermitAll() throws Exception {
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/categories - Should be permitted for anonymous visitors")
    void testGetCategories_PermitAll() throws Exception {
        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/v1/products - Anonymous request should be rejected (401 Unauthorized)")
    void testPostProducts_Anonymous_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Test Food\", \"price\": 50000}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /api/v1/products/{id} - Anonymous request should be rejected (401 Unauthorized)")
    void testDeleteProducts_Anonymous_Unauthorized() throws Exception {
        mockMvc.perform(delete("/api/v1/products/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/v1/categories - Anonymous request should be rejected (401 Unauthorized)")
    void testPostCategories_Anonymous_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Fast Food\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/v1/products - USER role should be forbidden (403 Forbidden)")
    @WithMockUser(username = "customer@gmail.com", roles = { "USER" })
    void testPostProducts_WithUserRole_Forbidden() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Test Food\", \"price\": 50000}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/v1/products - ADMIN role should be allowed")
    @WithMockUser(username = "admin@gmail.com", roles = { "ADMIN" })
    void testPostProducts_WithAdminRole_Allowed() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Test Food\", \"price\": 50000}"))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("PUT /api/v1/vouchers/{id}/status - Anonymous request should be rejected (401 Unauthorized)")
    void testUpdateVoucherStatus_Anonymous_Unauthorized() throws Exception {
        mockMvc.perform(put("/api/v1/vouchers/1/status"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /api/v1/vouchers/{id}/status - USER role should be forbidden (403 Forbidden)")
    @WithMockUser(username = "customer@gmail.com", roles = { "USER" })
    void testUpdateVoucherStatus_WithUserRole_Forbidden() throws Exception {
        mockMvc.perform(put("/api/v1/vouchers/1/status"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/v1/vouchers/{id}/status - ADMIN role should be allowed (200 OK)")
    @WithMockUser(username = "admin@gmail.com", roles = { "ADMIN" })
    void testUpdateVoucherStatus_WithAdminRole_Allowed() throws Exception {
        mockMvc.perform(put("/api/v1/vouchers/1/status"))
                .andExpect(status().isOk());
    }
}
