package com.example.CartService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository repository;
    private final CartRepository cartRepository;
    private final WebClient.Builder webClientBuilder;

    public Mono<Cart> addToCart(Cart cart) {
        return repository.save(cart)
                .doOnSuccess(savedCustomer ->
                        System.out.println("Cart saved with ID: " + savedCustomer.getId()));
    }

    public Flux<Cart> getAllCartDetails(){
        return repository.findAll();
    }
    public Mono<CartResponse> getCartByCustomerId(Long customerId) {
        return repository.findByCustomerId(customerId)
                .flatMap(cart -> webClientBuilder.build()
                        .get()
                        .uri("http://localhost:8081/products/{id}", cart.getProductId())
                        .retrieve()
                        .bodyToMono(Product.class)
                        .map(product -> new CartResponse(cart, product))
                );
}
}