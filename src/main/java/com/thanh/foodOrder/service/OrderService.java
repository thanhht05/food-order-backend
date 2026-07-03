package com.thanh.foodOrder.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.catalina.security.SecurityUtil;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thanh.foodOrder.domain.BookingTable;
import com.thanh.foodOrder.domain.CartDetail;
import com.thanh.foodOrder.domain.Order;
import com.thanh.foodOrder.domain.OrderDetail;
import com.thanh.foodOrder.domain.Product;
import com.thanh.foodOrder.domain.User;
import com.thanh.foodOrder.domain.Voucher;
import com.thanh.foodOrder.dtos.request.BuyNowRequestDTO;
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
import com.thanh.foodOrder.specification.OrderSpecification;
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

    private final EmailService emailService;

    public OrderService(OrderRepository orderRepository, CartDetailRepository cartDetailRepository,
            OrderDetailRepository orderDetailRepository, BookingTableService bookingTableService,
            ProductService productService, VoucherService voucherService, UserService userService,
            EmailService emailService) {
        this.orderRepository = orderRepository;
        this.voucherService = voucherService;
        this.bookingTableService = bookingTableService;
        this.cartDetailRepository = cartDetailRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.productService = productService;
        this.userService = userService;
        this.emailService = emailService;

    }

    public List<AdminOrderResponseDTO> getAllOrder(OrderStatus orderStatus) {
        Specification<Order> spec = Specification.allOf(OrderSpecification.hasStatus(orderStatus));
        List<Order> lstOrders = this.orderRepository.findAll(spec);
        List<AdminOrderResponseDTO> res = new ArrayList<>();

        for (Order od : lstOrders) {
            AdminOrderResponseDTO item = new AdminOrderResponseDTO();

            item.setOrderId(od.getId());
            item.setOrderDate(od.getOrderDate());
            item.setStatus(od.getOrderStatus().name());
            item.setTotalPrice(od.getTotalPrice());
            item.setDiscount(od.getDiscount());
            item.setTableId(od.getBookingTable().getId());
            item.setPaymentStatus(od.getPaymentStatus());

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

        // 6. Delete cart
        String subject = "Hóa đơn đơn hàng #" + orderSaved.getId() + " - Food Order";
        String htmlContent = buildInvoiceHtml(orderSaved);
        this.emailService.sendOrderInvoiceEmail("huuthanhht05@gmail.com", subject, htmlContent);
        cartDetailRepository.deleteAll(cartDetails);

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

    private String buildInvoiceHtml(Order order) {
        StringBuilder itemsHtml = new StringBuilder();

        // Vòng lặp duyệt qua danh sách các món ăn khách đặt để build thành các dòng
        // trong bảng <tr>
        for (OrderDetail item : order.getOrderDetails()) {
            itemsHtml.append("<tr>")
                    .append("<td style='padding: 8px; border-bottom: 1px solid #ddd;'>")
                    .append(item.getProduct().getName())
                    .append("</td>")
                    .append("<td style='padding: 8px; border-bottom: 1px solid #ddd; text-align: center;'>")
                    .append(item.getQuantity()).append("</td>")
                    .append("<td style='padding: 8px; border-bottom: 1px solid #ddd; text-align: right;'>")
                    .append(item.getPrice()).append("đ</td>")
                    .append("</tr>");
        }

        // Toàn bộ khung giao diện hóa đơn HTML
        return "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; border: 1px solid #eee; padding: 20px;'>"
                + "  <h2 style='color: #ff4757; text-align: center;'>CẢM ƠN BẠN ĐÃ ĐẶT HÀNG!</h2>"
                + "  <p>Xin chào <strong>" + order.getUser().getFullName() + "</strong>,</p>"
                + "  <p>Đơn hàng <strong>#" + order.getId()
                + "</strong> của bạn đã được tiếp nhận thành công. Dưới đây là thông tin chi tiết hóa đơn:</p>"
                + "  <table style='width: 100%; border-collapse: collapse; margin-top: 20px;'>"
                + "    <thead>"
                + "      <tr style='background-color: #f2f2f2;'>"
                + "        <th style='padding: 8px; text-align: left;'>Món ăn</th>"
                + "        <th style='padding: 8px; text-align: center;'>SL</th>"
                + "        <th style='padding: 8px; text-align: right;'>Giá</th>"
                + "      </tr>"
                + "    </thead>"
                + "    <tbody>"
                + itemsHtml.toString()
                + "    </tbody>"
                + "  </table>"
                + "  <h3 style='text-align: right; margin-top: 20px; color: #ff4757;'>Tổng thanh toán: "
                + order.getTotalPrice() + "đ</h3>"
                + "  <p style='margin-top: 30px; font-size: 12px; color: #777; text-align: center;'>Nếu có bất kỳ thắc mắc nào, vui lòng liên hệ hotline 1900xxxx. Chúc bạn ngon miệng!</p>"
                + "</div>";
    }
}
