package com.thanh.foodorder.util;

import java.util.Map;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.thanh.foodorder.domain.Order;
import com.thanh.foodorder.domain.OrderDetail;
import com.thanh.foodorder.dto.response.order.AdminOrderResponseDTO;
import com.thanh.foodorder.service.EmailService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderPaidEventListener {
    private final SimpMessagingTemplate messagingTemplate;
    private final EmailService emailService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(OrderPaidEvent event) {

        Order order = event.getOrder();

        sendEmail(order);

        notifyCustomer(order);

        notifyAdmin(order);
    }

    private void notifyCustomer(Order order) {

        Map<String, String> payload = Map.of(
                "status", "PAID");

        messagingTemplate.convertAndSend(
                "/topic/order/" + order.getId(),
                payload);
    }

    private void notifyAdmin(Order order) {

        AdminOrderResponseDTO response = AdminOrderResponseDTO.from(order);

        messagingTemplate.convertAndSend(
                "/topic/admin/orders",
                response);
    }

    private void sendEmail(Order order) {

        String subject = "Hóa đơn #" + order.getId();

        String html = buildInvoiceHtml(order);

        emailService.sendOrderInvoiceEmail(
                order.getUser().getEmail(),
                subject,
                html);
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
