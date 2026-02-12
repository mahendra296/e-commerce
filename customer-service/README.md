# Customer Service Microservice

A Spring Boot microservice for managing customer and customer address data with RESTful APIs.

## Technology Stack

- Java 25
- Spring Boot 3.3.0
- Maven
- Spring Data JPA
- PostgreSQL
- Lombok

## Project Structure

```
com.mestro
├── config/          - Application configurations
├── controller/      - REST API controllers
├── dto/            - Data Transfer Objects
├── enums/          - Enumerations
├── exceptions/     - Custom exceptions and global exception handler
├── model/          - JPA entities
├── repository/     - JPA repositories
└── service/        - Business logic services
```

## Running the Application

### Prerequisites
- Java 25 installed
- Maven installed

### Build and Run
```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start on port **8081**.

### Database

PostgreSQL is required. Default connection:
- URL: `jdbc:postgresql://localhost:5432/customerdb`
- Username: `postgres`
- Password: `root`

Override with environment variables: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`.

---

## Running with Docker

### Prerequisites
- [Docker](https://docs.docker.com/get-docker/) installed and running

### Using Docker Compose (Recommended)

This starts both PostgreSQL and the application with a single command:

```bash
# Build and start all services
docker compose up -d

# View logs
docker compose logs -f customer-service

# Stop all services
docker compose down

# Stop and remove volumes (deletes database data)
docker compose down -v
```

The application will be available at `http://localhost:8081`.
Swagger UI will be at `http://localhost:8081/swagger-ui.html`.

### Using Dockerfile Only

If you already have a PostgreSQL instance running:

```bash
# Build the image
docker build -t customer-service .

# Run the container
docker run -d \
  --name customer-service \
  -p 8081:8081 \
  -e DB_HOST=host.docker.internal \
  -e DB_PORT=5432 \
  -e DB_NAME=customerdb \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=root \
  customer-service
```

> **Note:** `host.docker.internal` connects to services running on your host machine. Replace with the actual database host if different.

### Useful Docker Commands

```bash
# Check running containers
docker ps

# View application logs
docker logs -f customer-service

# Stop the container
docker stop customer-service

# Remove the container
docker rm customer-service

# Rebuild after code changes
docker compose up -d --build
```

## API Endpoints

### Customer APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/customers` | Create a customer |
| GET | `/api/v1/customers/{id}` | Get customer by ID |
| GET | `/api/v1/customers/email/{email}` | Get customer by email |
| GET | `/api/v1` | Get all customers |
| PUT | `/api/v1/customers/{id}` | Update a customer |
| DELETE | `/api/v1/customers/{id}` | Delete a customer |

### Customer Address APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/customers/{customerId}/addresses` | Create an address |
| GET | `/api/v1/customers/{customerId}/addresses` | Get all addresses for a customer |
| GET | `/api/v1/customers/{customerId}/addresses/{addressId}` | Get address by ID |
| PUT | `/api/v1/customers/{customerId}/addresses/{addressId}` | Update an address |
| DELETE | `/api/v1/customers/{customerId}/addresses/{addressId}` | Delete an address |

## Testing the API

### Using cURL

#### Create a Customer
```bash
curl -X POST {{baseURL}}/api/v1/customers \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "dob": "1990-01-15",
    "phone": "+1234567890",
    "gender": "Male",
    "notes": "VIP customer"
  }'
```

#### Get Customer by ID
```bash
curl {{baseURL}}/api/v1/customers/1
```

#### Get Customer by Email
```bash
curl {{baseURL}}/api/v1/customers/email/john.doe@example.com
```

#### Get All Customers
```bash
curl {{baseURL}}/api/v1
```

#### Update a Customer
```bash
curl -X PUT {{baseURL}}/api/v1/customers/1 \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "dob": "1990-01-15",
    "phone": "+9876543210",
    "gender": "Male",
    "notes": "Updated notes"
  }'
```

#### Delete a Customer
```bash
curl -X DELETE {{baseURL}}/api/v1/customers/1
```

#### Create an Address
```bash
curl -X POST {{baseURL}}/api/v1/customers/1/addresses \
  -H "Content-Type: application/json" \
  -d '{
    "addressType": "HOME",
    "street": "123 Main Street",
    "city": "New York",
    "state": "NY",
    "zipCode": "10001",
    "country": "USA",
    "isDefault": true
  }'
```

#### Get All Addresses for a Customer
```bash
curl {{baseURL}}/api/v1/customers/1/addresses
```

#### Get Address by ID
```bash
curl {{baseURL}}/api/v1/customers/1/addresses/1
```

#### Update an Address
```bash
curl -X PUT {{baseURL}}/api/v1/customers/1/addresses/1 \
  -H "Content-Type: application/json" \
  -d '{
    "addressType": "OFFICE",
    "street": "456 Business Ave",
    "city": "San Francisco",
    "state": "CA",
    "zipCode": "94105",
    "country": "USA",
    "isDefault": false
  }'
```

#### Delete an Address
```bash
curl -X DELETE {{baseURL}}/api/v1/customers/1/addresses/1
```

## Error Codes

- `CUST_001` - Customer not found
- `CUST_002` - Duplicate email
- `ADDR_001` - Customer address not found
- `VAL_001` - Invalid input data
- `REQ_001` - Bad request
- `SYS_001` - Internal server error

## Sample API Response

All APIs return a standard response format:

```json
{
  "success": true,
  "message": "Customer created successfully",
  "data": { ... }
}
```

Error response:

```json
{
  "success": false,
  "message": "Customer not found",
  "errorCode": "CUST_001"
}
```

## Features

- ✅ RESTful API design
- ✅ CRUD operations for customers and addresses
- ✅ Input validation
- ✅ Common response structure
- ✅ Global exception handling
- ✅ Audit fields (createdAt, updatedAt)
- ✅ One-to-many relationship (Customer → Addresses)
- ✅ Email uniqueness validation
- ✅ Proper HTTP status codes
- ✅ Lombok for reduced boilerplate
- ✅ Transaction management

## License

This project is open source and available under the MIT License.
