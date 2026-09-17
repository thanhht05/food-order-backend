package com.thanh.foodorder.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.foreign.Linker.Option;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jpa.domain.Specification;

import com.thanh.foodorder.domain.Address;
import com.thanh.foodorder.domain.Order;
import com.thanh.foodorder.domain.OrderDetail;
import com.thanh.foodorder.domain.Product;
import com.thanh.foodorder.domain.User;
import com.thanh.foodorder.dto.response.order.AdminOrderResponseDTO;
import com.thanh.foodorder.dto.response.order.OrderResponseDTO;
import com.thanh.foodorder.enums.OrderStatus;
import com.thanh.foodorder.enums.PaymentStatus;
import com.thanh.foodorder.repository.CartDetailRepository;
import com.thanh.foodorder.repository.OrderDetailRepository;
import com.thanh.foodorder.repository.OrderRepository;
import com.thanh.foodorder.util.exception.CommonException;

import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;

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
}
