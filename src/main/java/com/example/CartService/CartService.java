package com.example.CartService;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartService.class);

    private final CartRepository repository;
    private final WebClient.Builder webClientBuilder;

    private static final String CIRCUIT_BREAKER_NAME = "productService";
    private static final String RETRY_NAME = "productServiceRetry";

    // Add Cart to Repository
    public Mono<Cart> addToCart(Cart cart) {
        return repository.save(cart)
                .doOnSuccess(savedCart -> logger.info("Cart saved with ID: {}", savedCart.getId()))
                .doOnError(error -> logger.error("Error saving cart", error));
    }

    // Fetch all Cart Details
    public Flux<Cart> getAllCartDetails() {
        return repository.findAll()
                .doOnComplete(() -> logger.info("Fetched all cart details"))
                .doOnError(error -> logger.error("Error fetching cart details", error));
    }

    // Get Cart details by Customer ID with Circuit Breaker & Retry
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "getCartFallback")
    @Retry(name = RETRY_NAME)
    public Mono<CartResponse> getCartByCustomerId(Long customerId) {
        logger.info("Fetching cart details for customer ID: {}", customerId);
        return repository.findByCustomerId(customerId)
                .flatMap(cart -> fetchProductWithCircuitBreaker(cart.getProductId())
                        .map(product -> new CartResponse(cart, product)))
                .doOnError(error -> logger.error("Error fetching cart details for customer ID: {}", customerId, error));
    }

    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "fallbackProductResponse")
    @Retry(name = RETRY_NAME)
    public Mono<Product> fetchProductWithCircuitBreaker(Long productId) {
        logger.info("Fetching product details for product ID: {}", productId);
        return webClientBuilder.build()
                .get()
                .uri("http://localhost:8081/products/{id}", productId)
                .retrieve()
                .bodyToMono(Product.class)
                .doOnSuccess(product -> {
                    logger.info("Successfully fetched and cached product ID: {}", productId);
                })
                .onErrorResume(throwable -> {
                    logger.error("Error calling Product Service for ID: {} - {}", productId, throwable.getMessage());
                    return fallbackProductResponse(productId, throwable);
                });
    }

    private Mono<CartResponse> getCartFallback(Long customerId, Throwable throwable) {
        logger.warn("Circuit Breaker Activated for Cart Service! Returning default cart response for customer ID: {}", customerId);
        return Mono.just(new CartResponse(null, null)); // Returning null for product
    }

    private Mono<Product> fallbackProductResponse(Long productId, Throwable throwable) {
        logger.warn("Product Service is down! Returning default product for ID: {}", productId);
        return Mono.just(new Product(productId, "Product is currently unavailable", BigDecimal.ZERO, true));
    }
}
