package com.thanh.foodorder.core.util.event;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import com.thanh.foodorder.feature.order.domain.Order;

@Getter
@Setter
@AllArgsConstructor
public class OrderPaidEvent {
    private final Order order;

}
