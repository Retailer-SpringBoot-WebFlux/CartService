package com.example.CartService.config;

import com.example.CartService.model.Product;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

@Configuration
public class RedisConfig {
    @Bean
    public ReactiveRedisTemplate<String, Product> reactiveRedisTemplate(ReactiveRedisConnectionFactory factory) {
        // Configure Jackson JSON serializer for Product objects
        Jackson2JsonRedisSerializer<Product> serializer = new Jackson2JsonRedisSerializer<>(Product.class);

        // Configure ObjectMapper for custom settings
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // Support Java 8 time API
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Build the serialization context for RedisTemplate
        RedisSerializationContext.RedisSerializationContextBuilder<String, Product> builder =
                RedisSerializationContext.newSerializationContext(new StringRedisSerializer());

        // Set the Jackson2JsonRedisSerializer for value serialization
        RedisSerializationContext<String, Product> context = builder.value(serializer).build();

        // Return the ReactiveRedisTemplate with the serialization context
        return new ReactiveRedisTemplate<>(factory, context);
    }
}
