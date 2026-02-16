# Product Microservice

A comprehensive Product Catalog Microservice built with Java 25, Spring Boot, and PostgreSQL.

## Features

- Category Management (CRUD operations)
- Product Management (CRUD operations)
- Inventory Management (Stock tracking, reservations)
- Warehouse Management (CRUD operations)
- RESTful API with standardized responses
- Global Exception Handling
- Input Validation
- Database relationships (One-to-Many, Many-to-One)

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

The application will start on `http://localhost:8083`

## API Endpoints

### Category Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/categories` | Create a new category |
| GET | `/api/v1/categories/{id}` | Get category by ID |
| GET | `/api/v1/categories` | Get all categories |
| GET | `/api/v1/categories/active` | Get active categories |
| GET | `/api/v1/categories/subcategories/{parentId}` | Get subcategories |
| PUT | `/api/v1/categories/{id}` | Update category |
| PATCH | `/api/v1/categories/{id}/toggle-status` | Toggle category status |
| DELETE | `/api/v1/categories/{id}` | Delete category |

### Product Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/products` | Create a new product |
| GET | `/api/v1/products/{id}` | Get product by ID |
| GET | `/api/v1/products/sku/{sku}` | Get product by SKU |
| GET | `/api/v1/products` | Get all products |
| GET | `/api/v1/products/active` | Get active products |
| GET | `/api/v1/products/category/{categoryId}` | Get products by category |
| GET | `/api/v1/products/search?keyword={keyword}` | Search products |
| PUT | `/api/v1/products/{id}` | Update product |
| PATCH | `/api/v1/products/{id}/toggle-status` | Toggle product status |
| DELETE | `/api/v1/products/{id}` | Delete product |

### Inventory Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/inventories` | Create inventory |
| GET | `/api/v1/inventories/{id}` | Get inventory by ID |
| GET | `/api/v1/inventories` | Get all inventories |
| GET | `/api/v1/inventories/product/{productId}` | Get inventories by product |
| GET | `/api/v1/inventories/low-stock` | Get low stock items |
| GET | `/api/v1/inventories/product/{productId}/total` | Get total quantity |
| PUT | `/api/v1/inventories/{id}` | Update inventory |
| PATCH | `/api/v1/inventories/{id}/adjust?quantity={qty}` | Adjust quantity |
| PATCH | `/api/v1/inventories/{id}/reserve?quantity={qty}` | Reserve quantity |
| PATCH | `/api/v1/inventories/{id}/release?quantity={qty}` | Release reserved |
| PATCH | `/api/v1/inventories/product/{productId}/reserve?quantity={qty}` | Reserve by product |
| PATCH | `/api/v1/inventories/product/{productId}/release?quantity={qty}` | Release by product |
| DELETE | `/api/v1/inventories/{id}` | Delete inventory |

### Warehouse Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/warehouses` | Create a new warehouse |
| GET | `/api/v1/warehouses/{id}` | Get warehouse by ID |
| GET | `/api/v1/warehouses` | Get all warehouses |
| GET | `/api/v1/warehouses/active` | Get active warehouses |
| GET | `/api/v1/warehouses/city/{city}` | Get warehouses by city |
| PUT | `/api/v1/warehouses/{id}` | Update warehouse |
| PATCH | `/api/v1/warehouses/{id}/toggle-status` | Toggle warehouse status |
| DELETE | `/api/v1/warehouses/{id}` | Delete warehouse |

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

## cURL Commands / Postman Examples

### Category Endpoints

#### Create Category
```bash
curl --location 'http://localhost:8083/api/v1/categories' \
--header 'Content-Type: application/json' \
--data '{
    "name": "Electronics",
    "description": "Electronic devices and accessories",
    "parentCategoryId": null,
    "imageUrl": "https://example.com/electronics.png",
    "isActive": true
}'
```

#### Get Category by ID
```bash
curl --location 'http://localhost:8083/api/v1/categories/1'
```

#### Get All Categories
```bash
curl --location 'http://localhost:8083/api/v1/categories'
```

#### Get Active Categories
```bash
curl --location 'http://localhost:8083/api/v1/categories/active'
```

#### Get Subcategories
```bash
curl --location 'http://localhost:8083/api/v1/categories/subcategories/1'
```

#### Update Category
```bash
curl --location --request PUT 'http://localhost:8083/api/v1/categories/1' \
--header 'Content-Type: application/json' \
--data '{
    "name": "Electronics & Gadgets",
    "description": "Updated description for electronics",
    "parentCategoryId": null,
    "imageUrl": "https://example.com/electronics-updated.png",
    "isActive": true
}'
```

#### Toggle Category Status
```bash
curl --location --request PATCH 'http://localhost:8083/api/v1/categories/1/toggle-status'
```

#### Delete Category
```bash
curl --location --request DELETE 'http://localhost:8083/api/v1/categories/1'
```

---

### Product Endpoints

#### Create Product
```bash
curl --location 'http://localhost:8083/api/v1/products' \
--header 'Content-Type: application/json' \
--data '{
    "categoryId": 1,
    "sku": "LAPTOP-001",
    "name": "Dell Laptop",
    "description": "High performance laptop for professionals",
    "brand": "Dell",
    "price": 999.99,
    "discountPercentage": 10.00,
    "taxRate": 8.00,
    "weight": 2.5,
    "dimensions": "35x25x2 cm",
    "isActive": true
}'
```

#### Get Product by ID
```bash
curl --location 'http://localhost:8083/api/v1/products/1'
```

