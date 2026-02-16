# Order Service Microservice

A RESTful microservice for managing customer orders built with Java 25, Spring Boot 3.3, and Maven.

## Architecture

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

## API Endpoints

### Order Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/orders` | Create a new order |
| GET | `/api/v1/orders/{orderId}` | Get order by ID |
| GET | `/api/v1/orders` | Get all orders |
| GET | `/api/v1/orders/customer/{customerId}` | Get orders by customer |
| GET | `/api/v1/orders/status/{status}` | Get orders by status |
| GET | `/api/v1/orders/date-range?startDate=&endDate=` | Get orders by date range |
| PUT | `/api/v1/orders/{orderId}` | Update order |
| PATCH | `/api/v1/orders/{orderId}/status?status=` | Update order status |
| DELETE | `/api/v1/orders/{orderId}` | Delete order |
| GET | `/api/v1/orders/customer/{customerId}/count` | Get order count by customer |

### Order Statuses
- `PENDING` - Order created but not confirmed
- `CONFIRMED` - Order confirmed by customer
- `PROCESSING` - Order is being processed
- `SHIPPED` - Order has been shipped
- `DELIVERED` - Order delivered to customer
- `CANCELLED` - Order cancelled
- `RETURNED` - Order returned by customer

## cURL Commands / Postman Examples

### Create a New Order
```bash
curl --location 'http://localhost:8082/api/v1/orders' \
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

### Get Order by ID
```bash
curl --location 'http://localhost:8082/api/v1/orders/1'
```

### Get All Orders
```bash
curl --location 'http://localhost:8082/api/v1/orders'
```

### Get Orders by Customer ID
```bash
curl --location 'http://localhost:8082/api/v1/orders/customer/1'
```

### Get Orders by Status
```bash
curl --location 'http://localhost:8082/api/v1/orders/status/PENDING'
```

### Get Orders by Date Range
```bash
curl --location 'http://localhost:8082/api/v1/orders/date-range?startDate=2025-01-01T00:00:00&endDate=2025-12-31T23:59:59'
```

### Update Order
```bash
curl --location --request PUT 'http://localhost:8082/api/v1/orders/1' \
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

### Update Order Status
```bash
curl --location --request PATCH 'http://localhost:8082/api/v1/orders/1/status?status=CONFIRMED'
```

### Delete Order
```bash
curl --location --request DELETE 'http://localhost:8082/api/v1/orders/1'
```

### Get Order Count by Customer
```bash
curl --location 'http://localhost:8082/api/v1/orders/customer/1/count'
```

## Running with Docker

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

## Configuration

### Database Configuration
Edit `src/main/resources/application.yml`:
