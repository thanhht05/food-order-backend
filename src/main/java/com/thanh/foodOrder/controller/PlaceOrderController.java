package com.thanh.foodorder.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thanh.foodorder.domain.Order;
import com.thanh.foodorder.domain.User;
import com.thanh.foodorder.dto.request.BuyNowRequestDTO;
import com.thanh.foodorder.dto.request.CheckoutRequestDTO;
import com.thanh.foodorder.dto.response.CheckOutResponseDTO;
import com.thanh.foodorder.dto.response.order.OrderResponseDTO;
import com.thanh.foodorder.service.EmailService;
import com.thanh.foodorder.service.OrderService;
import com.thanh.foodorder.service.UserService;
import com.thanh.foodorder.util.JwtUtil;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/v1")
public class PlaceOrderController {
    private final OrderService orderService;
    private final UserService userService;
    private EmailService emailService;

    public PlaceOrderController(OrderService odOrderService, UserService userService, EmailService emailService) {
        this.orderService = odOrderService;
        this.userService = userService;
        this.emailService = emailService;
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

    @PostMapping("orders/buy-now")
    public ResponseEntity<OrderResponseDTO> handleBuyNow(@RequestBody BuyNowRequestDTO dto) {
        String email = JwtUtil.getCurrentUserLogin().orElse("");
        User curUser = this.userService.getUserByEmail(email);

        return ResponseEntity.status(HttpStatus.OK).body(this.orderService.handleBuyNow(dto, curUser));
    }

}
