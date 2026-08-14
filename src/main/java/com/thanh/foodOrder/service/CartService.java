package com.thanh.foodorder.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thanh.foodorder.domain.Cart;
import com.thanh.foodorder.domain.CartDetail;
import com.thanh.foodorder.domain.CartItemDetail;
import com.thanh.foodorder.domain.Product;
import com.thanh.foodorder.domain.User;
import com.thanh.foodorder.dto.request.CartItemRequestDTO;
import com.thanh.foodorder.dto.request.CartRequestDTO;
import com.thanh.foodorder.dto.request.MergeCartRequest;
import com.thanh.foodorder.dto.response.cart.AddToCartResponseDTO;
import com.thanh.foodorder.dto.response.cart.CartDetailUserDTO;
import com.thanh.foodorder.dto.response.cart.CartResponeDTO;
import com.thanh.foodorder.repository.CartDetailRepository;
import com.thanh.foodorder.repository.CartRepository;
import com.thanh.foodorder.util.JwtUtil;
import com.thanh.foodorder.util.exception.CommonException;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class CartService {
    private final RoleService roleService;
    private final CartRepository cartRepository;
    private final UserService userService;
    private final CartDetailRepository cartDetailRepository;
    private final ProductService productService;

    public CartService(CartRepository cartRepository,
            UserService userService,
            ProductService productService, CartDetailRepository cartDetailRepository, RoleService roleService) {
        this.cartRepository = cartRepository;
        this.productService = productService;
        this.userService = userService;
        this.cartDetailRepository = cartDetailRepository;
        this.roleService = roleService;
    }

    @Transactional
    public CartResponeDTO addProductsToCart(CartRequestDTO request) {

        String email = JwtUtil.getCurrentUserLogin().orElseThrow();
        User user = userService.getUserByEmail(email);
        // 1. Lấy cart
        Cart cart = user.getCart();
        if (cart == null) {
            cart = new Cart();
            cart.setUser(user);
            cart.setCartDetails(new ArrayList<>());
        }

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
        return mapToResponse(cart);
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

    public BigDecimal getTotalPrice(Long userId) {
        Cart cart = cartRepository.findByUserId(userId);

        if (cart == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal total = BigDecimal.ZERO;
        List<CartDetail> items = cart.getCartDetails();

        for (CartDetail item : items) {
            BigDecimal amount = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(amount);
        }

        return total;
    }

    // public CartDetailsResponseDTO mergeCart(Long userId, MergeCartRequest
    // request) {

    // // 1. Lấy user + cart
    // User user = userService.getUserById(userId);

    // Cart cart = cartRepository.findByUserId(userId);
    // if (cart == null) {
    // cart = new Cart();
    // cart.setUser(user);
    // }

    // // 2. Map cart hiện tại
    // Map<Long, CartDetail> dbMap = new HashMap<>();
    // for (CartDetail cd : cart.getCartDetails()) {
    // dbMap.put(cd.getProduct().getId(), cd);
    // }

    // // 3. Merge
    // for (CartItemRequestDTO reqItem : request.getItems()) {

    // Long productId = reqItem.getProductId();
    // int quantity = reqItem.getQuantity();

    // // ❗ validate
    // if (quantity <= 0)
    // continue;

    // if (dbMap.containsKey(productId)) {
    // // 👉 đã có → cộng
    // CartDetail existing = dbMap.get(productId);
    // existing.setQuantity(existing.getQuantity() + quantity);

    // } else {
    // // 👉 chưa có → thêm mới
    // Product product = productService.getProductById(productId);

    // CartDetail newItem = new CartDetail();
    // newItem.setCart(cart);
    // newItem.setProduct(product);
    // newItem.setQuantity(quantity);
    // newItem.setPrice(product.getPrice());

    // cart.getCartDetails().add(newItem); // 🔥 quan trọng
    // }
    // }

    // // 4. Save 1 lần duy nhất
    // cartRepository.save(cart);

    // // 5. Build response
    // return mapToResponse(cart);
    // }

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

    public CartResponeDTO updateCartItem(CartRequestDTO request) {

        String email = JwtUtil.getCurrentUserLogin().orElseThrow();
        User user = userService.getUserByEmail(email);

        Cart cart = user.getCart();
        Product product = productService.getProductById(request.getProductId());

        CartDetail cartItem = cartDetailRepository.findByCartAndProductId(cart, product.getId());
        // 👉 CASE 1: quantity = 0 → delete
        if (request.getQuantity() <= 0) {
            this.cartDetailRepository.delete(cartItem);
            return mapToResponse(cart);

        }
        if (cartItem != null) {

            cartItem.setQuantity(request.getQuantity());
            cartDetailRepository.save(cartItem);

        }
        // chưa có → add mới
        else {
            CartDetail newItem = new CartDetail();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(request.getQuantity());
            newItem.setPrice(product.getPrice());

            cartDetailRepository.save(newItem);
        }
        return mapToResponse(cart);
    }

    public CartResponeDTO getCartDetailsByUser() {
        String email = JwtUtil.getCurrentUserLogin().orElseThrow();
        User user = userService.getUserByEmail(email);

        Cart cart = user.getCart();
        if (cart == null) {
            CartResponeDTO response = new CartResponeDTO();
            response.setLst(new ArrayList<>());
            response.setTotalQuantity(0);
            response.setTotalPrice(BigDecimal.ZERO);

            return response;
        }
        List<CartDetail> cartDetails = cart.getCartDetails();

        List<CartDetailUserDTO> lst = new ArrayList<>();
        int totalQuantity = 0;
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (CartDetail cd : cartDetails) {
            CartDetailUserDTO dto = new CartDetailUserDTO();
            dto.setCartDetailId(cd.getId());
            dto.setQuantity(cd.getQuantity());

            CartDetailUserDTO.ProductInnerCartDetail p = new CartDetailUserDTO.ProductInnerCartDetail();
            Product product = cd.getProduct();
            p.setId(product.getId());
            p.setName(product.getName());
            p.setCategoryName(product.getCategory().getName());
            p.setImg(product.getLstImg().get(0).getImgName());
            p.setPrice(product.getPrice());
            p.setQuantity(product.getQuantity());

            dto.setProductsInnerCartDetail(p);
            lst.add(dto);

            // Tổng quantity
            totalQuantity += cd.getQuantity();

            // price * quantity
            BigDecimal itemTotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(cd.getQuantity()));

            totalPrice = totalPrice.add(itemTotal);
        }
        CartResponeDTO response = new CartResponeDTO();
        response.setLst(lst);
        response.setTotalQuantity(totalQuantity);
        response.setTotalPrice(totalPrice);
        return response;
    }

    private CartResponeDTO mapToResponse(Cart cart) {
        List<CartDetailUserDTO> lst = new ArrayList<>();
        int totalQuantity = 0;
        BigDecimal totalPrice = BigDecimal.ZERO;

        for (CartDetail cd : cart.getCartDetails()) {

            totalQuantity += cd.getQuantity();
            BigDecimal amount = cd.getPrice().multiply(BigDecimal.valueOf(cd.getQuantity()));
            totalPrice = totalPrice.add(amount);

            CartDetailUserDTO dto = new CartDetailUserDTO();
            dto.setCartDetailId(cd.getId());
            dto.setQuantity(cd.getQuantity());

            Product p = cd.getProduct();

            CartDetailUserDTO.ProductInnerCartDetail item = new CartDetailUserDTO.ProductInnerCartDetail();

            item.setId(p.getId());
            item.setName(p.getName());
            item.setPrice(p.getPrice());
            item.setQuantity(cd.getQuantity());
            item.setCategoryName(p.getCategory().getName());
            item.setImg(p.getLstImg().get(0).getImgName());

            // tránh null ảnh
            if (p.getLstImg() != null && !p.getLstImg().isEmpty()) {
                item.setImg(p.getLstImg().get(0).getImgName());
            }

            if (p.getCategory() != null) {
                item.setCategoryName(p.getCategory().getName());
            }
            dto.setProductsInnerCartDetail(item);

            lst.add(dto);

        }
        CartResponeDTO response = new CartResponeDTO();
        response.setLst(lst);
        response.setTotalQuantity(totalQuantity);
        response.setTotalPrice(totalPrice);

        return response;
    }
}
