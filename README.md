# Retailer Microservices (Spring WebFlux + JPA + MySQL)

## 📌 Overview
This project consists of four microservices:
- **Product Service**: Manages products
- **Order Service**: Handles customer orders
- **Customer Service**: Stores customer details
- **Cart Service**: Manages the shopping cart

## 🚀 Tech Stack
- **Spring Boot 3.2**
- **Spring WebFlux + WebClient**
- **Spring JPA + MySQL**
- **Gradle** as the build tool

## 🛠 Setup Instructions

### 1️⃣ Clone the Repository
```bash
git clone https://github.com/your-username/retailer-microservices.git
cd retailer-microservices
CREATE DATABASE retailer_db;
USE retailer_db;

-- Product Table
CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(10,2) NOT NULL
);

-- Customer Table
CREATE TABLE customers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL
);

-- Order Table
CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (customer_id) REFERENCES customers(id)
);

-- Cart Table
CREATE TABLE carts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES customers(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
);

//workflow
Microservices Communication
Each service communicates using WebClient:

Order Service → Product Service (to fetch product details)
Cart Service → Product & Customer Service (to validate cart items)


Redis Configuration.
Run the local installed Radis 
   ->radis-server
   ->radis-cli monitoring logs.
   ->radis-stop

properties file 
JAVA_OPTS=-Xms256m -Xmx1024m
yml file
services:
  product-service:
    environment:
      - JAVA_OPTS=-Xms256m -Xmx1024m