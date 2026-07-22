package com.thanh.foodorder.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
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
import com.thanh.foodorder.dto.response.cart.CartDetailsResponseDTO;
import com.thanh.foodorder.repository.CartDetailRepository;
import com.thanh.foodorder.repository.CartRepository;
import com.thanh.foodorder.util.JwtUtil;
import com.thanh.foodorder.util.exception.CommonException;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class CartService {
    private final CartRepository cartRepository;
    private final UserService userService;
    private final CartDetailRepository cartDetailRepository;
    private final ProductService productService;

    public CartService(CartRepository cartRepository,
            UserService userService,
            ProductService productService, CartDetailRepository cartDetailRepository) {
        this.cartRepository = cartRepository;
        this.productService = productService;
        this.userService = userService;
        this.cartDetailRepository = cartDetailRepository;
    }

    @Transactional
    public CartDetailsResponseDTO addProductsToCart(CartRequestDTO request) {

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

    public CartDetailsResponseDTO mergeCart(Long userId, MergeCartRequest request) {

        // 1. Lấy user + cart
        User user = userService.getUserById(userId);

        Cart cart = cartRepository.findByUserId(userId);
        if (cart == null) {
            cart = new Cart();
            cart.setUser(user);
        }

        // 2. Map cart hiện tại
        Map<Long, CartDetail> dbMap = new HashMap<>();
        for (CartDetail cd : cart.getCartDetails()) {
            dbMap.put(cd.getProduct().getId(), cd);
        }

        // 3. Merge
        for (CartItemRequestDTO reqItem : request.getItems()) {

            Long productId = reqItem.getProductId();
            int quantity = reqItem.getQuantity();

            // ❗ validate
            if (quantity <= 0)
                continue;

            if (dbMap.containsKey(productId)) {
                // 👉 đã có → cộng
                CartDetail existing = dbMap.get(productId);
                existing.setQuantity(existing.getQuantity() + quantity);

            } else {
                // 👉 chưa có → thêm mới
                Product product = productService.getProductById(productId);

                CartDetail newItem = new CartDetail();
                newItem.setCart(cart);
                newItem.setProduct(product);
                newItem.setQuantity(quantity);
                newItem.setPrice(product.getPrice());

                cart.getCartDetails().add(newItem); // 🔥 quan trọng
            }
        }

        // 4. Save 1 lần duy nhất
        cartRepository.save(cart);

        // 5. Build response
        return mapToResponse(cart);
    }

    private CartDetailsResponseDTO mapToResponse(Cart cart) {

        CartDetailsResponseDTO res = new CartDetailsResponseDTO();

        int totalQuantity = 0;
        double totalPrice = 0;

        List<CartDetailsResponseDTO.ProductInnerCartDetail> items = new ArrayList<>();

        for (CartDetail cd : cart.getCartDetails()) {

            Product p = cd.getProduct();

            CartDetailsResponseDTO.ProductInnerCartDetail item = new CartDetailsResponseDTO.ProductInnerCartDetail();

            item.setId(p.getId());
            item.setName(p.getName());
            item.setPrice(p.getPrice());
            item.setQuantity(cd.getQuantity());

            // tránh null ảnh
            if (p.getLstImg() != null && !p.getLstImg().isEmpty()) {
                item.setImg(p.getLstImg().get(0).getImgName());
            }

            if (p.getCategory() != null) {
                item.setCategoryName(p.getCategory().getName());
            }

            items.add(item);

            totalQuantity += cd.getQuantity();
            totalPrice += cd.getQuantity() * cd.getPrice();
        }

        res.setQuantity(totalQuantity);
        res.setTotalPrice(totalPrice);
        res.setProductsInnerCartDetail(items);
        List<Long> cdIds = new ArrayList<>();
        for (CartDetail ids : cart.getCartDetails()) {
            cdIds.add(ids.getId());
        }
        res.setCartDetailId(cdIds);

        return res;
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

    // public CartDetailsResponseDTO getAllCartDetail() {
    // String email = JwtUtil.getCurrentUserLogin().orElseThrow();
    // User user = userService.getUserByEmail(email);

    // Cart cart = user.getCart();
    // if (cart == null) {
    // // Return empty data to avoid FrontEnd errors when calling the getcartDetail
    // API
    // // in case user not have cart-cartdetail
    // return new CartDetailsResponseDTO();
    // }
    // List<CartDetail> cartDetails = cart.getCartDetails();

    // CartDetailsResponseDTO res = new CartDetailsResponseDTO();
    // int total = getTotalQuantity(user.getId());
    // double totalPrice = getTotalPrice(user.getId());
    // res.setQuantity(total);
    // res.setTotalPrice(totalPrice);
    // res.setCartDetailId(total);
    // List<CartDetailsResponseDTO.ProductInnerCartDetail> lst = new ArrayList<>();

    // for (CartDetail cd : cartDetails) {
    // CartDetailsResponseDTO.ProductInnerCartDetail p = new
    // CartDetailsResponseDTO.ProductInnerCartDetail();
    // Product prd = cd.getProduct();
    // p.setId(prd.getId());
    // p.setQuantity(cd.getQuantity());
    // p.setName(prd.getName());
    // p.setImg(prd.getLstImg().get(0).getImgName());
    // p.setPrice(prd.getPrice());
    // p.setCategoryName(prd.getCategory().getName());
    // lst.add(p);

    // }
    // res.setProductsInnerCartDetail(lst);
    // return res;

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

    public CartDetailsResponseDTO updateCartItem(CartRequestDTO request) {

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

    public List<CartDetailUserDTO> getCartDetailsByUser() {
        String email = JwtUtil.getCurrentUserLogin().orElseThrow();
        User user = userService.getUserByEmail(email);

        Cart cart = user.getCart();
        if (cart == null) {
            // Return empty data to avoid FrontEnd errors when calling the getcartDetail API
            // in case user not have cart-cartdetail
            return null;
        }
        List<CartDetail> cartDetails = cart.getCartDetails();

        var lst = new ArrayList<CartDetailUserDTO>();

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
        }
        return lst;
    }
}
