package com.thanh.foodorder.dto.statistic;

import java.math.BigDecimal;

public record RevenueByMonthResponse(Integer month,
        BigDecimal revenue) {

}
