package com.thanh.foodorder.feature.dashboard.dto;

import java.math.BigDecimal;

public record RevenueByMonthResponse(Integer month,
        BigDecimal revenue) {

}
