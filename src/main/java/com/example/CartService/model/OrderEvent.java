package com.example.CartService.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderEvent {
    private Long orderId;
    private Long productId;
    private Long customerId;
    private Integer quantity;
    private BigDecimal totalPrice;
    private String orderStatus;
    private LocalDateTime orderDate;
}
