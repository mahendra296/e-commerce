# Order Service Microservice

A RESTful microservice for managing customer orders built with Java 25, Spring Boot 3.3, and Maven.

## 🏗️ Architecture

### Package Structure
```
com.mestro
├── controller      # REST API endpoints
├── service         # Business logic
├── repository      # Data access layer
├── model           # JPA entities
├── dto             # Data Transfer Objects
├── config          # Configuration classes
├── exceptions      # Custom exceptions and global handler
└── enums           # Enumerations
```

## 📋 API Endpoints

### Order Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/orders` | Create a new order |
| GET | `/orders/{orderId}` | Get order by ID |
| GET | `/orders` | Get all orders |
| GET | `/orders/customer/{customerId}` | Get orders by customer |
| GET | `/orders/status/{status}` | Get orders by status |
| GET | `/orders/date-range?startDate=&endDate=` | Get orders by date range |
| PUT | `/orders/{orderId}` | Update order |
| PATCH | `/orders/{orderId}/status?status=` | Update order status |
| DELETE | `/orders/{orderId}` | Delete order |
| GET | `/orders/customer/{customerId}/count` | Get order count by customer |

### Order Statuses
- `PENDING` - Order created but not confirmed
- `CONFIRMED` - Order confirmed by customer
- `PROCESSING` - Order is being processed
- `SHIPPED` - Order has been shipped
- `DELIVERED` - Order delivered to customer
- `CANCELLED` - Order cancelled
- `RETURNED` - Order returned by customer

## 📝 Sample cURL Commands

### Order Endpoints

#### Create a new order
```bash
curl --location '{{baseURL1}}/api/v1/orders' \
--header 'Content-Type: application/json' \
--data '{
    "customerId": 1,
    "shippingAddress": "123 Main Street, New York, NY 10001",
    "billingAddress": "123 Main Street, New York, NY 10001",
    "notes": "Please deliver before noon",
    "orderItems": [
        {
            "productId": 101,
            "productName": "Wireless Mouse",
            "quantity": 2,
            "unitPrice": 29.99
        },
        {
            "productId": 102,
            "productName": "USB Keyboard",
            "quantity": 1,
            "unitPrice": 49.99
        }
    ]
}'
```

#### Get order by ID
```bash
curl --location '{{baseURL1}}/api/v1/orders/1'
```

#### Get all orders
```bash
curl --location '{{baseURL1}}/api/v1/orders'
```

#### Get orders by customer ID
```bash
curl --location '{{baseURL1}}/api/v1/orders/customer/1'
```

#### Get orders by status
```bash
curl --location '{{baseURL1}}/api/v1/orders/status/PENDING'
```

#### Get orders by date range
```bash
curl --location '{{baseURL1}}/api/v1/orders/date-range?startDate=2025-01-01T00:00:00&endDate=2025-12-31T23:59:59'
```

#### Update order
```bash
curl --location --request PUT '{{baseURL1}}/api/v1/orders/1' \
--header 'Content-Type: application/json' \
--data '{
    "customerId": 1,
    "shippingAddress": "456 Updated Ave, San Francisco, CA 94105",
    "billingAddress": "456 Updated Ave, San Francisco, CA 94105",
    "notes": "Updated delivery instructions",
    "orderItems": [
        {
            "productId": 101,
            "productName": "Wireless Mouse",
            "quantity": 3,
            "unitPrice": 29.99
        }
    ]
}'
```

#### Update order status
```bash
curl --location --request PATCH '{{baseURL1}}/api/v1/orders/1/status?status=CONFIRMED'
```

#### Delete order
```bash
curl --location --request DELETE '{{baseURL1}}/api/v1/orders/1'
```

#### Get order count by customer
```bash
curl --location '{{baseURL1}}/api/v1/orders/customer/1/count'
```


## 🐳 Running with Docker

### Prerequisites
- Docker installed and running

### 1. Create a Docker network
```bash
docker network create order-network
```

### 2. Start PostgreSQL
```bash
docker run -d \
  --name orderdb \
  --network order-network \
  -e POSTGRES_DB=orderdb \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=root \
  -p 5432:5432 \
  postgres:17
```

### 3. Build the application image
```bash
docker build -t order-service .
```

### 4. Run the application
```bash
docker run -d \
  --name order-service \
  --network order-network \
  -e DB_HOST=orderdb \
  -e DB_PORT=5432 \
  -e DB_NAME=orderdb \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=root \
  -p 8082:8082 \
  order-service
```

### 5. Verify
- API: http://localhost:8082/api/v1/orders
- Swagger UI: http://localhost:8082/swagger-ui/index.html

### Stop and clean up
```bash
docker stop order-service orderdb
docker rm order-service orderdb
docker network rm order-network
```

## 🔧 Configuration

### Database Configuration
Edit `src/main/resources/application.yml`:
