package com.example.CartService.repository;

import com.example.CartService.model.Cart;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface CartRepository extends R2dbcRepository<Cart, Long> {
    Mono<Cart> findByCustomerId(Long customerId);
}