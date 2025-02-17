package com.example.CartService.repository;

import com.example.CartService.model.Cart;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface CartRepository extends R2dbcRepository<Cart, Long> {
    Mono<Cart> findByCustomerId(Long customerId);
    @Query("SELECT * FROM carts WHERE customer_id = :customerId AND product_id = :productId LIMIT 1")
    Mono<Cart> findByCustomerIdAndProductId(Long customerId, Long productId);
}