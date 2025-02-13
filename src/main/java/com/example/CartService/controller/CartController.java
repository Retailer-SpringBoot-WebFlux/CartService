package com.example.CartService.controller;

import com.example.CartService.model.Cart;
import com.example.CartService.model.CartResponse;
import com.example.CartService.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

@RestController
@RequestMapping("/carts")
@RequiredArgsConstructor
@Slf4j // Enables structured logging
public class CartController {
    private final CartService service;

    @PostMapping
    public Mono<ResponseEntity<Cart>> addToCart(@RequestBody Cart cart) {
        log.info("Received request to add cart: {}", cart);
        return service.addToCart(cart)
                .doOnSuccess(savedCart -> log.info("Cart added successfully: {}", savedCart))
                .doOnError(error -> log.error("Error adding cart", error))
                .map(ResponseEntity::ok);
    }

    @GetMapping
    public Flux<Cart> getAllOrders() {
        log.info("Fetching all cart details...");
        return service.getAllCartDetails()
                .doOnComplete(() -> log.info("Successfully fetched all cart details"))
                .switchIfEmpty(s->ResponseEntity.notFound().build())
                .doOnError(error -> log.error("Error fetching cart details", error));
    }

    @GetMapping("/{customerId}")
    public Mono<ResponseEntity<CartResponse>> getCartByCustomerId(@PathVariable Long customerId) {
        log.info("Fetching cart for customer ID: {}", customerId);
        return service.getCartByCustomerId(customerId)
                .doOnSuccess(cartResponse -> Optional.ofNullable(cartResponse)
                        .ifPresentOrElse(
                                response -> log.info("Cart retrieved successfully for customer ID: {}", customerId),
                                () -> log.warn("No cart found for customer ID: {}", customerId)
                        ))
                .doOnError(error -> log.error("Error fetching cart for customer ID {}", customerId, error))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
