# Product Microservice

A comprehensive Product Catalog Microservice built with Java 25, Spring Boot, and PostgreSQL.

## Features

- ✅ Category Management (CRUD operations)
- ✅ Product Management (CRUD operations)
- ✅ Inventory Management (Stock tracking, reservations)
- ✅ RESTful API with standardized responses
- ✅ Global Exception Handling
- ✅ Input Validation
- ✅ Database relationships (One-to-Many, Many-to-One)

## Technology Stack

- **Java**: 25
- **Spring Boot**: 3.3.0
- **Database**: PostgreSQL
- **Build Tool**: Maven
- **ORM**: Spring Data JPA / Hibernate
- **Validation**: Jakarta Bean Validation
- **Logging**: SLF4J with Logback
- **Mapping**: ModelMapper

## Project Structure

```
com.mestro
├── controller/          # REST API Controllers
├── service/            # Business Logic Layer
├── repository/         # Data Access Layer
├── model/              # Entity Classes
├── dto/                # Data Transfer Objects
├── exception/          # Custom Exceptions & Global Handler
├── enums/              # Enumerations
└── config/             # Application Configuration
```

## Database Schema

### Tables

1. **categories** - Product categories with hierarchical support
2. **products** - Product catalog with pricing and details
3. **product_images** - Product images with display order
4. **product_inventory** - Stock management per warehouse

## Prerequisites

- Java 25 (JDK)
- Maven 3.8+
- PostgreSQL 14+
- IDE (IntelliJ IDEA, Eclipse, or VS Code)

## Setup Instructions

### 1. Database Setup

Create PostgreSQL database:

```sql
CREATE DATABASE product_db;
```

Update `application.properties` with your database credentials:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/product_db
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 2. Build the Project

```bash
mvn clean install
```

### 3. Run the Application

```bash
mvn spring-boot:run
```

Or run the JAR file:

```bash
java -jar target/product-microservice-1.0.0.jar
```

The application will start on `http://localhost:8081`

## API Endpoints

### Category Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/categories` | Create a new category |
| GET | `/api/categories/{id}` | Get category by ID |
| GET | `/api/categories` | Get all categories |
| GET | `/api/categories/active` | Get active categories |
| GET | `/api/categories/subcategories/{parentId}` | Get subcategories |
| PUT | `/api/categories/{id}` | Update category |
| PATCH | `/api/categories/{id}/toggle-status` | Toggle category status |
| DELETE | `/api/categories/{id}` | Delete category |

### Product Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/products` | Create a new product |
| GET | `/api/products/{id}` | Get product by ID |
| GET | `/api/products/sku/{sku}` | Get product by SKU |
| GET | `/api/products` | Get all products |
| GET | `/api/products/active` | Get active products |
| GET | `/api/products/category/{categoryId}` | Get products by category |
| GET | `/api/products/search?keyword={keyword}` | Search products |
| PUT | `/api/products/{id}` | Update product |
| PATCH | `/api/products/{id}/toggle-status` | Toggle product status |
| DELETE | `/api/products/{id}` | Delete product |

### Inventory Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/inventories` | Create inventory |
| GET | `/api/inventories/{id}` | Get inventory by ID |
| GET | `/api/inventories` | Get all inventories |
| GET | `/api/inventories/product/{productId}` | Get inventories by product |
| GET | `/api/inventories/low-stock` | Get low stock items |
| GET | `/api/inventories/product/{productId}/total` | Get total quantity |
| PUT | `/api/inventories/{id}` | Update inventory |
| PATCH | `/api/inventories/{id}/adjust?quantity={qty}` | Adjust quantity |
| PATCH | `/api/inventories/{id}/reserve?quantity={qty}` | Reserve quantity |
| PATCH | `/api/inventories/{id}/release?quantity={qty}` | Release reserved |
| DELETE | `/api/inventories/{id}` | Delete inventory |

## API Response Format

### Success Response
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... },
  "error": null
}
```

### Error Response
```json
{
  "success": false,
  "message": "Error message",
  "data": null,
  "error": {
    "errorCode": "ERROR_CODE",
    "errorMessage": "Detailed error message"
  }
}
```

## Sample Requests

### Create Category
```bash
curl -X POST http://localhost:8081/api/categories \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Electronics",
    "description": "Electronic devices and accessories",
    "isActive": true
  }'
```

### Create Product
```bash
curl -X POST http://localhost:8081/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "categoryId": 1,
    "sku": "LAPTOP-001",
    "name": "Dell Laptop",
    "description": "High performance laptop",
    "brand": "Dell",
    "price": 999.99,
    "discountPercentage": 10.00,
    "taxRate": 8.00,
    "isActive": true
  }'
```

### Create Inventory
```bash
curl -X POST http://localhost:8081/api/inventories \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 1,
    "warehouseId": 1,
    "quantityAvailable": 100,
    "quantityReserved": 0,
    "reorderLevel": 20
  }'
```

## Testing

Run unit tests:
```bash
mvn test
```

## Error Codes

- `CATEGORY_NOT_FOUND` - Category does not exist
- `CATEGORY_ALREADY_EXISTS` - Category name already in use
- `PRODUCT_NOT_FOUND` - Product does not exist
- `PRODUCT_SKU_EXISTS` - SKU already in use
- `INVENTORY_NOT_FOUND` - Inventory record not found
- `VALIDATION_ERROR` - Input validation failed
- `INTERNAL_SERVER_ERROR` - Unexpected server error

## Features in Detail

### Inventory Management
- Track quantity available and reserved
- Multiple warehouses support
- Low stock alerts
- Reserve/Release operations for order processing
- Automatic stock adjustment

### Category Management
- Hierarchical categories (parent-child relationships)
- Soft delete with active/inactive status
- Cascade operations

### Product Management
- SKU-based unique identification
- Category association
- Price and discount management
- Tax rate support
- Image and inventory associations

## Future Enhancements

- [ ] Product images upload functionality
- [ ] Pagination and filtering
- [ ] Product reviews and ratings
- [ ] Batch operations
- [ ] Caching with Redis
- [ ] API documentation with Swagger/OpenAPI
- [ ] Unit and integration tests
- [ ] Docker containerization
- [ ] Kubernetes deployment

## License

MIT License

## Contact

For questions or support, please contact the development team.
