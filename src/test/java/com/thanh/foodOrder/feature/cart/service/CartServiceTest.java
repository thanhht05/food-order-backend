package com.thanh.foodorder.feature.cart.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.thanh.foodorder.core.util.JwtUtil;
import com.thanh.foodorder.feature.cart.domain.Cart;
import com.thanh.foodorder.feature.cart.domain.CartDetail;
import com.thanh.foodorder.feature.cart.dto.AddToCartResponseDTO;
import com.thanh.foodorder.feature.cart.dto.CartRequestDTO;
import com.thanh.foodorder.feature.cart.dto.CartResponeDTO;
import com.thanh.foodorder.feature.cart.repository.CartDetailRepository;
import com.thanh.foodorder.feature.cart.repository.CartRepository;
import com.thanh.foodorder.feature.category.domain.Category;
import com.thanh.foodorder.feature.product.domain.Product;
import com.thanh.foodorder.feature.product.domain.ProductImage;
import com.thanh.foodorder.feature.product.service.ProductService;
import com.thanh.foodorder.feature.user.domain.User;
import com.thanh.foodorder.feature.user.service.RoleService;
import com.thanh.foodorder.feature.user.service.UserService;

@ExtendWith(MockitoExtension.class)
public class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private UserService userService;

    @Mock
    private ProductService productService;

    @Mock
    private CartDetailRepository cartDetailRepository;

    @Mock
    private RoleService roleService;

    @InjectMocks
    private CartService cartService;

    private User user;
    private Cart cart;
    private Product product;
    private CartDetail cartDetail;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("customer@gmail.com");

        cart = new Cart();
        cart.setId(10L);
        cart.setUser(user);
        cart.setCartDetails(new ArrayList<>());
        user.setCart(cart);

        Category category = new Category();
        category.setId(1L);
        category.setName("Fast Food");

        ProductImage img = new ProductImage();
        img.setId(1L);
        img.setImgName("burger.png");

        product = Product.builder()
                .id(100L)
                .name("Burger Deluxe")
                .price(BigDecimal.valueOf(50000))
                .quantity(10)
                .category(category)
                .lstImg(List.of(img))
                .build();

        cartDetail = CartDetail.builder()
                .id(1000L)
                .cart(cart)
                .product(product)
                .quantity(2)
                .price(BigDecimal.valueOf(50000))
                .build();
    }

    @Test
    @DisplayName("getCartDetailsByUser - Thành công, nạp chi tiết giỏ hàng bằng findByCartWithDetails (Tránh N+1)")
    void getCartDetailsByUser_Success() {
        try (MockedStatic<JwtUtil> mockedJwt = Mockito.mockStatic(JwtUtil.class)) {
            mockedJwt.when(JwtUtil::getCurrentUserLogin).thenReturn(Optional.of("customer@gmail.com"));
            when(userService.getUserByEmail("customer@gmail.com")).thenReturn(user);
            when(cartDetailRepository.findByCartWithDetails(cart)).thenReturn(List.of(cartDetail));

            CartResponeDTO result = cartService.getCartDetailsByUser();

            assertNotNull(result);
            assertEquals(2, result.getTotalQuantity());
            assertEquals(BigDecimal.valueOf(100000), result.getTotalPrice());
            assertEquals(1, result.getLst().size());

            var item = result.getLst().get(0).getProductsInnerCartDetail();
            assertEquals(100L, item.getId());
            assertEquals("Burger Deluxe", item.getName());
            assertEquals("Fast Food", item.getCategoryName());
            assertEquals("burger.png", item.getImg());
            assertEquals(BigDecimal.valueOf(50000), item.getPrice());

            verify(cartDetailRepository, times(1)).findByCartWithDetails(cart);
        }
    }

    @Test
    @DisplayName("getCartDetailsByUser - Giỏ hàng rỗng (user chưa có cart)")
    void getCartDetailsByUser_NullCart() {
        user.setCart(null);
        try (MockedStatic<JwtUtil> mockedJwt = Mockito.mockStatic(JwtUtil.class)) {
            mockedJwt.when(JwtUtil::getCurrentUserLogin).thenReturn(Optional.of("customer@gmail.com"));
            when(userService.getUserByEmail("customer@gmail.com")).thenReturn(user);

            CartResponeDTO result = cartService.getCartDetailsByUser();

            assertNotNull(result);
            assertEquals(0, result.getTotalQuantity());
            assertEquals(BigDecimal.ZERO, result.getTotalPrice());
            assertEquals(0, result.getLst().size());
        }
    }

    @Test
    @DisplayName("addProductsToCart - Thêm sản phẩm vào giỏ hàng thành công")
    void addProductsToCart_Success() {
        try (MockedStatic<JwtUtil> mockedJwt = Mockito.mockStatic(JwtUtil.class)) {
            mockedJwt.when(JwtUtil::getCurrentUserLogin).thenReturn(Optional.of("customer@gmail.com"));
            when(userService.getUserByEmail("customer@gmail.com")).thenReturn(user);
            when(productService.getProductById(100L)).thenReturn(product);
            when(cartDetailRepository.findByCartWithDetails(cart)).thenReturn(List.of(cartDetail));

            CartRequestDTO request = new CartRequestDTO();
            request.setProductId(100L);
            request.setQuantity(2);

            CartResponeDTO result = cartService.addProductsToCart(request);

            assertNotNull(result);
            verify(cartRepository, times(1)).save(cart);
            verify(cartDetailRepository, times(1)).findByCartWithDetails(cart);
        }
    }

    @Test
    @DisplayName("removeProductFromCart - Xóa sản phẩm khỏi giỏ hàng")
    void removeProductFromCart_Success() {
        cart.getCartDetails().add(cartDetail);
        try (MockedStatic<JwtUtil> mockedJwt = Mockito.mockStatic(JwtUtil.class)) {
            mockedJwt.when(JwtUtil::getCurrentUserLogin).thenReturn(Optional.of("customer@gmail.com"));
            when(userService.getUserByEmail("customer@gmail.com")).thenReturn(user);
            when(cartRepository.findByUserId(1L)).thenReturn(cart);

            AddToCartResponseDTO response = cartService.removeProductFromCart(100L);

            assertNotNull(response);
            verify(cartRepository, times(1)).save(cart);
        }
    }

    @Test
    @DisplayName("updateCartItem - Cập nhật số lượng sản phẩm")
    void updateCartItem_Success() {
        try (MockedStatic<JwtUtil> mockedJwt = Mockito.mockStatic(JwtUtil.class)) {
            mockedJwt.when(JwtUtil::getCurrentUserLogin).thenReturn(Optional.of("customer@gmail.com"));
            when(userService.getUserByEmail("customer@gmail.com")).thenReturn(user);
            when(productService.getProductById(100L)).thenReturn(product);
            when(cartDetailRepository.findByCartAndProductId(cart, 100L)).thenReturn(cartDetail);
            when(cartDetailRepository.findByCartWithDetails(cart)).thenReturn(List.of(cartDetail));

            CartRequestDTO request = new CartRequestDTO();
            request.setProductId(100L);
            request.setQuantity(5);

            CartResponeDTO response = cartService.updateCartItem(request);

            assertNotNull(response);
            assertEquals(5, cartDetail.getQuantity());
            verify(cartDetailRepository, times(1)).save(cartDetail);
            verify(cartDetailRepository, times(1)).findByCartWithDetails(cart);
        }
    }
}
