package com.thanh.foodOrder.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.catalina.security.SecurityUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thanh.foodOrder.domain.BookingTable;
import com.thanh.foodOrder.domain.CartDetail;
import com.thanh.foodOrder.domain.Order;
import com.thanh.foodOrder.domain.OrderDetail;
import com.thanh.foodOrder.domain.Product;
import com.thanh.foodOrder.domain.User;
import com.thanh.foodOrder.domain.Voucher;
import com.thanh.foodOrder.dtos.request.CheckoutRequestDTO;
import com.thanh.foodOrder.dtos.response.CheckOutResponseDTO;
import com.thanh.foodOrder.dtos.response.order.AdminOrderResponseDTO;
import com.thanh.foodOrder.dtos.response.order.OrderHistoryDTO;
import com.thanh.foodOrder.dtos.response.order.OrderHistoryProjection;
import com.thanh.foodOrder.dtos.response.order.OrderItemDTO;
import com.thanh.foodOrder.dtos.response.order.OrderResponseDTO;
import com.thanh.foodOrder.enums.OrderStatus;
import com.thanh.foodOrder.enums.PaymentStatus;
import com.thanh.foodOrder.enums.TableStatus;
import com.thanh.foodOrder.repository.CartDetailRepository;
import com.thanh.foodOrder.repository.CartRepository;
import com.thanh.foodOrder.repository.OrderDetailRepository;
import com.thanh.foodOrder.repository.OrderRepository;
import com.thanh.foodOrder.util.JwtUtil;
import com.thanh.foodOrder.util.exception.CommonException;

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

    public OrderService(OrderRepository orderRepository, CartDetailRepository cartDetailRepository,
            OrderDetailRepository orderDetailRepository, BookingTableService bookingTableService,
            ProductService productService, VoucherService voucherService, UserService userService) {
        this.orderRepository = orderRepository;
        this.voucherService = voucherService;
        this.bookingTableService = bookingTableService;
        this.cartDetailRepository = cartDetailRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.productService = productService;
        this.userService = userService;

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
        AdminOrderResponseDTO res = new AdminOrderResponseDTO();
        res.setOrderId(id);
        res.setOrderDate(order.getOrderDate());
        res.setStatus(order.getOrderStatus().name());
        res.setTotalPrice(order.getTotalPrice());
        res.setDiscount(order.getDiscount());
        res.setTableId(order.getBookingTable().getId());
        res.setPaymentStatus(order.getPaymentStatus());

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

        orderRepository.save(order);

        // 4. Create OrderDetail
        for (CartDetail cd : cartDetails) {
            OrderDetail od = new OrderDetail();
            od.setOrder(order);
            od.setProduct(cd.getProduct());
            od.setQuantity(cd.getQuantity());
            od.setPrice(cd.getPrice());
            od.setNote(dto.getNote());

            orderDetailRepository.save(od);
            // Update inventory
            Product p = cd.getProduct();
            p.setQuantity(p.getQuantity() - cd.getQuantity());
        }

        // 5. Change table status
        bookingTable.setTableStatus(TableStatus.RESERVED);

        // 6. Delete cart
        cartDetailRepository.deleteAll(cartDetails);

        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(order.getId());

        return mapToOrderResponseDTO(order, orderDetails);
    }

    @Transactional
    public void payOrder(Long id) {
        Order order = getOrderById(id);

        // 1. Không cho thanh toán lại
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new CommonException("Order already paid");
        }

        // 2. Validate trạng thái order
        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new CommonException("Cannot pay cancelled order");
        }

        // 3. Thanh toán thành công
        order.setPaymentStatus(PaymentStatus.PAID);
        order.setOrderStatus(OrderStatus.COMPLETED);

        // 4. Trả bàn
        BookingTable table = order.getBookingTable();
        table.setTableStatus(TableStatus.AVAILABLE);
    }

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
            response.setInfoOrders(new ArrayList<>());
            return response;
        }

        Map<Long, OrderHistoryDTO.InfoOrder> orderMap = new LinkedHashMap<>();

        for (OrderHistoryProjection row : rows) {

            OrderHistoryDTO.InfoOrder order = orderMap.computeIfAbsent(
                    row.getOrderId(),
                    id -> {

                        OrderHistoryDTO.InfoOrder dto = new OrderHistoryDTO.InfoOrder();

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

        response.setInfoOrders(
                new ArrayList<>(orderMap.values()));

        return response;
    }

}
