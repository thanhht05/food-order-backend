package com.thanh.foodOrder.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thanh.foodOrder.domain.CartDetail;
import com.thanh.foodOrder.domain.User;
import com.thanh.foodOrder.dtos.request.CartRequestDTO;
import com.thanh.foodOrder.dtos.request.MergeCartRequest;
import com.thanh.foodOrder.dtos.response.AddToCartResponseDTO;
import com.thanh.foodOrder.dtos.response.CartDetailsResponseDTO;
import com.thanh.foodOrder.service.CartService;
import com.thanh.foodOrder.service.UserService;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/v1")
public class CartController {
    private final CartService cartService;
    private final UserService userService;

    public CartController(CartService cartService, UserService userService) {
        this.cartService = cartService;
        this.userService = userService;
    }

    @PostMapping("/carts")
    public ResponseEntity<CartDetailsResponseDTO> addProcutToCart(@RequestBody CartRequestDTO request) {

        return ResponseEntity.status(HttpStatus.OK).body(cartService.addProductsToCart(request));
    }

    @DeleteMapping("/cartDetail/product/{id}")
    public ResponseEntity<AddToCartResponseDTO> handleDeleteCartDetail(@PathVariable("id") Long id) {

        return ResponseEntity.status(HttpStatus.OK).body(this.cartService.removeProductFromCart(id));
    }

    @GetMapping("/cartDetails")
    public ResponseEntity<CartDetailsResponseDTO> getAllCarts() {
        CartDetailsResponseDTO res = this.cartService.getAllCartDetail();
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @PostMapping("/cartMerge")
    public ResponseEntity<?> mergeCart(
            @RequestBody MergeCartRequest request,
            @AuthenticationPrincipal UserDetails userdDetail) {
        User user = this.userService.getUserByEmail(userdDetail.getUsername());
        return ResponseEntity.ok(cartService.mergeCart(user.getId(), request));
    }

}