#### Get Product by SKU
```bash
curl --location 'http://localhost:8083/api/v1/products/sku/LAPTOP-001'
```

#### Get All Products
```bash
curl --location 'http://localhost:8083/api/v1/products'
```

#### Get Active Products
```bash
curl --location 'http://localhost:8083/api/v1/products/active'
```

#### Get Products by Category
```bash
curl --location 'http://localhost:8083/api/v1/products/category/1'
```

#### Search Products
```bash
curl --location 'http://localhost:8083/api/v1/products/search?keyword=laptop'
```

#### Update Product
```bash
curl --location --request PUT 'http://localhost:8083/api/v1/products/1' \
--header 'Content-Type: application/json' \
--data '{
    "categoryId": 1,
    "sku": "LAPTOP-001",
    "name": "Dell Laptop Pro",
    "description": "Updated high performance laptop",
    "brand": "Dell",
    "price": 1099.99,
    "discountPercentage": 15.00,
    "taxRate": 8.00,
    "weight": 2.3,
    "dimensions": "34x24x1.8 cm",
    "isActive": true
}'
```

#### Toggle Product Status
```bash
curl --location --request PATCH 'http://localhost:8083/api/v1/products/1/toggle-status'
```

#### Delete Product
```bash
curl --location --request DELETE 'http://localhost:8083/api/v1/products/1'
```

---

### Inventory Endpoints

#### Create Inventory
```bash
curl --location 'http://localhost:8083/api/v1/inventories' \
--header 'Content-Type: application/json' \
--data '{
    "productId": 1,
    "warehouseId": 1,
    "quantityAvailable": 100,
    "quantityReserved": 0,
    "reorderLevel": 20
}'
```

#### Get Inventory by ID
```bash
curl --location 'http://localhost:8083/api/v1/inventories/1'
```

#### Get All Inventories
```bash
curl --location 'http://localhost:8083/api/v1/inventories'
```

#### Get Inventories by Product
```bash
curl --location 'http://localhost:8083/api/v1/inventories/product/1'
```

#### Get Low Stock Inventories
```bash
curl --location 'http://localhost:8083/api/v1/inventories/low-stock'
```

#### Get Total Available Quantity for Product
```bash
curl --location 'http://localhost:8083/api/v1/inventories/product/1/total'
```

#### Update Inventory
```bash
curl --location --request PUT 'http://localhost:8083/api/v1/inventories/1' \
--header 'Content-Type: application/json' \
--data '{
    "productId": 1,
    "warehouseId": 1,
    "quantityAvailable": 150,
    "quantityReserved": 10,
    "reorderLevel": 25
}'
```

#### Adjust Inventory Quantity
```bash
curl --location --request PATCH 'http://localhost:8083/api/v1/inventories/1/adjust?quantity=50'
```

#### Reserve Quantity by Inventory ID
```bash
curl --location --request PATCH 'http://localhost:8083/api/v1/inventories/1/reserve?quantity=10'
```

#### Release Reserved Quantity by Inventory ID
```bash
curl --location --request PATCH 'http://localhost:8083/api/v1/inventories/1/release?quantity=5'
```

#### Reserve Quantity by Product ID
```bash
curl --location --request PATCH 'http://localhost:8083/api/v1/inventories/product/1/reserve?quantity=10'
```

#### Release Reserved Quantity by Product ID
```bash
curl --location --request PATCH 'http://localhost:8083/api/v1/inventories/product/1/release?quantity=5'
```

#### Delete Inventory
```bash
curl --location --request DELETE 'http://localhost:8083/api/v1/inventories/1'
```

---

### Warehouse Endpoints

#### Create Warehouse
```bash
curl --location 'http://localhost:8083/api/v1/warehouses' \
--header 'Content-Type: application/json' \
--data '{
    "name": "Main Warehouse",
    "location": "123 Industrial Park, Building A",
    "city": "New York",
    "state": "NY",
    "country": "USA",
    "zipCode": "10001",
    "capacity": 5000,
    "isActive": true
}'
```

#### Get Warehouse by ID
```bash
curl --location 'http://localhost:8083/api/v1/warehouses/1'
```

#### Get All Warehouses
```bash
curl --location 'http://localhost:8083/api/v1/warehouses'
```

#### Get Active Warehouses
```bash
curl --location 'http://localhost:8083/api/v1/warehouses/active'
```

#### Get Warehouses by City
```bash
curl --location 'http://localhost:8083/api/v1/warehouses/city/New%20York'
```

#### Update Warehouse
```bash
curl --location --request PUT 'http://localhost:8083/api/v1/warehouses/1' \
--header 'Content-Type: application/json' \
--data '{
    "name": "Main Warehouse - Expanded",
    "location": "123 Industrial Park, Building A & B",
    "city": "New York",
    "state": "NY",
    "country": "USA",
    "zipCode": "10001",
    "capacity": 10000,
    "isActive": true
}'
```

#### Toggle Warehouse Status
```bash
curl --location --request PATCH 'http://localhost:8083/api/v1/warehouses/1/toggle-status'
```

#### Delete Warehouse
```bash
curl --location --request DELETE 'http://localhost:8083/api/v1/warehouses/1'
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
- `WAREHOUSE_NOT_FOUND` - Warehouse does not exist
- `VALIDATION_ERROR` - Input validation failed
- `INTERNAL_SERVER_ERROR` - Unexpected server error

## Features in Detail

### Warehouse Management
- Create and manage multiple warehouses
- Track warehouse capacity
- Filter by city and active status
- Soft delete with active/inactive toggle

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
