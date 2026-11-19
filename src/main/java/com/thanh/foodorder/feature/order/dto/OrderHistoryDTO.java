package com.thanh.foodorder.feature.order.dto;

import java.time.Instant;
import java.util.List;


import lombok.Getter;
import lombok.Setter;
import com.thanh.foodorder.feature.order.enums.OrderStatus;
import com.thanh.foodorder.feature.order.enums.PaymentStatus;

@Getter
@Setter
public class OrderHistoryDTO {

    private long userId;
    private String fullName;
    private long cartId;

    private List<OrderInfo> orderInfo;

    @Getter
    @Setter
    public static class OrderInfo {

        private long orderId;
        private Instant orderDate;
        private OrderStatus orderStatus;
        private double totalPrice;
        private String paymentMethod;
        private PaymentStatus paymentStatus;

        private String phone;
        private String province;
        private String recipientName;
        private String ward;
        private String addressDetail;
        private String paymentLinkId;

        private List<ProductInsideOrder> products;
    }

    @Getter
    @Setter
    public static class ProductInsideOrder {

        private long productId;
        private String productName;
        private double price;
        private long quantity;
        private String img;
    }
}