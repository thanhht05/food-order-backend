package com.thanh.foodorder.dto.response;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckOutResponseDTO {
    private List<Long> cartDetailIds = new ArrayList<>(); // các dòng trong giỏ
    private Double totalPrice;
    private Double discount;
    private Double finalPrice;
    private Long tableId;
}
