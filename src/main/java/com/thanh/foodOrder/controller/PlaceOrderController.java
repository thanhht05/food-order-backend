package com.thanh.foodOrder.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thanh.foodOrder.domain.Order;
import com.thanh.foodOrder.domain.User;
import com.thanh.foodOrder.dtos.request.CheckoutRequestDTO;
import com.thanh.foodOrder.dtos.response.CheckOutResponseDTO;
import com.thanh.foodOrder.dtos.response.order.OrderResponseDTO;
import com.thanh.foodOrder.service.OrderService;
import com.thanh.foodOrder.service.UserService;
import com.thanh.foodOrder.util.JwtUtil;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/v1")
public class PlaceOrderController {
    private final OrderService orderService;
    private final UserService userService;

    public PlaceOrderController(OrderService odOrderService, UserService userService) {
        this.orderService = odOrderService;
        this.userService = userService;
    }

    @PostMapping("orders/checkout")
    public ResponseEntity<CheckOutResponseDTO> handleCheckout(
            @RequestBody CheckoutRequestDTO dto) {
        String email = JwtUtil.getCurrentUserLogin().orElse("");
        User curUser = this.userService.getUserByEmail(email);

        return ResponseEntity.status(HttpStatus.OK).body(this.orderService.handleCheckOut(dto, curUser));
    }

    @PostMapping("/orders/placeOrder")
    public ResponseEntity<OrderResponseDTO> handlePlaceOrder(@RequestBody CheckoutRequestDTO dto) {
        String email = JwtUtil.getCurrentUserLogin().orElse("");
        User curUser = this.userService.getUserByEmail(email);
        return ResponseEntity.status(HttpStatus.OK).body(this.orderService.placeOrder(dto, curUser));
    }

    // @GetMapping("orders/updateStatus/{id}")
    // public String handleUpdateOrderStatus(@PathVariable("id") Long id) {
    // //TODO: process POST request

    // return entity;
    // }

}
