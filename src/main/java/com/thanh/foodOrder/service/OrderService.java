package com.thanh.foodorder.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.catalina.security.SecurityUtil;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thanh.foodorder.domain.Address;
import com.thanh.foodorder.domain.CartDetail;
import com.thanh.foodorder.domain.Order;
import com.thanh.foodorder.domain.OrderDetail;
import com.thanh.foodorder.domain.Product;
import com.thanh.foodorder.domain.User;
import com.thanh.foodorder.domain.Voucher;
import com.thanh.foodorder.dto.CreateOrderData;
import com.thanh.foodorder.dto.request.BuyNowRequestDTO;
import com.thanh.foodorder.dto.request.CheckoutRequestDTO;
import com.thanh.foodorder.dto.response.CheckOutResponseDTO;
import com.thanh.foodorder.dto.response.order.AdminOrderResponseDTO;
import com.thanh.foodorder.dto.response.order.OrderHistoryDTO;
import com.thanh.foodorder.dto.response.order.OrderHistoryProjection;
import com.thanh.foodorder.dto.response.order.OrderItemDTO;
import com.thanh.foodorder.dto.response.order.OrderResponseDTO;
import com.thanh.foodorder.dto.statistic.LatestOrderResponse;
import com.thanh.foodorder.enums.OrderStatus;
import com.thanh.foodorder.enums.PaymentStatus;

import com.thanh.foodorder.repository.CartDetailRepository;
import com.thanh.foodorder.repository.CartRepository;
import com.thanh.foodorder.repository.OrderDetailRepository;
import com.thanh.foodorder.repository.OrderRepository;
import com.thanh.foodorder.specification.OrderSpecification;
import com.thanh.foodorder.util.JwtUtil;
import com.thanh.foodorder.util.event.OrderCreatedEvent;
import com.thanh.foodorder.util.event.OrderPaidEvent;
import com.thanh.foodorder.util.exception.CommonException;

import lombok.extern.log4j.Log4j2;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.webhooks.WebhookData;

@Service
@Log4j2
public class OrderService {

    private final VoucherService voucherService;
    private final OrderRepository orderRepository;
    private final CartDetailRepository cartDetailRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductService productService;
    private final UserService userService;
    private final ApplicationEventPublisher eventPublisher;

    public OrderService(OrderRepository orderRepository, CartDetailRepository cartDetailRepository,
            OrderDetailRepository orderDetailRepository,
            ProductService productService, VoucherService voucherService, UserService userService,
            ApplicationEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.voucherService = voucherService;
        this.cartDetailRepository = cartDetailRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.productService = productService;
        this.userService = userService;
        this.eventPublisher = eventPublisher;

    }

    public List<AdminOrderResponseDTO> getAllOrder(OrderStatus orderStatus) {
        Specification<Order> spec = Specification.allOf(OrderSpecification.hasStatus(orderStatus));
        List<Order> lstOrders = this.orderRepository.findAll(spec);
        List<AdminOrderResponseDTO> res = new ArrayList<>();

        for (Order od : lstOrders) {
            AdminOrderResponseDTO item = AdminOrderResponseDTO.from(od);
            res.add(item);
        }

        return res;
    }

    public Order getOrderById(Long id) {

        return this.orderRepository.findById(id).orElseThrow(() -> {
            log.warn("Order with id: {} not found", id);
            return new CommonException("Order with id " + id + " not found");
        });

    }

    public Order getOrderByOrderCode(Long orderCode) {
        return orderRepository.findByOrderCode(orderCode).orElseThrow(() -> {
            log.warn("Order with order code: {} not found", orderCode);
            return new CommonException("Order order code " + orderCode + " not found");

        });

    }

    // get order on screen admin
    public AdminOrderResponseDTO getResponseOrderById(Long id) {
        Order order = getOrderById(id);
        AdminOrderResponseDTO res = AdminOrderResponseDTO.from(order);
        return res;

    }

    private boolean isPayment(Order order) {
        return order.getPaymentStatus() == PaymentStatus.PAID;
    }

    @Transactional
    public void handleCreatePaymetLink(Order order, Long orderCode, CreatePaymentLinkResponse data) {

        order.setOrderCode(orderCode);
        order.setPaymentLinkId(data.getPaymentLinkId());
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setExpiredAt(data.getExpiredAt());

        save(order);
    }

