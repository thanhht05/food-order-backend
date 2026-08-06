package com.thanh.foodorder.dto.response;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckOutResponseDTO {
    private List<Long> cartDetailIds = new ArrayList<>(); // các dòng trong giỏ
    private BigDecimal totalPrice;
    private BigDecimal discount;
    private BigDecimal finalPrice;
    private Long tableId;
}
