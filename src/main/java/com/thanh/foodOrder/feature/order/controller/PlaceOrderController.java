package com.thanh.foodorder.feature.order.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import com.thanh.foodorder.core.util.JwtUtil;
import com.thanh.foodorder.feature.order.dto.CheckoutRequestDTO;
import com.thanh.foodorder.feature.order.dto.CheckOutResponseDTO;
import com.thanh.foodorder.feature.order.dto.OrderResponseDTO;
import com.thanh.foodorder.feature.order.service.EmailService;
import com.thanh.foodorder.feature.order.service.OrderService;
import com.thanh.foodorder.feature.user.domain.User;
import com.thanh.foodorder.feature.user.service.UserService;

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

    // @PostMapping("orders/checkout")
    // public ResponseEntity<CheckOutResponseDTO> handleCheckout(
    // @RequestBody CheckoutRequestDTO dto) {
    // String email = JwtUtil.getCurrentUserLogin().orElse("");
    // User curUser = this.userService.getUserByEmail(email);

    // return
    // ResponseEntity.status(HttpStatus.OK).body(this.orderService.handleCheckOut(dto,
    // curUser));
    // }

    @PostMapping("/orders/placeOrder")
    public ResponseEntity<OrderResponseDTO> handlePlaceOrder(@RequestBody CheckoutRequestDTO dto) {
        String email = JwtUtil.getCurrentUserLogin().orElse("");
        User curUser = this.userService.getUserByEmail(email);
        return ResponseEntity.status(HttpStatus.OK).body(this.orderService.placeOrder(dto, curUser));
    }

}