    @Transactional
    public void handlePayment(Long orderCode, WebhookData data) {

        Order order = getOrderByOrderCode(orderCode);

        if (order == null) {
            throw new CommonException("Order not found");
        }

        // Kiểm tra đã thanh toán
        if (isPayment(order)) {
            throw new CommonException("Order already paid");
        }

        // Kiểm tra số tiền
        if (order.getTotalPrice().longValue() != data.getAmount()) {
            throw new CommonException("Price invalid");
        }

        // Cập nhật payment status
        order.setPaymentStatus(PaymentStatus.PAID);

        Order orderSaved = this.orderRepository.save(order);
        eventPublisher.publishEvent(
                new OrderPaidEvent(orderSaved));
        clearCart(order);
    }

    public Order getOrderByPaymentLink(String id) {
        return orderRepository.findByPaymentLinkId(id).orElseThrow(() -> {
            log.warn("Order with paymentLinkId: {} not found", id);
            return new CommonException("Order with " + id + " not found");

        });
    }

    @Transactional
    public void handelCancelPayment(Long id) {
        Order order = getOrderByOrderCode(id);

        order.setOrderStatus(OrderStatus.CANCELLED);
        order.setPaymentStatus(PaymentStatus.CANCELLED);

        this.orderRepository.save(order);
    }

    public void save(Order order) {
        this.orderRepository.save(order);
    }

    public OrderResponseDTO getOrderDetail(Long id) {
        Order order = getOrderById(id);
        List<OrderDetail> orderDetails = this.orderDetailRepository.findByOrderId(id);

        OrderResponseDTO res = mapToOrderResponseDTO(order, orderDetails);
        return res;
    }

    private OrderResponseDTO mapToOrderResponseDTO(
            Order order,
            List<OrderDetail> orderDetails) {

        OrderResponseDTO dto = new OrderResponseDTO();

        // Order information
        dto.setOrderId(order.getId());
        dto.setOrderDate(order.getOrderDate());
        dto.setStatus(order.getOrderStatus().name());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setDiscount(order.getDiscount());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setCustomerName(order.getUser().getFullName());
        dto.setEmail(order.getUser().getEmail());
        dto.setAddress(order.getAddress());

        // Order items
        List<OrderItemDTO> items = new ArrayList<>();

        for (OrderDetail od : orderDetails) {
            OrderItemDTO itemDTO = new OrderItemDTO();
            itemDTO.setProductId(od.getProduct().getId());
            itemDTO.setProductName(od.getProduct().getName());
            itemDTO.setQuantity(od.getQuantity());
            itemDTO.setPrice(od.getPrice());
            items.add(itemDTO);
        }

        dto.setItems(items);

        return dto;
    }

    private void validBeforePlaceOrder(CheckoutRequestDTO dto, User curUser, List<CartDetail> cartDetails) {

        // If no items are available for checkout, stop the process
        if (cartDetails.isEmpty()) {
            throw new CommonException("No available items to place order");
        }

        // 2. Validate CartDetail ownership and quantity
        for (CartDetail cd : cartDetails) {

            // Check whether the cart detail belongs to the current user
            if (!cd.getCart().getUser().getId().equals(curUser.getId())) {
                throw new CommonException("CartDetail does not belong to the current user");
            }

            // Validate item quantity
            if (cd.getQuantity() <= 0) {
                throw new CommonException("Item quantity is invalid");
            }
        }
    }

    private BigDecimal caculateTotalPrice(List<CartDetail> cartDetails) {
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (CartDetail cd : cartDetails) {
            BigDecimal amount = cd.getPrice().multiply(BigDecimal.valueOf(cd.getQuantity()));
            totalPrice = totalPrice.add(amount);
        }
        return totalPrice;
    }

