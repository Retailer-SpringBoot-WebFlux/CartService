package com.example.CartService.service;

import com.example.CartService.model.OrderEvent;
import com.example.CartService.repository.CartRepository;
import com.example.CartService.model.Cart;
import com.example.CartService.model.CartResponse;
import com.example.CartService.model.Product;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
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
    private final ReactiveRedisTemplate<String, Product> redisTemplate;

    private static final String CIRCUIT_BREAKER_NAME = "productService";
    private static final String RETRY_NAME = "productServiceRetry";

    public Mono<Cart> addToCart(Cart cart) {
        return repository.save(cart)
                .doOnSuccess(savedCart -> logger.info("Cart saved with ID: {}", savedCart.getId()))
                .doOnError(error -> logger.error("Error saving cart", error));
    }

    public Flux<Cart> getAllCartDetails() {
        return repository.findAll()
                .doOnComplete(() -> logger.info("Fetched all cart details"))
                .doOnError(error -> logger.error("Error fetching cart details", error));
    }


    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "getCartFallback")
    @Retry(name = RETRY_NAME)
    public Mono<CartResponse> getCartByCustomerId(Long customerId) {
        logger.info("Fetching cart details for customer ID: {}", customerId);
        return repository.findByCustomerId(customerId)
                .flatMap(cart -> fetchProductWithCache(cart.getProductId())
                        .map(product -> new CartResponse(cart, product)))
                .doOnError(error -> logger.error("Error fetching cart details for customer ID: {}",
                        customerId, error));
    }

    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "fallbackProductResponse")
    @Retry(name = RETRY_NAME)
    public Mono<Product> fetchProductWithCache(Long productId) {
        // First, try to get the product from Redis cache
        logger.info("Checking Redis cache for product ID: {}", productId);
        return redisTemplate.opsForValue().get("product:" + productId)
                .doOnNext(product -> logger.info("Product found in cache for product ID: {}", productId))
                .switchIfEmpty(fetchProductFromServiceAndCache(productId))
                .doOnError(error -> logger.error("Error retrieving product from cache for product ID: {}",
                        productId, error));
    }

    private Mono<Product> fetchProductFromServiceAndCache(Long productId) {
        logger.info("Fetching product details for product ID: {}", productId);

        return webClientBuilder.build()
                .get()
                .uri("lb://APIGATEWAY/products/{id}", productId)
                .retrieve()
                .bodyToMono(Product.class)
                .flatMap(product -> redisTemplate.opsForValue().set("product:" + productId, product)
                        .thenReturn(product))
                .doOnSuccess(product -> logger.info("Successfully fetched and cached product ID: {}", productId))
                .onErrorResume(throwable -> {
                    logger.error("Error calling Product Service for ID: {} - {}", productId, throwable.getMessage());
                    return fallbackProductResponse(productId, throwable);
                });
    }

    private Mono<CartResponse> getCartFallback(Long customerId, Throwable throwable) {
        logger.warn("Circuit Breaker Activated for Cart Service! Returning default cart response for customer ID: {}",
                customerId);
        return Mono.just(new CartResponse(null, null)); // Returning null for product
    }

    private Mono<Product> fallbackProductResponse(Long productId, Throwable throwable) {
        logger.warn("Product Service is down! Returning default product for ID: {}", productId);
        return Mono.just(new Product(productId, "Product is currently unavailable", BigDecimal.ZERO,
                true));
    }
    public Mono<Void> removeCartItem(Long customerId, Long productId) {
        return repository.findByCustomerIdAndProductId(customerId, productId)
                .flatMap(cartItem -> repository.delete(cartItem))
                .doOnSuccess(removed -> System.out.println("Cart item removed for customer: " +
                        "" + customerId))
                .then();
    }

    @KafkaListener(topics = "order-topic", groupId = "cart-group")
    public void consumeOrderEvent(OrderEvent event) {
        System.out.println("Removing items from cart for Order ID: " + event.getProductId());

        removeCartItem(event.getCustomerId(), event.getProductId())
                .doOnSuccess(removed -> System.out.println("Item removed from cart"))
                .doOnError(error -> System.err.println("Failed to remove item from cart: "
                        + error.getMessage()))
                .subscribe();
    }


}
