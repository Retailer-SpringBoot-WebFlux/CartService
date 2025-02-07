package com.example.CartService;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    @LoadBalanced  // if I use this then only ProductService Invoking because we are using APIGateWay functionality so..
    public WebClient.Builder webClientBuilder() {

        return WebClient.builder();
    }
}
