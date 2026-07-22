package com.thanh.foodorder.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thanh.foodorder.domain.Order;
import com.thanh.foodorder.dto.request.PaymentConfirmRequest;
import com.thanh.foodorder.dto.response.order.AdminOrderResponseDTO;
import com.thanh.foodorder.dto.response.order.OrderHistoryDTO;
import com.thanh.foodorder.dto.response.order.OrderResponseDTO;
import com.thanh.foodorder.enums.OrderStatus;
import com.thanh.foodorder.service.OrderService;
import com.thanh.foodorder.util.annotation.ApiMessage;

import java.util.List;

import org.springframework.http.HttpStatus;
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

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<AdminOrderResponseDTO> handleGetOrder(@PathVariable("id") Long id) {

        return ResponseEntity.status(HttpStatus.OK).body(this.orderService.getResponseOrderById(id));

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

    @PostMapping("/orders/pay")
    @ApiMessage("Payment order successfully")
    public ResponseEntity<PaymentConfirmRequest> handlePaymentOrder(@RequestBody PaymentConfirmRequest req) {

        this.orderService.payOrder(req.getOrderId(), req.getAmount());

        return ResponseEntity.status(HttpStatus.OK).body(null);
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

}
