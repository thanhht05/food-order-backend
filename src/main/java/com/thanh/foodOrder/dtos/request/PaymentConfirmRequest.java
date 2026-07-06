package com.thanh.foodOrder.dtos.request;

public class PaymentConfirmRequest {
    private Long orderId;

    private Double amount;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

}
