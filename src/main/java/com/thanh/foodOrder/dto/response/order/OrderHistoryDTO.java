package com.thanh.foodorder.dto.response.order;

import java.time.Instant;
import java.util.List;

import com.thanh.foodorder.enums.OrderStatus;

import lombok.Getter;
import lombok.Setter;

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
        private long tableId;
        private double totalPrice;

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