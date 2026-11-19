package com.thanh.foodorder.feature.order.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thanh.foodorder.core.util.exception.CommonException;
import com.thanh.foodorder.feature.order.dto.CheckoutRequestDTO;
import com.thanh.foodorder.feature.order.dto.OrderResponseDTO;
import com.thanh.foodorder.feature.order.enums.OrderStatus;
import com.thanh.foodorder.feature.order.enums.PaymentStatus;
import com.thanh.foodorder.feature.order.service.OrderService;
import com.thanh.foodorder.feature.user.domain.Address;
import com.thanh.foodorder.feature.user.domain.User;
import com.thanh.foodorder.feature.user.service.UserService;

@WebMvcTest(PlaceOrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class PlaceOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private UserService userService;

    private User sampleUser;
    private CheckoutRequestDTO sampleCheckoutRequestDTO;
    private OrderResponseDTO sampleOrderResponseDTO;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setFullName("Nguyen Van A");
        sampleUser.setEmail("user@example.com");

        Address address = new Address();
        address.setRecipientName("Nguyen Van A");
        address.setPhone("0987654321");
        address.setAddressDetail("123 Duong Le Loi");
        address.setProvince("Ha Noi");
        address.setWard("Ben Nghe");

        sampleCheckoutRequestDTO = new CheckoutRequestDTO();
        sampleCheckoutRequestDTO.setCartDetailIds(List.of(1L, 2L));
        sampleCheckoutRequestDTO.setVoucherCode("GIAM20K");
        sampleCheckoutRequestDTO.setNote("Giao gio hanh chinh");
        sampleCheckoutRequestDTO.setPaymentMethod("PAYOS");
        sampleCheckoutRequestDTO.setShippingAddress(address);

        sampleOrderResponseDTO = OrderResponseDTO.builder()
                .orderId(101L)
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.PENDING.name())
                .totalPrice(BigDecimal.valueOf(250000))
                .discount(BigDecimal.valueOf(20000))
                .paymentStatus(PaymentStatus.UNPAID)
                .customerName("Nguyen Van A")
                .email("user@example.com")
                .address(address)
                .items(new ArrayList<>())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateUser(String email) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication auth = new UsernamePasswordAuthenticationToken(email, null, Collections.emptyList());
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    // ==========================================
    // POST /api/v1/orders/placeOrder Tests
    // ==========================================
    @Nested
    @DisplayName("POST /api/v1/orders/placeOrder Tests")
    class PlaceOrderEndpointTests {

        @Test
        @DisplayName("POST /api/v1/orders/placeOrder - Success when user is authenticated")
        void testPlaceOrder_Success() throws Exception {
            authenticateUser("user@example.com");

            when(userService.getUserByEmail("user@example.com")).thenReturn(sampleUser);
            when(orderService.placeOrder(any(CheckoutRequestDTO.class), eq(sampleUser)))
                    .thenReturn(sampleOrderResponseDTO);

            mockMvc.perform(post("/api/v1/orders/placeOrder")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(sampleCheckoutRequestDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.data.orderId").value(101))
                    .andExpect(jsonPath("$.data.status").value("PENDING"))
                    .andExpect(jsonPath("$.data.totalPrice").value(250000))
                    .andExpect(jsonPath("$.data.discount").value(20000))
                    .andExpect(jsonPath("$.data.customerName").value("Nguyen Van A"));

            verify(userService, times(1)).getUserByEmail("user@example.com");
            verify(orderService, times(1)).placeOrder(any(CheckoutRequestDTO.class), eq(sampleUser));
        }

        @Test
        @DisplayName("POST /api/v1/orders/placeOrder - Failure when cart is empty")
        void testPlaceOrder_EmptyCart() throws Exception {
            authenticateUser("user@example.com");

            when(userService.getUserByEmail("user@example.com")).thenReturn(sampleUser);
            when(orderService.placeOrder(any(CheckoutRequestDTO.class), eq(sampleUser)))
                    .thenThrow(new CommonException("No available items to place order"));

            mockMvc.perform(post("/api/v1/orders/placeOrder")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(sampleCheckoutRequestDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.message").value("No available items to place order"));

            verify(orderService, times(1)).placeOrder(any(CheckoutRequestDTO.class), eq(sampleUser));
        }

        @Test
        @DisplayName("POST /api/v1/orders/placeOrder - Failure when voucher is invalid or expired")
        void testPlaceOrder_InvalidVoucher() throws Exception {
            authenticateUser("user@example.com");

            when(userService.getUserByEmail("user@example.com")).thenReturn(sampleUser);
            when(orderService.placeOrder(any(CheckoutRequestDTO.class), eq(sampleUser)))
                    .thenThrow(new CommonException("Voucher không hợp lệ hoặc đã hết hạn"));

            mockMvc.perform(post("/api/v1/orders/placeOrder")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(sampleCheckoutRequestDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.message").value("Voucher không hợp lệ hoặc đã hết hạn"));

            verify(orderService, times(1)).placeOrder(any(CheckoutRequestDTO.class), eq(sampleUser));
        }

        @Test
        @DisplayName("POST /api/v1/orders/placeOrder - Failure when cart items do not belong to user")
        void testPlaceOrder_CartDetailOwnershipFailure() throws Exception {
            authenticateUser("user@example.com");

            when(userService.getUserByEmail("user@example.com")).thenReturn(sampleUser);
            when(orderService.placeOrder(any(CheckoutRequestDTO.class), eq(sampleUser)))
                    .thenThrow(new CommonException("CartDetail does not belong to the current user"));

            mockMvc.perform(post("/api/v1/orders/placeOrder")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(sampleCheckoutRequestDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.message").value("CartDetail does not belong to the current user"));

            verify(orderService, times(1)).placeOrder(any(CheckoutRequestDTO.class), eq(sampleUser));
        }

        @Test
        @DisplayName("POST /api/v1/orders/placeOrder - Failure when user is unauthenticated or not found")
        void testPlaceOrder_UserNotFound() throws Exception {
            SecurityContextHolder.clearContext();

            when(userService.getUserByEmail("")).thenThrow(new CommonException("User không tồn tại"));

            mockMvc.perform(post("/api/v1/orders/placeOrder")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(sampleCheckoutRequestDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.message").value("User không tồn tại"));

            verify(userService, times(1)).getUserByEmail("");
        }
    }
}
