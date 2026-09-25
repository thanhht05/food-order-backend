package com.thanh.foodorder.feature.order.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thanh.foodorder.core.util.exception.CommonException;
import com.thanh.foodorder.feature.order.domain.Order;
import com.thanh.foodorder.feature.order.dto.AdminOrderResponseDTO;
import com.thanh.foodorder.feature.order.dto.CreatePaymentRequest;
import com.thanh.foodorder.feature.order.dto.OrderHistoryDTO;
import com.thanh.foodorder.feature.order.dto.OrderResponseDTO;
import com.thanh.foodorder.feature.order.enums.OrderStatus;
import com.thanh.foodorder.feature.order.enums.PaymentStatus;
import com.thanh.foodorder.feature.order.service.OrderService;
import com.thanh.foodorder.feature.user.domain.Address;
import com.thanh.foodorder.feature.user.domain.User;

import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.webhooks.ConfirmWebhookResponse;
import vn.payos.model.webhooks.WebhookData;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean(answers = Answers.RETURNS_DEEP_STUBS)
    private PayOS payOS;

    private Order sampleOrder;
    private OrderResponseDTO sampleOrderResponseDTO;
    private AdminOrderResponseDTO sampleAdminOrderResponseDTO;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(1L);
        user.setFullName("Nguyen Van A");
        user.setEmail("user@example.com");

        Address address = new Address();
        address.setRecipientName("Nguyen Van A");
        address.setPhone("0987654321");
        address.setAddressDetail("123 Duong Le Loi");
        address.setProvince("Ha Noi");
        address.setWard("Ben Nghe");

        sampleOrder = new Order();
        sampleOrder.setId(100L);
        sampleOrder.setOrderDate(LocalDateTime.now());
        sampleOrder.setTotalPrice(BigDecimal.valueOf(150000));
        sampleOrder.setDiscount(BigDecimal.ZERO);
        sampleOrder.setOrderStatus(OrderStatus.PENDING);
        sampleOrder.setPaymentStatus(PaymentStatus.UNPAID);
        sampleOrder.setPaymentMethod("PAYOS");
        sampleOrder.setUser(user);
        sampleOrder.setAddress(address);

        sampleOrderResponseDTO = OrderResponseDTO.builder()
                .orderId(100L)
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.PENDING.name())
                .totalPrice(BigDecimal.valueOf(150000))
                .discount(BigDecimal.ZERO)
                .paymentStatus(PaymentStatus.UNPAID)
                .customerName("Nguyen Van A")
                .email("user@example.com")
                .address(address)
                .items(new ArrayList<>())
                .build();

        sampleAdminOrderResponseDTO = AdminOrderResponseDTO.builder()
                .orderId(100L)
                .orderDate(LocalDateTime.now())
                .totalPrice(BigDecimal.valueOf(150000))
                .status(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.UNPAID)
                .innerUserOrder(AdminOrderResponseDTO.InnerUserOrder.builder()
                        .fullName("Nguyen Van A")
                        .email("user@example.com")
                        .address(address)
                        .build())
                .build();
    }

    // ==========================================
    // 1. GET /api/v1/orders
    // ==========================================
    @Nested
    @DisplayName("GET /api/v1/orders Tests")
    class GetAllOrdersTests {

        @Test
        @DisplayName("GET /api/v1/orders - Success with status filter")
        void testGetAllOrders_WithStatusFilter() throws Exception {
            when(orderService.getAllOrder(OrderStatus.PENDING))
                    .thenReturn(List.of(sampleAdminOrderResponseDTO));

            mockMvc.perform(get("/api/v1/orders")
                    .param("status", "PENDING"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.data[0].orderId").value(100))
                    .andExpect(jsonPath("$.data[0].status").value("PENDING"))
                    .andExpect(jsonPath("$.data[0].innerUserOrder.fullName").value("Nguyen Van A"));

            verify(orderService, times(1)).getAllOrder(OrderStatus.PENDING);
        }

        @Test
        @DisplayName("GET /api/v1/orders - Success without status filter (all orders)")
        void testGetAllOrders_WithoutStatusFilter() throws Exception {
            when(orderService.getAllOrder(null))
                    .thenReturn(List.of(sampleAdminOrderResponseDTO));

            mockMvc.perform(get("/api/v1/orders"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.data[0].orderId").value(100))
                    .andExpect(jsonPath("$.data[0].totalPrice").value(150000));

            verify(orderService, times(1)).getAllOrder(null);
        }

        @Test
        @DisplayName("GET /api/v1/orders - Success with empty result list")
        void testGetAllOrders_EmptyList() throws Exception {
            when(orderService.getAllOrder(OrderStatus.CANCELLED))
                    .thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/v1/orders")
                    .param("status", "CANCELLED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data").isEmpty());

            verify(orderService, times(1)).getAllOrder(OrderStatus.CANCELLED);
        }
    }

    // ==========================================
    // 2. GET /api/v1/orderDetails/{id}
    // ==========================================
    @Nested
    @DisplayName("GET /api/v1/orderDetails/{id} Tests")
    class GetOrderDetailTests {

        @Test
        @DisplayName("GET /api/v1/orderDetails/{id} - Success")
        void testGetOrderDetail_Success() throws Exception {
            when(orderService.getOrderDetail(100L)).thenReturn(sampleOrderResponseDTO);

            mockMvc.perform(get("/api/v1/orderDetails/{id}", 100L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.data.orderId").value(100))
                    .andExpect(jsonPath("$.data.status").value("PENDING"))
                    .andExpect(jsonPath("$.data.customerName").value("Nguyen Van A"));

            verify(orderService, times(1)).getOrderDetail(100L);
        }

        @Test
        @DisplayName("GET /api/v1/orderDetails/{id} - Failure when order not found")
        void testGetOrderDetail_NotFound() throws Exception {
            when(orderService.getOrderDetail(999L))
                    .thenThrow(new CommonException("Order not found"));

            mockMvc.perform(get("/api/v1/orderDetails/{id}", 999L))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.message").value("Order not found"));

            verify(orderService, times(1)).getOrderDetail(999L);
        }
    }

    // ==========================================
    // 3. PUT /api/v1/orders
    // ==========================================
    @Nested
    @DisplayName("PUT /api/v1/orders Tests")
    class UpdateOrderTests {

        @Test
        @DisplayName("PUT /api/v1/orders - Success")
        void testUpdateOrder_Success() throws Exception {
            sampleOrder.setOrderStatus(OrderStatus.CONFIRMED);
            sampleOrderResponseDTO.setStatus(OrderStatus.CONFIRMED.name());

            when(orderService.updateOrder(any(Order.class))).thenReturn(sampleOrderResponseDTO);

            mockMvc.perform(put("/api/v1/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(sampleOrder)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.data.orderId").value(100))
                    .andExpect(jsonPath("$.data.status").value("CONFIRMED"));

            verify(orderService, times(1)).updateOrder(any(Order.class));
        }

        @Test
        @DisplayName("PUT /api/v1/orders - Failure when order does not exist")
        void testUpdateOrder_NotFound() throws Exception {
            when(orderService.updateOrder(any(Order.class)))
                    .thenThrow(new CommonException("Order is not exsit"));

            mockMvc.perform(put("/api/v1/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(sampleOrder)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.message").value("Order is not exsit"));

            verify(orderService, times(1)).updateOrder(any(Order.class));
        }
    }

    // ==========================================
    // 4. GET /api/v1/orderHistory
    // ==========================================
    @Nested
    @DisplayName("GET /api/v1/orderHistory Tests")
    class GetOrderHistoryTests {

        @Test
        @DisplayName("GET /api/v1/orderHistory - Success")
        void testGetOrderHistory_Success() throws Exception {
            OrderHistoryDTO historyDTO = new OrderHistoryDTO();
            historyDTO.setUserId(1L);
            historyDTO.setFullName("Nguyen Van A");
            historyDTO.setCartId(10L);
            historyDTO.setOrderInfo(new ArrayList<>());

            when(orderService.getOrderHistoryByUser()).thenReturn(historyDTO);

            mockMvc.perform(get("/api/v1/orderHistory"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.data.userId").value(1))
                    .andExpect(jsonPath("$.data.fullName").value("Nguyen Van A"))
                    .andExpect(jsonPath("$.data.cartId").value(10));

            verify(orderService, times(1)).getOrderHistoryByUser();
        }
    }

    // ==========================================
    // 5. POST /api/v1/create-payment-link
    // ==========================================
    @Nested
    @DisplayName("POST /api/v1/create-payment-link Tests")
    class CreatePaymentLinkTests {

        @Test
        @DisplayName("POST /api/v1/create-payment-link - Success")
        void testCreatePaymentLink_Success() throws Exception {
            CreatePaymentRequest request = new CreatePaymentRequest();
            request.setOrderId(100L);

            CreatePaymentLinkResponse paymentResponse = new CreatePaymentLinkResponse();
            paymentResponse.setCheckoutUrl("https://payos.vn/checkout-url");
            paymentResponse.setPaymentLinkId("payos-link-100");

            when(orderService.getOrderById(100L)).thenReturn(sampleOrder);
            when(payOS.paymentRequests().create(any())).thenReturn(paymentResponse);
            doNothing().when(orderService).handleCreatePaymetLink(eq(sampleOrder), anyLong(), eq(paymentResponse));

            mockMvc.perform(post("/api/v1/create-payment-link")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.data.checkoutUrl").value("https://payos.vn/checkout-url"))
                    .andExpect(jsonPath("$.data.paymentLinkId").value("payos-link-100"));

            verify(orderService, times(1)).getOrderById(100L);
            verify(payOS.paymentRequests(), times(1)).create(any());
            verify(orderService, times(1)).handleCreatePaymetLink(eq(sampleOrder), anyLong(), eq(paymentResponse));
        }

        @Test
        @DisplayName("POST /api/v1/create-payment-link - Failure when exception is thrown")
        void testCreatePaymentLink_Failure() throws Exception {
            CreatePaymentRequest request = new CreatePaymentRequest();
            request.setOrderId(999L);

            when(orderService.getOrderById(999L)).thenThrow(new RuntimeException("Order not found"));

            mockMvc.perform(post("/api/v1/create-payment-link")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(orderService, times(1)).getOrderById(999L);
        }
    }

    // ==========================================
    // 6. POST /api/v1/payment-requests/{id}/cancel
    // ==========================================
    @Nested
    @DisplayName("POST /api/v1/payment-requests/{id}/cancel Tests")
    class CancelPaymentTests {

        @Test
        @DisplayName("POST /api/v1/payment-requests/{id}/cancel - Success")
        void testCancelPayment_Success() throws Exception {
            doNothing().when(orderService).handelCancelPayment(100L);

            mockMvc.perform(post("/api/v1/payment-requests/{id}/cancel", 100L))
                    .andExpect(status().isOk());

            verify(orderService, times(1)).handelCancelPayment(100L);
        }

        @Test
        @DisplayName("POST /api/v1/payment-requests/{id}/cancel - Failure when cancel throws error")
        void testCancelPayment_Failure() throws Exception {
            doThrow(new CommonException("Cannot cancel payment"))
                    .when(orderService).handelCancelPayment(999L);

            mockMvc.perform(post("/api/v1/payment-requests/{id}/cancel", 999L))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.message").value("Cannot cancel payment"));

            verify(orderService, times(1)).handelCancelPayment(999L);
        }
    }

    // ==========================================
    // 7. POST /api/v1/confirm-webhook
    // ==========================================
    @Nested
    @DisplayName("POST /api/v1/confirm-webhook Tests")
    class ConfirmWebhookTests {

        @Test
        @DisplayName("POST /api/v1/confirm-webhook - Success")
        void testConfirmWebhook_Success() throws Exception {
            Map<String, String> requestBody = Map.of("webhookUrl", "https://api.example.com/webhook");
            ConfirmWebhookResponse mockResponse = mock(ConfirmWebhookResponse.class);

            when(payOS.webhooks().confirm("https://api.example.com/webhook")).thenReturn(mockResponse);

            mockMvc.perform(post("/api/v1/confirm-webhook")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(status().isOk());

            verify(payOS.webhooks(), times(1)).confirm("https://api.example.com/webhook");
        }

        @Test
        @DisplayName("POST /api/v1/confirm-webhook - Failure when PayOS throws exception")
        void testConfirmWebhook_Failure() throws Exception {
            Map<String, String> requestBody = Map.of("webhookUrl", "https://invalid-url.com");

            when(payOS.webhooks().confirm(anyString())).thenThrow(new RuntimeException("Webhook error"));

            mockMvc.perform(post("/api/v1/confirm-webhook")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(status().isBadRequest());

            verify(payOS.webhooks(), times(1)).confirm(anyString());
        }
    }

    // ==========================================
    // 8. POST /api/v1/payos_transfer_handler
    // ==========================================
    @Nested
    @DisplayName("POST /api/v1/payos_transfer_handler Tests")
    class PayosTransferHandlerTests {

        @Test
        @DisplayName("POST /api/v1/payos_transfer_handler - Success")
        void testPayosTransferHandler_Success() throws Exception {
            Map<String, Object> webhookBody = Map.of("code", "00", "desc", "success");
            WebhookData data = new WebhookData();
            data.setOrderCode(123456L);
            data.setAmount(150000L);

            when(payOS.webhooks().verify(any())).thenReturn(data);
            doNothing().when(orderService).handlePayment(eq(123456L), eq(data));

            mockMvc.perform(post("/api/v1/payos_transfer_handler")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(webhookBody)))
                    .andExpect(status().isOk());

            verify(payOS.webhooks(), times(1)).verify(any());
            verify(orderService, times(1)).handlePayment(eq(123456L), eq(data));
        }

        @Test
        @DisplayName("POST /api/v1/payos_transfer_handler - Failure on CommonException (e.g. order already paid)")
        void testPayosTransferHandler_CommonException() throws Exception {
            Map<String, Object> webhookBody = Map.of("code", "00");
            WebhookData data = new WebhookData();
            data.setOrderCode(123456L);

            when(payOS.webhooks().verify(any())).thenReturn(data);
            doThrow(new CommonException("Order already paid"))
                    .when(orderService).handlePayment(eq(123456L), eq(data));

            mockMvc.perform(post("/api/v1/payos_transfer_handler")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(webhookBody)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Order already paid"));

            verify(payOS.webhooks(), times(1)).verify(any());
            verify(orderService, times(1)).handlePayment(eq(123456L), eq(data));
        }

        @Test
        @DisplayName("POST /api/v1/payos_transfer_handler - Failure on Invalid Webhook exception")
        void testPayosTransferHandler_InvalidWebhookException() throws Exception {
            Map<String, Object> webhookBody = Map.of("corrupted", "payload");

            when(payOS.webhooks().verify(any())).thenThrow(new RuntimeException("Signature verification failed"));

            mockMvc.perform(post("/api/v1/payos_transfer_handler")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(webhookBody)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Invalid webhook"));

            verify(payOS.webhooks(), times(1)).verify(any());
        }
    }
}
