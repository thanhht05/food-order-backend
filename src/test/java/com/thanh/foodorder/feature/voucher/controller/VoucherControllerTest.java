package com.thanh.foodorder.feature.voucher.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import java.time.LocalDate;

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
import com.thanh.foodorder.core.util.exception.CommonException;
import com.thanh.foodorder.feature.voucher.domain.Voucher;
import com.thanh.foodorder.feature.voucher.dto.UpdateVoucherStatusDTO;
import com.thanh.foodorder.feature.voucher.enums.VoucherStatus;
import com.thanh.foodorder.feature.voucher.service.VoucherService;

@WebMvcTest(VoucherController.class)
@AutoConfigureMockMvc(addFilters = false)
public class VoucherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private VoucherService voucherService;

    private Voucher voucher;

    @BeforeEach
    void setUp() {
        voucher = new Voucher();
        voucher.setId(1L);
        voucher.setCode("PROMO50");
        voucher.setPercentDiscount(50);
        voucher.setMaxDiscount(BigDecimal.valueOf(100000));
        voucher.setStatus(VoucherStatus.ACTIVE);
        voucher.setExpiration(LocalDate.now().plusDays(5));
        voucher.setUsageLimit(5);
    }

    @Nested
    @DisplayName("PUT /api/v1/vouchers/{id}/status Tests")
    class UpdateVoucherStatusTests {

        @Test
        @DisplayName("PUT /api/v1/vouchers/{id}/status - Toggle without body/params")
        void updateVoucherStatus_Toggle_Success() throws Exception {
            voucher.setStatus(VoucherStatus.INACTIVE);
            when(voucherService.updateVoucherStatus(1L, null)).thenReturn(voucher);

            mockMvc.perform(put("/api/v1/vouchers/1/status"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("INACTIVE"))
                    .andExpect(jsonPath("$.message").value("Update voucher status"));

            verify(voucherService, times(1)).updateVoucherStatus(1L, null);
        }

        @Test
        @DisplayName("PUT /api/v1/vouchers/{id}/status - With query param ?status=INACTIVE")
        void updateVoucherStatus_WithQueryParam_Success() throws Exception {
            voucher.setStatus(VoucherStatus.INACTIVE);
            when(voucherService.updateVoucherStatus(1L, VoucherStatus.INACTIVE)).thenReturn(voucher);

            mockMvc.perform(put("/api/v1/vouchers/1/status")
                    .param("status", "INACTIVE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("INACTIVE"));

            verify(voucherService, times(1)).updateVoucherStatus(1L, VoucherStatus.INACTIVE);
        }

        @Test
        @DisplayName("PUT /api/v1/vouchers/{id}/status - With JSON body")
        void updateVoucherStatus_WithJsonBody_Success() throws Exception {
            voucher.setStatus(VoucherStatus.INACTIVE);
            when(voucherService.updateVoucherStatus(1L, VoucherStatus.INACTIVE)).thenReturn(voucher);

            UpdateVoucherStatusDTO dto = new UpdateVoucherStatusDTO(VoucherStatus.INACTIVE);

            mockMvc.perform(put("/api/v1/vouchers/1/status")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("INACTIVE"));

            verify(voucherService, times(1)).updateVoucherStatus(1L, VoucherStatus.INACTIVE);
        }

        @Test
        @DisplayName("PUT /api/v1/vouchers/{id}/status - Not Found returns 400 Bad Request")
        void updateVoucherStatus_NotFound() throws Exception {
            when(voucherService.updateVoucherStatus(eq(99L), any()))
                    .thenThrow(new CommonException("Voucher with id 99 not found"));

            mockMvc.perform(put("/api/v1/vouchers/99/status"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Voucher with id 99 not found"));

            verify(voucherService, times(1)).updateVoucherStatus(eq(99L), any());
        }
    }

    @Nested
    @DisplayName("Other VoucherController Endpoint Tests")
    class OtherEndpointsTests {

        @Test
        @DisplayName("POST /api/v1/vouchers - Create voucher")
        void createVoucher_Success() throws Exception {
            when(voucherService.createVoucher(any(Voucher.class))).thenReturn(voucher);

            mockMvc.perform(post("/api/v1/vouchers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(voucher)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.code").value("PROMO50"))
                    .andExpect(jsonPath("$.data.status").value("ACTIVE"));

            verify(voucherService, times(1)).createVoucher(any(Voucher.class));
        }

        @Test
        @DisplayName("GET /api/v1/vouchers/{id} - Get voucher by id")
        void getVoucherById_Success() throws Exception {
            when(voucherService.getVoucherById(1L)).thenReturn(voucher);

            mockMvc.perform(get("/api/v1/vouchers/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.code").value("PROMO50"));

            verify(voucherService, times(1)).getVoucherById(1L);
        }

        @Test
        @DisplayName("DELETE /api/v1/vouchers/{id} - Delete voucher")
        void deleteVoucher_Success() throws Exception {
            mockMvc.perform(delete("/api/v1/vouchers/1"))
                    .andExpect(status().isOk());

            verify(voucherService, times(1)).delteVoucherById(1L);
        }
    }
}
