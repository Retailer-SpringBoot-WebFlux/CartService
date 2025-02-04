package com.example.CartService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/carts")
public class CartController {
    private final CartService service;
    
    public CartController(CartService service) {
        this.service = service;
    }
    
    @PostMapping
    public Mono<ResponseEntity<Cart>> addToCart(@RequestBody Cart cart) {
        return service.addToCart(cart)
                .map(ResponseEntity::ok);
    }
    @GetMapping
    public Flux<Cart> getAllOrders() {
        return service.getAllCartDetails();
    }
    @GetMapping("/{customerId}")
    public Mono<ResponseEntity<CartResponse>> getCartByCustomerId(@PathVariable Long customerId) {
        return service.getCartByCustomerId(customerId)
                .map(cartResponse -> ResponseEntity.ok(cartResponse))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}