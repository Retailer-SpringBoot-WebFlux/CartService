package com.example.CartService;

import java.time.Instant;

public class CachedProduct {
        private final Product product;
        private final Instant timestamp;

        public CachedProduct(Product product, Instant timestamp) {
            this.product = product;
            this.timestamp = timestamp;
        }

        public Product getProduct() {
            return product;
        }

        public Instant getTimestamp() {
            return timestamp;
        }
    }