    public CheckOutResponseDTO handleCheckOut(CheckoutRequestDTO dto, User curUser) {

        List<CartDetail> cartDetails = cartDetailRepository.findByIdIn(dto.getCartDetailIds());

        // 1. Validate
        validBeforePlaceOrder(dto, curUser, cartDetails);

        // 2. Caculate price
        BigDecimal totalPrice = caculateTotalPrice(cartDetails);
        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal finalPrice = totalPrice;

        // 3. If have an voucher then CHECK
        if (dto.getVoucherCode() != null) {
            Voucher voucher = voucherService.getVoucherByCode(dto.getVoucherCode());
            voucherService.checkVoucherBeforeApply(voucher, curUser);
            BigDecimal percent = BigDecimal.valueOf(voucher.getPercentDiscount());

            // totalPrice * percent / 100
            BigDecimal discountByPercent = totalPrice
                    .multiply(percent)
                    .divide(BigDecimal.valueOf(100));
            // Lấy số nhỏ hơn giữa giảm theo % và maxDiscount
            discount = discountByPercent.min(BigDecimal.valueOf(voucher.getMaxDiscount()));
            finalPrice = totalPrice.subtract(discount);
        }

        // 4. Retuen preview for user
        CheckOutResponseDTO res = new CheckOutResponseDTO();
        for (CartDetail cd : cartDetails) {
            Long cartDetailId = cd.getId();
            res.getCartDetailIds().add(cartDetailId);
        }
        res.setTotalPrice(totalPrice);
        res.setDiscount(discount);
        res.setFinalPrice(finalPrice);

        return res;
    }

    public void updateSoldQuantity(CartDetail cd) {
        cd.getProduct().setSold(cd.getProduct().getSold() + 1);
    }

    @Transactional
    public OrderResponseDTO placeOrder(CheckoutRequestDTO dto, User curUser) {

        List<CartDetail> cartDetails = cartDetailRepository.findByIdIn(dto.getCartDetailIds());

        // 1. Validate again
        validBeforePlaceOrder(dto, curUser, cartDetails);

        // 2. Caculate price
        BigDecimal totalPrice = caculateTotalPrice(cartDetails);

        Voucher voucher = null;
        BigDecimal discount = BigDecimal.ZERO;

        if (dto.getVoucherCode() != null) {
            voucher = voucherService.getVoucherByCode(dto.getVoucherCode());
            voucherService.checkVoucherBeforeApply(voucher, curUser);

            BigDecimal percent = BigDecimal.valueOf(voucher.getPercentDiscount());

            BigDecimal discountByPercent = totalPrice
                    .multiply(percent)
                    .divide(BigDecimal.valueOf(100));
            discount = discountByPercent.min(BigDecimal.valueOf(voucher.getMaxDiscount()));
            // Update voucher usage
            voucher.setUsageLimit(voucher.getUsageLimit() - 1);
        }

        // 3. Create Order

        Address address = Address.builder().recipientName(dto.getShippingAddress().getRecipientName())
                .phone(dto.getShippingAddress().getPhone())
                .province(dto.getShippingAddress().getProvince()).ward(dto.getShippingAddress().getWard())
                .addressDetail(dto.getShippingAddress().getAddressDetail()).build();

        CreateOrderData data = CreateOrderData.builder()
                .user(curUser)
                .orderDate(LocalDateTime.now())
                .totalPrice(totalPrice)
                .discount(discount)
                .orderStatus(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.UNPAID)
                .paymentMethod(dto.getPaymentMethod())
                .voucher(voucher)
                .note(dto.getNote() != null ? dto.getNote() : "")
                .address(address).build();

        Order order = createOrder(data);
        Order orderSaved = orderRepository.save(order);

        // 4. Create OrderDetail

        List<OrderDetail> orderDetailsToSave = createOrderDetal(orderSaved, cartDetails, dto.getNote());
        List<OrderDetail> savedOrderDetails = orderDetailRepository.saveAll(orderDetailsToSave);
        orderSaved.setOrderDetails(savedOrderDetails);

        // update sold
        for (CartDetail cd : cartDetails) {

            updateSoldQuantity(cd);
        }

        if ("COD".equals(dto.getPaymentMethod())) {
            cartDetailRepository.deleteAll(cartDetails);
        }
        eventPublisher.publishEvent(
                new OrderCreatedEvent(orderSaved));

        return mapToOrderResponseDTO(order, savedOrderDetails);
    }

