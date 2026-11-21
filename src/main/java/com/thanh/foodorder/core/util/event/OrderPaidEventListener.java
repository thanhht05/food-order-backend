package com.thanh.foodorder.core.util.event;

import java.text.NumberFormat;
import java.util.Locale;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;


import lombok.RequiredArgsConstructor;
import com.thanh.foodorder.feature.order.domain.Order;
import com.thanh.foodorder.feature.order.domain.OrderDetail;
import com.thanh.foodorder.feature.order.service.EmailService;

// Listen for order payment event
@Component
@RequiredArgsConstructor
public class OrderPaidEventListener {
    private final SimpMessagingTemplate messagingTemplate;
    private final EmailService emailService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(OrderPaidEvent event) {

        Order order = event.getOrder();

        sendEmail(order);

        // notifyCustomer(order);

        // notifyAdmin(order);
    }

    
    // private void notifyCustomer(Order order) {

    // Map<String, String> payload = Map.of(
    // "status", "PAID");

    // messagingTemplate.convertAndSend(
    // "/topic/order/" + order.getId(),
    // payload);
    // }

    // Notify admin of newly paid order
    // private void notifyAdmin(Order order) {

    // AdminOrderResponseDTO response = AdminOrderResponseDTO.from(order);

    // messagingTemplate.convertAndSend(
    // "/topic/admin/orders",
    // response);
    // }

    private void sendEmail(Order order) {

        String subject = "Invoice #" + order.getId();

        String html = buildInvoiceHtml(order);

        emailService.sendOrderInvoiceEmail(
                order.getUser().getEmail(),
                subject,
                html);
    }

    private String buildInvoiceHtml(Order order) {
        StringBuilder itemsHtml = new StringBuilder();

        // Iterate through ordered items to build table rows
        // in HTML invoice table
        for (OrderDetail item : order.getOrderDetails()) {
            itemsHtml.append("<tr>")
                    .append("<td style='padding: 8px; border-bottom: 1px solid #ddd;'>")
                    .append(item.getProduct().getName())
                    .append("</td>")
                    .append("<td style='padding: 8px; border-bottom: 1px solid #ddd; text-align: center;'>")
                    .append(item.getQuantity()).append("</td>")
                    .append("<td style='padding: 8px; border-bottom: 1px solid #ddd; text-align: right;'>")
                    .append(NumberFormat.getInstance(new Locale("vi", "VN"))
                            .format(order.getTotalPrice()))
                    .append("đ</td>")
                    .append("</tr>");
        }

        // HTML email invoice template
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
                + NumberFormat.getInstance(new Locale("vi", "VN"))
                        .format(order.getTotalPrice())
                + "đ</h3>"
                + "  <p style='margin-top: 30px; font-size: 12px; color: #777; text-align: center;'>Nếu có bất kỳ thắc mắc nào, vui lòng liên hệ hotline 0898173004"
                + "</div>";
    }
}
