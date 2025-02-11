package com.example.CartService.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "carts")
@Data
public class Cart {
    @Id
    private Long id;
    private Long customerId;
    private Long productId;
    private Integer quantity;
}