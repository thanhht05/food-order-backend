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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thanh.foodorder.domain.BookingTable;
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
import com.thanh.foodorder.enums.TableStatus;
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

@Service
@Log4j2
public class OrderService {

    private final VoucherService voucherService;
    private final OrderRepository orderRepository;
    private final CartDetailRepository cartDetailRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final BookingTableService bookingTableService;
    private final ProductService productService;
    private final UserService userService;
    private final ApplicationEventPublisher eventPublisher;

    public OrderService(OrderRepository orderRepository, CartDetailRepository cartDetailRepository,
            OrderDetailRepository orderDetailRepository, BookingTableService bookingTableService,
            ProductService productService, VoucherService voucherService, UserService userService,
            ApplicationEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.voucherService = voucherService;
        this.bookingTableService = bookingTableService;
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

    // get order on screen admin
    public AdminOrderResponseDTO getResponseOrderById(Long id) {
        Order order = getOrderById(id);
        AdminOrderResponseDTO res = AdminOrderResponseDTO.from(order);
        return res;

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
        dto.setTableId(order.getBookingTable().getId());
        dto.setPaymentStatus(order.getPaymentStatus());

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

    private void validBeforePlaceOrder(CheckoutRequestDTO dto, User curUser, List<CartDetail> cartDetails,
            BookingTable bookingTable) {

        // check available table
        if (!this.bookingTableService.checkingTableStatus(bookingTable)) {
            throw new CommonException("This table is busy");

        }
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

        BookingTable bookingTable = bookingTableService.getTableById(dto.getTableId());
        List<CartDetail> cartDetails = cartDetailRepository.findByIdIn(dto.getCartDetailIds());

        // 1. Validate
        validBeforePlaceOrder(dto, curUser, cartDetails, bookingTable);

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
        res.setTableId(dto.getTableId());
        res.setFinalPrice(finalPrice);

        return res;
    }

    public void updateSoldQuantity(CartDetail cd) {
        cd.getProduct().setSold(cd.getProduct().getSold() + 1);
    }

    @Transactional
    public OrderResponseDTO placeOrder(CheckoutRequestDTO dto, User curUser) {

        BookingTable bookingTable = bookingTableService.getTableById(dto.getTableId());
        List<CartDetail> cartDetails = cartDetailRepository.findByIdIn(dto.getCartDetailIds());

        // 1. Validate again
        validBeforePlaceOrder(dto, curUser, cartDetails, bookingTable);

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

        CreateOrderData data = CreateOrderData.builder()
                .user(curUser)
                .orderDate(LocalDateTime.now())
                .totalPrice(totalPrice)
                .discount(discount)
                .orderStatus(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.UNPAID)
                .bookingTable(bookingTable)
                .voucher(voucher)
                .note(dto.getNote() != null ? dto.getNote() : "").build();

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

        // 5. Change table status
        bookingTable.setTableStatus(TableStatus.RESERVED);

        // if (dto.getPaymentMethod().equals("CASH")) { // this may have error NPE
        // clearCart(orderSaved);
        // }
        // fix
        if ("CASH".equals(dto.getPaymentMethod())) {
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
        order.setBookingTable(data.getBookingTable());
        order.setVoucher(data.getVoucher());
        order.setNote(data.getNote());

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

    @Transactional
    public OrderResponseDTO handleBuyNow(BuyNowRequestDTO req, User curUser) {
        BookingTable table = this.bookingTableService.getTableById(req.getTableId());
        if (table == null) {
            throw new CommonException("This table is busy");

        }
        Product product = this.productService.getProductById(req.getProductId());
        if (product == null) {
            throw new CommonException("No available product");

        }
        if (req.getQuantity() <= 0) {
            throw new CommonException("Item quantity is invalid");

        }

        BigDecimal totalPrice = product.getPrice().multiply(BigDecimal.valueOf(req.getQuantity()));

        // 3. Create Order
        Order order = new Order();
        order.setUser(curUser);
        order.setOrderDate(LocalDateTime.now());
        order.setTotalPrice(totalPrice);
        order.setDiscount(null);
        order.setOrderStatus(OrderStatus.PENDING);

        // handle payment
        order.setPaymentStatus(PaymentStatus.UNPAID);

        order.setBookingTable(table);
        order.setVoucher(null);
        order.setNote(null);

        orderRepository.save(order);
        // 4. Create OrderDetail
        OrderDetail od = new OrderDetail();
        od.setOrder(order);
        od.setProduct(product);
        od.setQuantity(req.getQuantity());
        od.setPrice(product.getPrice());
        od.setNote(null);

        orderDetailRepository.save(od);
        // Update inventory
        product.setQuantity(product.getQuantity() - req.getQuantity());

        // 5. Change table status
        table.setTableStatus(TableStatus.RESERVED);

        OrderResponseDTO res = new OrderResponseDTO();

        res.setOrderId(order.getId());
        res.setOrderDate(order.getOrderDate());
        res.setStatus(order.getOrderStatus().name());
        res.setTotalPrice(order.getTotalPrice());
        res.setDiscount(order.getDiscount());
        res.setTableId(table.getId());
        res.setPaymentStatus(order.getPaymentStatus());

        List<OrderItemDTO> lst = new ArrayList<>();

        OrderItemDTO item = new OrderItemDTO();
        item.setProductId(product.getId());
        item.setPrice(product.getPrice());
        item.setProductName(product.getName());
        item.setQuantity(req.getQuantity());

        lst.add(item);

        res.setItems(lst);
        return res;

    }

    @Transactional
    public void payOrder(Long id, BigDecimal amount) {

        Order order = getOrderById(id);

        validatePayment(order, amount);

        order.setPaymentStatus(PaymentStatus.PAID);
        order.setOrderStatus(OrderStatus.PENDING);

        clearCart(order);

        // Phát sự kiện
        eventPublisher.publishEvent(new OrderPaidEvent(order));
    }

    public void clearCart(Order order) {

        List<Long> productIds = order.getOrderDetails()
                .stream()
                .map(detail -> detail.getProduct().getId())
                .toList();

        Long userId = order.getUser().getId();

        cartDetailRepository.deleteByCartUserIdAndProductIdIn(userId, productIds);
    }

    private void validatePayment(Order order, BigDecimal amount) {

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new CommonException("Order already paid");
        }

        if (order.getTotalPrice() == null || amount == null || order.getTotalPrice().compareTo(amount) != 0) {
            throw new CommonException("Price is not correct");
        }
        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new CommonException("Cannot pay cancelled order");
        }
    }

    @Transactional
    public OrderResponseDTO updateOrder(Order order) {
        Order orderDb = getOrderById(order.getId());

        orderDb.setOrderStatus(order.getOrderStatus());

        this.orderRepository.save(orderDb);

        BookingTable table = this.bookingTableService.getTableById(orderDb.getBookingTable().getId());
        table.setTableStatus(TableStatus.AVAILABLE);
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

                        dto.setTableId(row.getTableId());
                        dto.setTotalPrice(row.getTotalPrice());

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
