package com.thanh.foodorder.dto.statistic;

import com.thanh.foodorder.enums.OrderStatus;

public record OrderStatusStatistic(OrderStatus orderStatus,
                Long count) {

}
