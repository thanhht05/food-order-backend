package com.thanh.foodorder.service;

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
import com.thanh.foodorder.dto.request.BuyNowRequestDTO;
import com.thanh.foodorder.dto.request.CheckoutRequestDTO;
import com.thanh.foodorder.dto.response.CheckOutResponseDTO;
import com.thanh.foodorder.dto.response.order.AdminOrderResponseDTO;
import com.thanh.foodorder.dto.response.order.OrderHistoryDTO;
import com.thanh.foodorder.dto.response.order.OrderHistoryProjection;
import com.thanh.foodorder.dto.response.order.OrderItemDTO;
import com.thanh.foodorder.dto.response.order.OrderResponseDTO;
import com.thanh.foodorder.enums.OrderStatus;
import com.thanh.foodorder.enums.PaymentStatus;
import com.thanh.foodorder.enums.TableStatus;
import com.thanh.foodorder.repository.CartDetailRepository;
import com.thanh.foodorder.repository.CartRepository;
import com.thanh.foodorder.repository.OrderDetailRepository;
import com.thanh.foodorder.repository.OrderRepository;
import com.thanh.foodorder.specification.OrderSpecification;
import com.thanh.foodorder.util.JwtUtil;
import com.thanh.foodorder.util.OrderPaidEvent;
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

    private double caculateTotalPrice(List<CartDetail> cartDetails) {
        double totalPrice = 0;
        for (CartDetail cd : cartDetails) {
            totalPrice += cd.getQuantity() * cd.getPrice();
        }
        return totalPrice;
    }

    public CheckOutResponseDTO handleCheckOut(CheckoutRequestDTO dto, User curUser) {

        BookingTable bookingTable = bookingTableService.getTableById(dto.getTableId());
        List<CartDetail> cartDetails = cartDetailRepository.findByIdIn(dto.getCartDetailIds());

        // 1. Validate
        validBeforePlaceOrder(dto, curUser, cartDetails, bookingTable);

        // 2. Caculate price
        double totalPrice = caculateTotalPrice(cartDetails);
        double discount = 0;
        double finalPrice = totalPrice;

        // 3. If have an voucher then CHECK
        if (dto.getVoucherCode() != null) {
            Voucher voucher = voucherService.getVoucherByCode(dto.getVoucherCode());
            voucherService.checkVoucherBeforeApply(voucher, curUser);

            double discountByPercent = totalPrice * voucher.getPercentDiscount() / 100;
            discount = Math.min(discountByPercent, voucher.getMaxDiscount());
            finalPrice = totalPrice - discount;
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

    @Transactional
    public OrderResponseDTO placeOrder(CheckoutRequestDTO dto, User curUser) {

        BookingTable bookingTable = bookingTableService.getTableById(dto.getTableId());
        List<CartDetail> cartDetails = cartDetailRepository.findByIdIn(dto.getCartDetailIds());

        // 1. Validate again
        validBeforePlaceOrder(dto, curUser, cartDetails, bookingTable);

        // 2. Caculate price
        double totalPrice = caculateTotalPrice(cartDetails);

        Voucher voucher = null;
        double discount = 0;

        if (dto.getVoucherCode() != null) {
            voucher = voucherService.getVoucherByCode(dto.getVoucherCode());
            voucherService.checkVoucherBeforeApply(voucher, curUser);

            double discountByPercent = totalPrice * voucher.getPercentDiscount() / 100;
            discount = Math.min(discountByPercent, voucher.getMaxDiscount());

            // Update voucher usage
            voucher.setUsageLimit(voucher.getUsageLimit() - 1);
        }

        // 3. Create Order
        Order order = new Order();
        order.setUser(curUser);
        order.setOrderDate(LocalDateTime.now());
        order.setTotalPrice(totalPrice);
        order.setDiscount(discount);
        order.setOrderStatus(OrderStatus.PENDING);
        order.setPaymentStatus(PaymentStatus.UNPAID);
        order.setBookingTable(bookingTable);
        order.setVoucher(voucher);
        order.setNote(dto.getNote() != null ? dto.getNote() : "");

        Order orderSaved = orderRepository.save(order);
        List<OrderDetail> orderDetailsToSave = new ArrayList<>();
        // 4. Create OrderDetail
        for (CartDetail cd : cartDetails) {
            OrderDetail od = new OrderDetail();
            od.setOrder(orderSaved);
            od.setProduct(cd.getProduct());
            od.setQuantity(cd.getQuantity());
            od.setPrice(cd.getPrice());
            od.setNote(dto.getNote());

            orderDetailRepository.save(od);
            // Update inventory
            Product p = cd.getProduct();
            p.setQuantity(p.getQuantity() - cd.getQuantity());
            orderDetailsToSave.add(od);
        }
        List<OrderDetail> savedOrderDetails = orderDetailRepository.saveAll(orderDetailsToSave);
        orderSaved.setOrderDetails(savedOrderDetails);

        // 5. Change table status
        bookingTable.setTableStatus(TableStatus.RESERVED);

        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(order.getId());

        return mapToOrderResponseDTO(order, orderDetails);
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
        double totalPrice = product.getPrice() * req.getQuantity();

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

    // @Transactional
    // public void payOrder(Long id, Double amount) {
    // Order order = getOrderById(id);
    // List<Long> ids = order.getOrderDetails()
    // .stream()
    // .map(OrderDetail::getId)
    // .toList();

    // List<CartDetail> cartDetails = cartDetailRepository.findByIdIn(ids);
    // // 1. Không cho thanh toán lại
    // if (order.getPaymentStatus() == PaymentStatus.PAID) {
    // throw new CommonException("Order already paid");
    // }
    // if (order.getTotalPrice() != amount) {
    // if (!order.getTotalPrice().equals(amount.doubleValue())) {
    // throw new CommonException("Price is not correct");
    // }
    // }

    // // 2. Validate trạng thái order
    // if (order.getOrderStatus() == OrderStatus.CANCELLED) {
    // throw new CommonException("Cannot pay cancelled order");
    // }

    // // 3. Thanh toán thành công
    // order.setPaymentStatus(PaymentStatus.PAID);
    // order.setOrderStatus(OrderStatus.PENDING);

    // // 6. Delete cart

    // String subject = "Hóa đơn đơn hàng #" + order.getId() + " - Food Order";
    // String htmlContent = buildInvoiceHtml(order);
    // this.emailService.sendOrderInvoiceEmail("huuthanhht05@gmail.com", subject,
    // htmlContent);
    // cartDetailRepository.deleteAll(cartDetails);

    // Map<String, String> payload = new HashMap<>();
    // payload.put("status", "PAID");
    // simpMessagingTemplate.convertAndSend(
    // "/topic/order/" + order.getId(),
    // payload // Spring Boot sẽ tự convert Map này thành {"status":"PAID"}
    // );
    // }
    @Transactional
    public void payOrder(Long id, Double amount) {

        Order order = getOrderById(id);

        validatePayment(order, amount);

        order.setPaymentStatus(PaymentStatus.PAID);
        order.setOrderStatus(OrderStatus.PENDING);

        clearCart(order);

        eventPublisher.publishEvent(new OrderPaidEvent(order));
    }

    private void clearCart(Order order) {

        List<Long> ids = order.getOrderDetails()
                .stream()
                .map(OrderDetail::getId)
                .toList();

        List<CartDetail> cartDetails = cartDetailRepository.findByIdIn(ids);

        cartDetailRepository.deleteAll(cartDetails);

    }

    private void validatePayment(Order order, Double amount) {

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new CommonException("Order already paid");
        }

        if (!Objects.equals(order.getTotalPrice(), amount)) {
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