    private Order createOrder(CreateOrderData data) {
        Order order = new Order();

        order.setUser(data.getUser());
        order.setOrderDate(data.getOrderDate());
        order.setTotalPrice(data.getTotalPrice());
        order.setDiscount(data.getDiscount());
        order.setOrderStatus(data.getOrderStatus());
        order.setPaymentStatus(data.getPaymentStatus());
        order.setVoucher(data.getVoucher());
        order.setNote(data.getNote());
        order.setAddress(data.getAddress());
        order.setPaymentMethod(data.getPaymentMethod());

        return order;
    }

    private List<OrderDetail> createOrderDetal(Order orderSaved, List<CartDetail> cartDetails, String note) {
        List<OrderDetail> orderDetailsToSave = new ArrayList<>();
        // 4. Create OrderDetail
        for (CartDetail cd : cartDetails) {
            OrderDetail od = new OrderDetail();
            od.setOrder(orderSaved);
            od.setProduct(cd.getProduct());
            od.setQuantity(cd.getQuantity());
            od.setPrice(cd.getPrice());
            od.setNote(note);

            orderDetailRepository.save(od);
            // Update inventory
            Product p = cd.getProduct();
            p.setQuantity(p.getQuantity() - cd.getQuantity());
            orderDetailsToSave.add(od);
        }
        return orderDetailsToSave;
    }

    public void clearCart(Order order) {

        List<Long> productIds = order.getOrderDetails()
                .stream()
                .map(detail -> detail.getProduct().getId())
                .toList();

        Long userId = order.getUser().getId();

        cartDetailRepository.deleteByCartUserIdAndProductIdIn(userId, productIds);
    }

    @Transactional
    public OrderResponseDTO updateOrder(Order order) {
        Order orderDb = getOrderById(order.getId());

        orderDb.setOrderStatus(order.getOrderStatus());

        this.orderRepository.save(orderDb);

        List<OrderDetail> odDetails = this.orderDetailRepository.findByOrderId(orderDb.getId());

        return mapToOrderResponseDTO(orderDb, odDetails);
    }

    // get order history

    public OrderHistoryDTO getOrderHistoryByUser() {

        String email = JwtUtil.getCurrentUserLogin()
                .orElseThrow();

        User user = userService.getUserByEmail(email);

        List<OrderHistoryProjection> rows = orderRepository.findOrderHistoryByUserId(user.getId());

        OrderHistoryDTO response = new OrderHistoryDTO();

        response.setUserId(user.getId());
        response.setFullName(user.getFullName());
        response.setCartId(user.getCart().getId());

        if (rows.isEmpty()) {
            response.setOrderInfo(new ArrayList<>());
            return response;
        }

        Map<Long, OrderHistoryDTO.OrderInfo> orderMap = new LinkedHashMap<>();

        for (OrderHistoryProjection row : rows) {

            OrderHistoryDTO.OrderInfo order = orderMap.computeIfAbsent(
                    row.getOrderId(),
                    id -> {

                        OrderHistoryDTO.OrderInfo dto = new OrderHistoryDTO.OrderInfo();

                        dto.setOrderId(row.getOrderId());
                        dto.setOrderDate(row.getOrderDate());
                        dto.setOrderStatus(
                                OrderStatus.valueOf(
                                        row.getOrderStatus()));

                        dto.setTotalPrice(row.getTotalPrice());
                        dto.setPaymentMethod(row.getPaymentMethod());
                        // dto.setPaymentStatus(PaymentStatus.valueOf(row.getPaymentStatus()));
                        dto.setPaymentStatus(PaymentStatus.valueOf(row.getPaymentStatus()));
                        dto.setAddressDetail(row.getAddressDetail());
                        dto.setPhone(row.getPhone());
                        dto.setProvince(row.getProvince());
                        dto.setWard(row.getWard());
                        dto.setRecipientName(row.getRecipientName());
                        dto.setPaymentLinkId(row.getPaymentLinkId());

                        dto.setProducts(new ArrayList<>());

                        return dto;
                    });

            OrderHistoryDTO.ProductInsideOrder product = new OrderHistoryDTO.ProductInsideOrder();

            product.setProductId(row.getProductId());
            product.setProductName(row.getProductName());
            product.setPrice(row.getPrice());
            product.setQuantity(row.getQuantity());
            product.setImg(row.getImg());

            order.getProducts().add(product);
        }

        response.setOrderInfo(
                new ArrayList<>(orderMap.values()));

        return response;
    }

}
