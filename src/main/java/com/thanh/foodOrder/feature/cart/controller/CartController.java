package com.thanh.foodorder.feature.cart.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;
import com.thanh.foodorder.feature.cart.dto.AddToCartResponseDTO;
import com.thanh.foodorder.feature.cart.dto.CartRequestDTO;
import com.thanh.foodorder.feature.cart.dto.CartResponeDTO;
import com.thanh.foodorder.feature.cart.dto.MergeCartRequest;
import com.thanh.foodorder.feature.cart.service.CartService;
import com.thanh.foodorder.feature.user.domain.User;
import com.thanh.foodorder.feature.user.service.UserService;

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
    public ResponseEntity<CartResponeDTO> addProcutToCart(@RequestBody CartRequestDTO request) {

        return ResponseEntity.status(HttpStatus.OK).body(cartService.addProductsToCart(request));
    }

    @DeleteMapping("/cartDetail/product/{id}")
    public ResponseEntity<AddToCartResponseDTO> handleDeleteCartDetail(@PathVariable("id") Long id) {

        return ResponseEntity.status(HttpStatus.OK).body(this.cartService.removeProductFromCart(id));
    }

    // @GetMapping("/cartDetails")
    // public ResponseEntity<CartDetailsResponseDTO> getAllCarts() {
    // CartDetailsResponseDTO res = this.cartService.getAllCartDetail();
    // return ResponseEntity.status(HttpStatus.OK).body(res);
    // }

    @PutMapping("/cartDetails")
    public ResponseEntity<CartResponeDTO> handeUpdateCartDetail(@RequestBody CartRequestDTO req) {

        return ResponseEntity.status(HttpStatus.OK).body(this.cartService.updateCartItem(req));
    }

    // @PostMapping("/cartMerge")
    // public ResponseEntity<?> mergeCart(
    // @RequestBody MergeCartRequest request,
    // @AuthenticationPrincipal UserDetails userdDetail) {
    // User user = this.userService.getUserByEmail(userdDetail.getUsername());
    // return ResponseEntity.ok(cartService.mergeCart(user.getId(), request));
    // }

    @GetMapping("/cartDetailUser")
    public ResponseEntity<CartResponeDTO> handleGetCartDetailByUser() {
        return ResponseEntity.ok(this.cartService.getCartDetailsByUser());
    }

}
