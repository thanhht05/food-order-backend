package com.thanh.foodOrder.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thanh.foodOrder.domain.Cart;
import com.thanh.foodOrder.domain.CartDetail;
import com.thanh.foodOrder.domain.CartItemDetail;
import com.thanh.foodOrder.domain.Product;
import com.thanh.foodOrder.domain.User;
import com.thanh.foodOrder.dtos.request.CartItemRequestDTO;
import com.thanh.foodOrder.dtos.request.CartRequestDTO;
import com.thanh.foodOrder.dtos.response.AddToCartResponseDTO;
import com.thanh.foodOrder.dtos.response.CartDetailsResponseDTO;
import com.thanh.foodOrder.repository.CartRepository;
import com.thanh.foodOrder.util.JwtUtil;
import com.thanh.foodOrder.util.exception.CommonException;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class CartService {
    private final CartRepository cartRepository;
    private final UserService userService;
    private final ProductService productService;

    public CartService(CartRepository cartRepository,
            UserService userService,
            ProductService productService) {
        this.cartRepository = cartRepository;
        this.productService = productService;
        this.userService = userService;
    }

    @Transactional
    public AddToCartResponseDTO addProductsToCart(CartRequestDTO request) {

        String email = JwtUtil.getCurrentUserLogin().orElseThrow();
        User user = userService.getUserByEmail(email);
        // 1. Lấy cart
        Cart cart = user.getCart();
        if (cart == null) {
            cart = new Cart();
            cart.setUser(user);
            cart.setCartDetails(new ArrayList<>());
        }
        // 2. Loop qua từng item từ JSON

        Product product = productService.getProductById(request.getProductId());

        // check inventory of product
        this.productService.checkQuantityProductBeforeAddToCart(product,
                request.getQuantity());
        // 3. Check product đã có trong cart chưa
        Optional<CartDetail> existingItem = cart.getCartDetails()
                .stream()
                .filter(cd -> cd.getProduct().getId().equals(product.getId()))
                .findFirst();

        if (existingItem.isPresent()) {
            // 4. Nếu có rồi → cộng thêm quantity
            CartDetail cartDetail = existingItem.get();
            cartDetail.setQuantity(
                    cartDetail.getQuantity() + request.getQuantity());
        } else {
            // 5. Nếu chưa có → tạo mới
            CartDetail cartDetail = new CartDetail();
            cartDetail.setCart(cart);
            cartDetail.setProduct(product);
            cartDetail.setQuantity(request.getQuantity());
            cartDetail.setPrice(product.getPrice());

            cart.getCartDetails().add(cartDetail);

        }
        product.setQuantity(product.getQuantity() - request.getQuantity());

        cartRepository.save(cart);
        AddToCartResponseDTO res = new AddToCartResponseDTO();
        int total = getTotalQuantity(user.getId());
        res.setTotalQuantity(total);
        return res;
    }

    public int getTotalQuantity(Long userId) {
        Cart cart = cartRepository.findByUserId(userId);

        if (cart == null) {
            return 0;
        }

        int total = 0;
        List<CartDetail> items = cart.getCartDetails();

        for (CartDetail item : items) {
            total += item.getQuantity();
        }

        return total;
    }

    public double getTotalPrice(Long userId) {
        Cart cart = cartRepository.findByUserId(userId);

        if (cart == null) {
            return 0;
        }

        double total = 0;
        List<CartDetail> items = cart.getCartDetails();

        for (CartDetail item : items) {
            total += item.getPrice() * item.getQuantity();
        }

        return total;
    }
    // private String cartKey(Long id) {
    // return "cart:user:" + id;
    // }

    // private String productField(Long productId) {
    // return "product:" + productId;
    // }
    // add product to cart with redis

    // @Transactional
    // public void removeProductFromCart(Long productId) {

    // String email = JwtUtil.getCurrentUserLogin().orElseThrow();
    // User user = userService.getUserByEmail(email);

    // Cart cart = user.getCart();

    // cart.getCartDetails().removeIf(
    // cd -> cd.getProduct().getId().equals(productId));

    // cartRepository.save(cart);
    // }

    // public void removeProductFromCart(Long productId) {
    // String email = JwtUtil.getCurrentUserLogin().orElseThrow();
    // User user = this.userService.getUserByEmail(email);
    // String key = "cart:user:" + user.getId();

    // HashOperations<String, String, CartItemDetail> hashOps =
    // redisTemplate.opsForHash();

    // Long deleted = hashOps.delete(key, productField(productId));
    // log.info("Deleted fields = {}", deleted);

    // // Gia hạn TTL nếu cart vẫn còn item
    // if (Boolean.TRUE.equals(redisTemplate.hasKey(key))
    // && hashOps.size(key) > 0) {
    // redisTemplate.expire(key, 30, TimeUnit.MINUTES);
    // }

    // // Nếu cart rỗng → xóa luôn key
    // if (hashOps.size(key) == 0) {
    // redisTemplate.delete(key);
    // }
    // }

    public CartDetailsResponseDTO getAllCartDetail() {
        String email = JwtUtil.getCurrentUserLogin().orElseThrow();
        User user = userService.getUserByEmail(email);

        Cart cart = user.getCart();
        if (cart == null) {
            throw new CommonException("Cart is null");
        }
        List<CartDetail> cartDetails = cart.getCartDetails();

        CartDetailsResponseDTO res = new CartDetailsResponseDTO();
        int total = getTotalQuantity(user.getId());
        double totalPrice = getTotalPrice(user.getId());
        res.setQuantity(total);
        res.setTotalPrice(totalPrice);
        List<CartDetailsResponseDTO.ProductInnerCartDetail> lst = new ArrayList<>();

        for (CartDetail cd : cartDetails) {
            CartDetailsResponseDTO.ProductInnerCartDetail p = new CartDetailsResponseDTO.ProductInnerCartDetail();
            Product prd = cd.getProduct();
            p.setId(prd.getId());
            p.setName(prd.getName());
            p.setLstImg(prd.getLstImg());
            p.setPrice(prd.getPrice());
            p.setCategoryName(prd.getCategory().getName());
            lst.add(p);

        }
        res.setProductsInnerCartDetail(lst);
        return res;

    }

    public AddToCartResponseDTO removeProductFromCart(Long productId) {

        String email = JwtUtil.getCurrentUserLogin().orElseThrow();
        User user = userService.getUserByEmail(email);

        Cart cart = user.getCart();
        if (cart == null) {
            throw new CommonException("Cart not found");
        }
        cart.getCartDetails().removeIf(
                cd -> cd.getProduct().getId().equals(productId));

        cartRepository.save(cart);
        AddToCartResponseDTO res = new AddToCartResponseDTO();
        int total = getTotalQuantity(user.getId());
        res.setTotalQuantity(total);
        return res;
    }

}
