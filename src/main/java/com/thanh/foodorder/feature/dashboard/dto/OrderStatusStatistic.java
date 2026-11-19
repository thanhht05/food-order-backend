package com.thanh.foodorder.feature.dashboard.dto;
import com.thanh.foodorder.feature.order.enums.OrderStatus;


public record OrderStatusStatistic(OrderStatus orderStatus,
                Long count) {

}
