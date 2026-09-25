package com.thanh.foodorder.feature.order.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.webhooks.WebhookData;
import com.thanh.foodorder.core.util.event.OrderCreatedEvent;
import com.thanh.foodorder.core.util.event.OrderPaidEvent;
import com.thanh.foodorder.core.util.exception.CommonException;
import com.thanh.foodorder.feature.cart.domain.Cart;
import com.thanh.foodorder.feature.cart.domain.CartDetail;
import com.thanh.foodorder.feature.cart.repository.CartDetailRepository;
import com.thanh.foodorder.feature.order.domain.Order;
import com.thanh.foodorder.feature.order.domain.OrderDetail;
import com.thanh.foodorder.feature.order.dto.AdminOrderResponseDTO;
import com.thanh.foodorder.feature.order.dto.CheckoutRequestDTO;
import com.thanh.foodorder.feature.order.dto.OrderHistoryDTO;
import com.thanh.foodorder.feature.order.dto.OrderHistoryProjection;
import com.thanh.foodorder.feature.order.dto.OrderResponseDTO;
import com.thanh.foodorder.feature.order.enums.OrderStatus;
import com.thanh.foodorder.feature.order.enums.PaymentStatus;
import com.thanh.foodorder.feature.order.repository.OrderDetailRepository;
import com.thanh.foodorder.feature.order.repository.OrderRepository;
import com.thanh.foodorder.feature.product.domain.Product;
import com.thanh.foodorder.feature.user.domain.Address;
import com.thanh.foodorder.feature.user.domain.User;
import com.thanh.foodorder.feature.user.service.UserService;
import com.thanh.foodorder.feature.voucher.service.VoucherService;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {
        @Mock
        private VoucherService voucherService;
        @Mock

        private OrderRepository orderRepository;
        @Mock

        private CartDetailRepository cartDetailRepository;
        @Mock

        private OrderDetailRepository orderDetailRepository;
        @Mock

        private UserService userService;
        @Mock

        private ApplicationEventPublisher eventPublisher;

        @InjectMocks
        private OrderService orderService;
        private Order order;

        @BeforeEach
        void setUp() {
                // Create User
                User user = new User();
                user.setFullName("userName");
                user.setId(1L);
                user.setEmail("test@gmail.com");

                // Create Address
                Address address = new Address();
                address.setRecipientName("Thanh Huu");
                address.setPhone("0123456789");
                address.setProvince("Thua Thien Hue");
                address.setWard("Phu Hoi");
                address.setAddressDetail("123 Nguyen Hue");

                // Create Order
                order = new Order();
                order.setId(1L);
                order.setOrderDate(LocalDateTime.now());
                order.setTotalPrice(new BigDecimal("120000"));
                order.setDiscount(BigDecimal.ZERO);
                order.setNote("No spicy");
                order.setOrderStatus(OrderStatus.PENDING);
                order.setPaymentStatus(PaymentStatus.UNPAID);
                order.setPaymentMethod("PAYOS");
                order.setPaymentLinkId("payment-link-123");
                order.setOrderCode(123456L);
                order.setExpiredAt(System.currentTimeMillis() + 30 * 60 * 1000);
                order.setAddress(address);
                order.setUser(user);

                order.setOrderDetails(new ArrayList<>());
        }

        @Test
        void getAllOrder_shouldReturnOrders_whenOrderStatusIsPending() {
                order.setOrderStatus(OrderStatus.PENDING);
                when(orderRepository.findAll(any(Specification.class))).thenReturn(List.of(order));

                List<AdminOrderResponseDTO> result = orderService.getAllOrder(OrderStatus.PENDING);

                assertNotNull(result);
                assertEquals(1, result.size());

                verify(orderRepository).findAll(any(Specification.class));
        }

        @Test
        void getAllOrder_shouldReturnEmptyList_whenNoOrder() {
                when(orderRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());

                List<AdminOrderResponseDTO> result = orderService.getAllOrder(OrderStatus.PENDING);

                assertNotNull(result);
                assertTrue(result.isEmpty());

                verify(orderRepository).findAll(any(Specification.class));

        }

        @Test
        void getAllOrder_shouldReturnAllOrders_whenOrderStatusIsNull() {
                when(orderRepository.findAll(any(Specification.class)))
                                .thenReturn(List.of(order));

                List<AdminOrderResponseDTO> result = orderService.getAllOrder(null);

                assertNotNull(result);
                assertEquals(1, result.size());

                verify(orderRepository).findAll(any(Specification.class));
        }

        @Test
        void getOrderByIdSuccessfully() {
                when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

                Order orderDB = orderService.getOrderById(order.getId());

                assertNotNull(orderDB);

                assertEquals(orderDB.getId(), order.getId());

                verify(orderRepository, times(1)).findById(order.getId());
        }

        @Test
        void getOrderById_shouldThrowException() {
                when(orderRepository.findById(order.getId())).thenReturn(Optional.empty());

                assertThrows(CommonException.class, () -> orderService.getOrderById(order.getId()));

                verify(orderRepository, times(1)).findById(order.getId());
        }

        @Test
        void getOrderByOrderCodeSuccessfully() {
                when(orderRepository.findByOrderCode(order.getOrderCode())).thenReturn(Optional.of(order));

                Order orderDb = orderService.getOrderByOrderCode(order.getOrderCode());
                assertNotNull(orderDb);

                verify(orderRepository, times(1)).findByOrderCode(order.getOrderCode());
        }

        @Test
        void getOrderByOrderCode_shouldThrowException() {
                when(orderRepository.findByOrderCode(order.getOrderCode())).thenReturn(Optional.empty());

                assertThrows(CommonException.class, () -> orderService.getOrderByOrderCode(order.getOrderCode()));

                verify(orderRepository, times(1)).findByOrderCode(order.getOrderCode());

        }

        @Test
        void handleCreatePaymentLink_shouldUpdateOrderAndSave() {
                Long orderCode = 123456L;

                CreatePaymentLinkResponse data = new CreatePaymentLinkResponse();
                data.setPaymentLinkId("payment-link-123");

                long expiredAt = LocalDateTime.now()
                                .plusMinutes(30)
                                .atZone(ZoneId.systemDefault())
                                .toInstant()
                                .toEpochMilli();
                data.setExpiredAt(expiredAt);

                orderService.handleCreatePaymetLink(order, orderCode, data);

                assertEquals(orderCode, order.getOrderCode());
                assertEquals("payment-link-123", order.getPaymentLinkId());
                assertEquals(PaymentStatus.PENDING, order.getPaymentStatus());
                assertEquals(expiredAt, order.getExpiredAt());

                verify(orderRepository, times(1)).save(order);
        }

        @Test
        void getOrderByPaymentLinkSuccessfully() {
                when(orderRepository.findByPaymentLinkId(order.getPaymentLinkId())).thenReturn(Optional.of(order));

                Order orderDb = orderService.getOrderByPaymentLink(order.getPaymentLinkId());

                assertNotNull(orderDb);
                assertEquals(1L, orderDb.getId());

                verify(orderRepository, times(1)).findByPaymentLinkId(order.getPaymentLinkId());
        }

        @Test
        void getOrderByPaymentLink_shouldThrowException() {
                when(orderRepository.findByPaymentLinkId(order.getPaymentLinkId())).thenReturn(Optional.empty());

                assertThrows(CommonException.class, () -> orderService.getOrderByPaymentLink(order.getPaymentLinkId()));

                verify(orderRepository, times(1)).findByPaymentLinkId(order.getPaymentLinkId());

        }

        @Test
        void handelCancelPaymentSuccessfully() {
                when(orderRepository.findByOrderCode(order.getOrderCode())).thenReturn(Optional.of(order));

                orderService.handelCancelPayment(order.getOrderCode());

                assertEquals(OrderStatus.CANCELLED, order.getOrderStatus());
                assertEquals(PaymentStatus.CANCELLED, order.getPaymentStatus());

                verify(orderRepository).findByOrderCode(order.getOrderCode());
                verify(orderRepository).save(order);
        }

        @Test
        void getOrderDetailSuccessfully() {
                Product p = new Product();
                p.setPrice(new BigDecimal("120000"));
                p.setId(1L);
                p.setName("nameProduct");

                OrderDetail orderDetail = new OrderDetail();
                orderDetail.setId(1L);
                orderDetail.setOrder(order);
                orderDetail.setPrice(new BigDecimal("120000"));
                orderDetail.setQuantity(1);
                orderDetail.setProduct(p);

                when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
                when(orderDetailRepository.findByOrderId(order.getId())).thenReturn(List.of(orderDetail));

                OrderResponseDTO res = orderService.getOrderDetail(order.getId());

                assertEquals("userName", res.getCustomerName());
                assertEquals(1L, res.getOrderId());
                assertEquals(BigDecimal.valueOf(120000), res.getTotalPrice());

                assertEquals("nameProduct", res.getItems().get(0).getProductName());

                verify(orderRepository).findById(order.getId());
                verify(orderDetailRepository).findByOrderId(order.getId());

        }

        @Test
        void placeOrderSuccessfully() {

                Cart cart = Cart.builder()
                                .id(1L)
                                .user(order.getUser())
                                .build();

                Product product = Product.builder().id(1L)
                                .name("productname")
                                .price(new BigDecimal(100000))
                                .quantity(100).build();

                Address shippingAddress = Address.builder()
                                .recipientName("huu thanh")
                                .phone("0898173004")
                                .province("TP Hue").ward("Phuong THuan Hoa")
                                .addressDetail("199 DIen bien phu")
                                .build();
                CheckoutRequestDTO req = new CheckoutRequestDTO();
                req.setCartDetailIds(List.of(1L));
                req.setPaymentMethod("QR");
                req.setShippingAddress(shippingAddress);

                CartDetail cd1 = CartDetail.builder()
                                .cart(cart)
                                .id(1L)
                                .product(product)
                                .price(product.getPrice())
                                .quantity(1)
                                .build();

                when(cartDetailRepository.findByIdIn(req.getCartDetailIds())).thenReturn(List.of(cd1));

                when(orderRepository.save(any(Order.class)))
                                .thenReturn(order);

                OrderDetail od = OrderDetail.builder()
                                .id(1L)
                                .order(order)
                                .product(product)
                                .quantity(1)
                                .price(product.getPrice())
                                .build();

                when(orderDetailRepository.saveAll(anyList()))
                                .thenReturn(List.of(od));

                // Act
                OrderResponseDTO result = orderService.placeOrder(req, order.getUser());

                // Assert
                assertNotNull(result);

                verify(orderRepository).save(any(Order.class));
                verify(orderDetailRepository).saveAll(anyList());

                verify(eventPublisher)
                                .publishEvent(any(OrderCreatedEvent.class));

                verify(cartDetailRepository, never())
                                .deleteAll(anyList());
                verify(voucherService, never()).getVoucherByCode(anyString());
        }

        @Test
        void placeOrder_whenNoAvailableItems_shouldThrowException() {

                // Arrange
                CheckoutRequestDTO req = new CheckoutRequestDTO();
                req.setCartDetailIds(List.of(1L));
                req.setPaymentMethod("QR");

                when(cartDetailRepository.findByIdIn(req.getCartDetailIds()))
                                .thenReturn(List.of());

                // Act & Assert
                CommonException exception = assertThrows(
                                CommonException.class,
                                () -> orderService.placeOrder(req, order.getUser()));

                assertEquals(
                                "No available items to place order",
                                exception.getMessage());

                verify(cartDetailRepository)
                                .findByIdIn(req.getCartDetailIds());

                verify(orderRepository, never())
                                .save(any(Order.class));

                verify(orderDetailRepository, never())
                                .saveAll(anyList());

                verify(eventPublisher, never())
                                .publishEvent(any());
        }

        @Test
        void placeOrder_whenCartNotBelongToUser_shouldThrowException() {

                // Arrange

                User user = new User();
                user.setId(2L);

                Cart cart = Cart.builder()
                                .id(1L)
                                .user(user)
                                .build();

                CartDetail cd1 = CartDetail.builder()
                                .cart(cart)
                                .id(1L)

                                .quantity(1)
                                .build();

                CheckoutRequestDTO req = new CheckoutRequestDTO();
                req.setCartDetailIds(List.of(1L));
                req.setPaymentMethod("QR");
                req.setCartDetailIds(List.of(1L));

                when(cartDetailRepository.findByIdIn(req.getCartDetailIds()))
                                .thenReturn(List.of(cd1));

                // Act & Assert
                CommonException exception = assertThrows(
                                CommonException.class,
                                () -> orderService.placeOrder(req, order.getUser()));

                assertEquals(
                                "CartDetail does not belong to the current user",
                                exception.getMessage());

                verify(cartDetailRepository)
                                .findByIdIn(req.getCartDetailIds());

                verify(orderRepository, never())
                                .save(any(Order.class));

                verify(orderDetailRepository, never())
                                .saveAll(anyList());

                verify(eventPublisher, never())
                                .publishEvent(any());
        }

        @Test
        void handlePaymentSuccessfully() {

                // Arrange
                Long orderCode = 123456L;

                WebhookData data = new WebhookData();
                data.setAmount(120000L);

                when(orderRepository.findByOrderCode(orderCode))
                                .thenReturn(Optional.of(order));

                when(orderRepository.save(any(Order.class)))
                                .thenReturn(order);

                // Act
                orderService.handlePayment(orderCode, data);

                // Assert
                assertEquals(PaymentStatus.PAID, order.getPaymentStatus());

                verify(orderRepository).save(order);

                verify(eventPublisher)
                                .publishEvent(any(OrderPaidEvent.class));
        }

        @Test
        void handlePayment_Order_already_paid_shouldThrowException() {
                Long orderCode = 123L;

                order.setPaymentStatus(PaymentStatus.PAID);

                when(orderRepository.findByOrderCode(orderCode)).thenReturn(Optional.of(order));

                WebhookData data = new WebhookData();
                data.setAmount(120000L);

                CommonException exception = assertThrows(
                                CommonException.class,
                                () -> orderService.handlePayment(orderCode, data));

                assertEquals(
                                "Order already paid",
                                exception.getMessage());
                assertEquals(PaymentStatus.PAID, order.getPaymentStatus());

                verify(orderRepository, never())
                                .save(any(Order.class));

                verify(eventPublisher, never())
                                .publishEvent(OrderPaidEvent.class);
        }

        @Test
        void handlePayment_Price_invalid_shouldThrowException() {
                Long orderCode = 123L;

                when(orderRepository.findByOrderCode(orderCode)).thenReturn(Optional.of(order));

                WebhookData data = new WebhookData();
                data.setAmount(1200001L);

                CommonException exception = assertThrows(
                                CommonException.class,
                                () -> orderService.handlePayment(orderCode, data));

                assertEquals(
                                "Price invalid",
                                exception.getMessage());
                assertNotEquals(data.getAmount(), order.getTotalPrice());

                verify(orderRepository, never())
                                .save(any(Order.class));

                verify(eventPublisher, never())
                                .publishEvent(OrderPaidEvent.class);
        }

        @AfterEach
        void tearDown() {
                SecurityContextHolder.clearContext();
        }

        @Test
        void updateOrder_successfully() {
                Order updateReq = new Order();
                updateReq.setId(1L);
                updateReq.setOrderStatus(OrderStatus.CONFIRMED);

                when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
                when(orderRepository.save(any(Order.class))).thenReturn(order);
                when(orderDetailRepository.findByOrderId(1L)).thenReturn(Collections.emptyList());

                OrderResponseDTO response = orderService.updateOrder(updateReq);

                assertNotNull(response);
                assertEquals(OrderStatus.CONFIRMED.name(), response.getStatus());
                assertEquals(OrderStatus.CONFIRMED, order.getOrderStatus());
                verify(orderRepository).save(order);
                verify(orderDetailRepository).findByOrderId(1L);
        }

        @Test
        void updateOrder_notFound_shouldThrowException() {
                Order updateReq = new Order();
                updateReq.setId(999L);
                updateReq.setOrderStatus(OrderStatus.CONFIRMED);

                when(orderRepository.findById(999L)).thenReturn(Optional.empty());

                CommonException exception = assertThrows(CommonException.class,
                                () -> orderService.updateOrder(updateReq));

                assertEquals("Order with id 999 not found", exception.getMessage());
                verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        void getOrderHistoryByUser_whenNoOrders_shouldReturnEmptyList() {
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                Authentication auth = new UsernamePasswordAuthenticationToken("test@gmail.com", null,
                                Collections.emptyList());
                context.setAuthentication(auth);
                SecurityContextHolder.setContext(context);

                User user = order.getUser();
                Cart cart = new Cart();
                cart.setId(10L);
                user.setCart(cart);

                when(userService.getUserByEmail("test@gmail.com")).thenReturn(user);
                when(orderRepository.findOrderHistoryByUserId(user.getId())).thenReturn(Collections.emptyList());

                OrderHistoryDTO result = orderService.getOrderHistoryByUser();

                assertNotNull(result);
                assertEquals(user.getId(), result.getUserId());
                assertEquals(user.getFullName(), result.getFullName());
                assertEquals(10L, result.getCartId());
                assertTrue(result.getOrderInfo().isEmpty());
        }

        @Test
        void getOrderHistoryByUser_withOrders_shouldReturnGroupedHistory() {
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                Authentication auth = new UsernamePasswordAuthenticationToken("test@gmail.com", null,
                                Collections.emptyList());
                context.setAuthentication(auth);
                SecurityContextHolder.setContext(context);

                User user = order.getUser();
                Cart cart = new Cart();
                cart.setId(10L);
                user.setCart(cart);

                OrderHistoryProjection row1 = mock(OrderHistoryProjection.class);
                when(row1.getOrderId()).thenReturn(1L);
                when(row1.getOrderDate()).thenReturn(Instant.now());
                when(row1.getOrderStatus()).thenReturn("PENDING");
                when(row1.getPaymentStatus()).thenReturn("UNPAID");
                when(row1.getPaymentMethod()).thenReturn("PAYOS");
                when(row1.getTotalPrice()).thenReturn(120000.0);
                when(row1.getProductId()).thenReturn(10L);
                when(row1.getProductName()).thenReturn("Burger");
                when(row1.getPrice()).thenReturn(60000.0);
                when(row1.getQuantity()).thenReturn(2L);
                when(row1.getRecipientName()).thenReturn("Thanh Huu");
                when(row1.getPhone()).thenReturn("0123456789");
                when(row1.getProvince()).thenReturn("Thua Thien Hue");
                when(row1.getWard()).thenReturn("Phu Hoi");
                when(row1.getAddressDetail()).thenReturn("123 Nguyen Hue");
                when(row1.getPaymentLinkId()).thenReturn("pay-link-123");

                when(userService.getUserByEmail("test@gmail.com")).thenReturn(user);
                when(orderRepository.findOrderHistoryByUserId(user.getId())).thenReturn(List.of(row1));

                OrderHistoryDTO result = orderService.getOrderHistoryByUser();

                assertNotNull(result);
                assertEquals(user.getId(), result.getUserId());
                assertEquals(1, result.getOrderInfo().size());
                assertEquals(1L, result.getOrderInfo().get(0).getOrderId());
                assertEquals(OrderStatus.PENDING, result.getOrderInfo().get(0).getOrderStatus());
                assertEquals(1, result.getOrderInfo().get(0).getProducts().size());
                assertEquals("Burger", result.getOrderInfo().get(0).getProducts().get(0).getProductName());
        }
}