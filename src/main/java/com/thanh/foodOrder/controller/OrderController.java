package com.thanh.foodorder.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.thanh.foodorder.domain.Order;
import com.thanh.foodorder.dto.request.CreatePaymentLinkRequestBody;
import com.thanh.foodorder.dto.request.CreatePaymentRequest;
import com.thanh.foodorder.dto.request.PaymentConfirmRequest;
import com.thanh.foodorder.dto.response.order.AdminOrderResponseDTO;
import com.thanh.foodorder.dto.response.order.OrderHistoryDTO;
import com.thanh.foodorder.dto.response.order.OrderResponseDTO;
import com.thanh.foodorder.enums.OrderStatus;
import com.thanh.foodorder.enums.PaymentStatus;
import com.thanh.foodorder.service.OrderService;
import com.thanh.foodorder.util.annotation.ApiMessage;
import com.thanh.foodorder.util.exception.CommonException;

import io.swagger.v3.oas.models.responses.ApiResponse;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;
import vn.payos.model.webhooks.ConfirmWebhookResponse;
import vn.payos.model.webhooks.WebhookData;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/api/v1")
public class OrderController {
    private final OrderService orderService;
    private final PayOS payOS;

    public OrderController(OrderService orderService, PayOS payOS) {
        this.orderService = orderService;
        this.payOS = payOS;
    }

    @GetMapping("/orders")
    public ResponseEntity<List<AdminOrderResponseDTO>> handleGetAllOrders(
            @RequestParam(name = "status", required = false) OrderStatus orderStatus) {

        return ResponseEntity.status(HttpStatus.OK).body(this.orderService.getAllOrder(orderStatus));
    }

    @GetMapping("/orderDetails/{id}")
    public ResponseEntity<OrderResponseDTO> handleGetOrderDetail(@PathVariable("id") Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(this.orderService.getOrderDetail(id));
    }

    @PutMapping("/orders")
    public ResponseEntity<OrderResponseDTO> handleUpdateOrder(@RequestBody Order order) {
        // TODO: process PUT request

        return ResponseEntity.status(HttpStatus.OK).body(this.orderService.updateOrder(order));
    }

    @GetMapping("/orderHistory")
    public ResponseEntity<OrderHistoryDTO> getOrderHistory() {
        OrderHistoryDTO lst = this.orderService.getOrderHistoryByUser();

        return ResponseEntity.status(HttpStatus.OK).body(lst);
    }

    @PostMapping("/create-payment-link")
    public ResponseEntity<CreatePaymentLinkResponse> handleCreatePaymetLink(
            @RequestBody CreatePaymentRequest requestBody) {
        try {
            Order order = this.orderService.getOrderById(requestBody.getOrderId());

            // Tạo orderCode cho PayOS

            long orderCode = System.currentTimeMillis() / 1000;

            PaymentLinkItem item = PaymentLinkItem.builder().name("Order #" + order.getId()).quantity(1)
                    .price(order.getTotalPrice().longValue())
                    .build();

            CreatePaymentLinkRequest paymentData = CreatePaymentLinkRequest.builder()
                    .orderCode(orderCode)
                    .description("Order #" + order.getId())
                    .amount(order.getTotalPrice().longValue())
                    .item(item)
                    .returnUrl("http://localhost:5173/payment/success")
                    .cancelUrl("http://localhost:5173/payment/cancel")
                    .expiredAt(Instant.now()
                            .plus(30, ChronoUnit.MINUTES)
                            .getEpochSecond())
                    .build();
            CreatePaymentLinkResponse data = payOS.paymentRequests().create(paymentData);

            this.orderService.handleCreatePaymetLink(order, orderCode, data);

            return ResponseEntity.status(HttpStatus.OK).body(data);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);

        }

    }

    @PostMapping("/payment-requests/{id}/cancel")
    public ResponseEntity<Void> handleCancelPayment(@PathVariable("id") Long id) {

        this.orderService.handelCancelPayment(id);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/confirm-webhook")
    public ResponseEntity<ConfirmWebhookResponse> confirmWebhook(
            @RequestBody Map<String, String> requestBody) {
        try {
            ConfirmWebhookResponse result = payOS.webhooks().confirm(requestBody.get("webhookUrl"));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    // if want to check webhook URL then enable this code
    // @PostMapping("/payos_transfer_handler")
    // public ResponseEntity<Object> payosTransferHandler(
    // @RequestBody Object body) {

    // try {
    // // 1. Verify webhook từ PayOS
    // WebhookData data = payOS.webhooks().verify(body);

    // return ResponseEntity.ok().body(data);

    // } catch (CommonException e) {

    // return ResponseEntity
    // .badRequest()
    // .body(e.getMessage());

    // } catch (Exception e) {

    // e.printStackTrace();

    // return ResponseEntity
    // .badRequest()
    // .body("Invalid webhook");
    // }
    // }

    @PostMapping("/payos_transfer_handler")
    public ResponseEntity<Object> payosTransferHandler(
            @RequestBody Object body) {

        try {
            // 1. Verify webhook từ PayOS
            WebhookData data = payOS.webhooks().verify(body);

            // 2. Lấy orderCode
            Long orderCode = data.getOrderCode();

            // 3. Gọi service xử lý payment
            orderService.handlePayment(orderCode, data);

            return ResponseEntity.ok().build();

        } catch (CommonException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .badRequest()
                    .body("Invalid webhook");
        }
    }

}
