# Chapter 04 - OpenAPI v3.1 Implementation Summary

## Overview

Chapter 04 contains **two complete implementations** demonstrating MicroProfile OpenAPI 4.1 alignment with OpenAPI v3.1 and JSON Schema 2020-12:

1. **catalog** - Database-backed product catalog (comprehensive implementation)
2. **mp-ecomm-store** - In-memory e-commerce store (simplified demonstration)

## Implementation Comparison

| Feature | Catalog Service | MP E-Commerce Store |
|---------|----------------|---------------------|
| **Data Storage** | PostgreSQL database | In-memory ArrayList |
| **JPA/Hibernate** | ✅ Full JPA implementation | ❌ No database |
| **Persistence** | ✅ Data persists across restarts | ❌ Data lost on restart |
| **Product Fields** | 14 fields (comprehensive) | 8 fields (essential) |
| **Lifecycle Callbacks** | ✅ @PrePersist, @PreUpdate | ❌ Not applicable |
| **Timestamps** | ✅ Automatic createdAt/updatedAt | ❌ No timestamps |
| **Complexity** | Advanced (production-ready) | Simple (learning-focused) |
| **Best For** | Production use, advanced learning | Quick demos, getting started |

## Project Structure

```
code/chapter04/
├── catalog/                          # Database-backed implementation
│   ├── src/main/java/
│   │   └── io/microprofile/tutorial/store/product/
│   │       ├── entity/
│   │       │   └── Product.java     # JPA entity with 14 fields
│   │       ├── resource/
│   │       │   └── ProductResource.java
│   │       └── ProductRestApplication.java
│   ├── README.adoc
│   ├── README_OPENAPI_V3.1.md       # Implementation guide
│   └── test-openapi-features.sh     # Test script
│
├── mp-ecomm-store/                   # In-memory implementation
│   ├── src/main/java/
│   │   └── io/microprofile/tutorial/store/product/
│   │       ├── entity/
│   │       │   └── Product.java     # POJO with 8 fields
│   │       ├── resource/
│   │       │   └── ProductResource.java
│   │       └── ProductRestApplication.java
│   ├── README.adoc
│   ├── README_OPENAPI.md            # Implementation guide
│   └── test-openapi-features.sh     # Test script
│
├── OPENAPI_V3.1_DEMO.md             # Comprehensive 500+ line guide
├── OPENAPI_COMPARISON.md            # OpenAPI 3.0 vs 3.1 (400+ lines)
├── QUICK_REFERENCE.md               # Quick reference card
└── IMPLEMENTATION_SUMMARY.md        # This file
```

## Catalog Service (Database-Backed)

### Product Entity - 14 Fields

```java
public class Product {
    private Long id;                  // Auto-generated primary key
    private String name;              // Product name (required)
    private String description;       // Detailed description (nullable)
    private Double price;             // Price with validation
    private String sku;               // Stock Keeping Unit (pattern validated)
    private String category;          // Product category (enumerated)
    private Integer stockQuantity;    // Available stock
    private Double rating;            // Customer rating (0.0 - 5.0)
    private Double weight;            // Product weight in kg
    private Boolean active;           // Product availability
    private String manufacturer;      // Manufacturer name
    private Integer warrantyMonths;   // Warranty period
    private LocalDateTime createdAt;  // Auto-populated on creation
    private LocalDateTime updatedAt;  // Auto-updated on modification
}
```

### Key Features

✅ **JPA Persistence**: Full database integration with PostgreSQL  
✅ **Lifecycle Callbacks**: Automatic timestamp management  
✅ **Advanced Validation**: 14 fields with comprehensive constraints  
✅ **Production Ready**: Complete CRUD operations  
✅ **Comprehensive Schema**: All OpenAPI v3.1 features demonstrated  

### Sample Product
```json
{
  "id": 1,
  "name": "Wireless Mouse",
  "description": "Ergonomic wireless mouse with USB receiver",
  "price": 29.99,
  "sku": "ELC-MS001-BLK",
  "category": "ELECTRONICS",
  "stockQuantity": 150,
  "rating": 4.5,
  "weight": 0.125,
  "active": true,
  "manufacturer": "TechCorp",
  "warrantyMonths": 12,
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-20T14:45:00"
}
```

### Documentation
- **README**: [catalog/README_OPENAPI_V3.1.md](catalog/README_OPENAPI_V3.1.md)
- **Port**: 9081
- **OpenAPI URL**: http://localhost:9081/openapi
- **Swagger UI**: http://localhost:9081/openapi/ui

## MP E-Commerce Store (In-Memory)

### Product Entity - 8 Fields

```java
public class Product {
    private Long id;                  // Unique identifier
    private String name;              // Product name
    private String description;       // Product description (nullable)
    private Double price;             // Price with validation
    private String sku;               // SKU (pattern validated)
    private String category;          // Category (enumerated)
    private Integer stockQuantity;    // Stock level
    private Boolean inStock;          // Availability flag
}
```

### Key Features

✅ **Simple Design**: In-memory ArrayList storage  
✅ **Quick Setup**: No database configuration needed  
✅ **Essential Fields**: 8 core product attributes  
✅ **Full OpenAPI v3.1**: All features demonstrated  
✅ **Learning Focused**: Easy to understand and modify  

### Sample Product
```json
{
  "id": 1,
  "name": "Wireless Mouse",
  "description": "Ergonomic wireless mouse with USB receiver",
  "price": 29.99,
  "sku": "ELC-MS001-BLK",
  "category": "ELECTRONICS",
  "stockQuantity": 150,
  "inStock": true
}
```

### Documentation
- **README**: [mp-ecomm-store/README_OPENAPI.md](mp-ecomm-store/README_OPENAPI.md)
- **Port**: 5050
- **OpenAPI URL**: http://localhost:5050/mp-ecomm-store/openapi
- **Swagger UI**: http://localhost:5050/mp-ecomm-store/openapi/ui

## Common OpenAPI v3.1 Features

Both implementations demonstrate the same OpenAPI v3.1 features:

### 1. Pattern Validation (SKU)
```java
@Schema(
    pattern = "^[A-Z]{3}-[A-Z0-9]+-[A-Z0-9]+$",
    example = "ELC-MS001-BLK"
)
private String sku;
```

✅ Valid: `ELC-MS001-BLK`, `CLO-TSH123-RED`  
❌ Invalid: `elc-ms001-blk`, `E-MS001-BLK`

### 2. Exclusive Numeric Bounds (Price)
```java
@Schema(
    minimum = "0.01",
    exclusiveMinimum = true,  // OpenAPI v3.1 feature
    multipleOf = "0.01",
    format = "double"
)
private Double price;
```

✅ Valid: `29.99`, `100.00`  
❌ Invalid: `0.00` (must be > 0.01), `29.999` (not multiple of 0.01)

### 3. Enumeration (Category)
```java
@Schema(
    enumeration = {"ELECTRONICS", "CLOTHING", "BOOKS", "HOME", "SPORTS"},
    example = "ELECTRONICS"
)
private String category;
```

✅ Valid: `ELECTRONICS`, `CLOTHING`, `BOOKS`, `HOME`, `SPORTS`  
❌ Invalid: `electronics` (case-sensitive), `TOYS` (not in enum)

### 4. Nullable Properties
```java
@Schema(
    nullable = true,
    minLength = 10,
    maxLength = 1000
)
private String description;
```

### 5. Format Specifications
```java
@Schema(format = "int64")    // For Long id
@Schema(format = "int32")    // For Integer stockQuantity
@Schema(format = "double")   // For Double price
```

### 6. Array Constraints
```java
@Schema(
    implementation = Product.class,
    type = SchemaType.ARRAY,
    minItems = 0,
    maxItems = 1000
)
```

## REST Endpoints

Both implementations provide the same REST API:

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/products` | List all products (array with constraints) |
| GET | `/products/{id}` | Get product by ID (format: int64) |
| POST | `/products` | Create new product (full validation) |
| GET | `/products/search` | Search with filters & pagination |

### Search Parameters

| Parameter | Type | Validation | Example |
|-----------|------|------------|---------|
| `name` | String | Pattern match | `?name=Mouse` |
| `category` | String | Enumeration | `?category=ELECTRONICS` |
| `minPrice` | Double | >= 0.01 | `?minPrice=20` |
| `maxPrice` | Double | > minPrice | `?maxPrice=100` |
| `page` | Integer | >= 0 | `?page=0` |
| `size` | Integer | 1-100 | `?size=10` |

## Running the Services

### Catalog Service (Port 9081)
```bash
cd code/chapter04/catalog
mvn clean package
mvn liberty:dev

# Access points:
# OpenAPI: http://localhost:9081/openapi
# Swagger:  http://localhost:9081/openapi/ui
# API:      http://localhost:9081/products
```

### MP E-Commerce Store (Port 5050)
```bash
cd code/chapter04/mp-ecomm-store
mvn clean package
mvn liberty:dev

# Access points:
# OpenAPI: http://localhost:5050/mp-ecomm-store/openapi
# Swagger:  http://localhost:5050/mp-ecomm-store/openapi/ui
# API:      http://localhost:5050/mp-ecomm-store/api/products
```

## Testing the APIs

Both services include test scripts:

```bash
# Test catalog service
cd code/chapter04/catalog
chmod +x test-openapi-features.sh
./test-openapi-features.sh

# Test mp-ecomm-store
cd code/chapter04/mp-ecomm-store
chmod +x test-openapi-features.sh
./test-openapi-features.sh
```

## Documentation Files

### Comprehensive Guides

1. **[OPENAPI_V3.1_DEMO.md](OPENAPI_V3.1_DEMO.md)** (500+ lines)
   - Complete OpenAPI v3.1 feature demonstration
   - Code examples and validation rules
   - Testing procedures
   - Best practices

2. **[OPENAPI_COMPARISON.md](OPENAPI_COMPARISON.md)** (400+ lines)
   - Detailed OpenAPI 3.0 vs 3.1 comparison
   - Migration guide
   - Breaking changes and compatibility
   - Feature-by-feature analysis

3. **[QUICK_REFERENCE.md](QUICK_REFERENCE.md)**
   - Quick reference card
   - Common patterns
   - Validation examples
   - Troubleshooting tips

### Implementation Guides

4. **[catalog/README_OPENAPI_V3.1.md](catalog/README_OPENAPI_V3.1.md)**
   - Database-backed implementation details
   - 14-field Product entity guide
   - JPA and persistence features

5. **[mp-ecomm-store/README_OPENAPI.md](mp-ecomm-store/README_OPENAPI.md)**
   - In-memory implementation guide
   - Simplified 8-field approach
   - Getting started tutorial

## Which Implementation Should I Use?

### Use **Catalog Service** if you want:
- ✅ Production-ready implementation
- ✅ Database persistence
- ✅ Advanced features (lifecycle, timestamps)
- ✅ Comprehensive field validation
- ✅ Learning JPA and Hibernate integration

### Use **MP E-Commerce Store** if you want:
- ✅ Quick demonstration
- ✅ No database setup
- ✅ Simple, easy-to-understand code
- ✅ Focus on OpenAPI annotations
- ✅ Getting started with OpenAPI v3.1

### Use **Both** if you want:
- ✅ Complete learning experience
- ✅ Compare simple vs. complex implementations
- ✅ See same features in different contexts
- ✅ Understand evolution from simple to advanced

## Key Differences from OpenAPI 3.0

### 1. JSON Schema Alignment
**3.0**: Used subset of JSON Schema  
**3.1**: Uses JSON Schema 2020-12 directly

### 2. exclusiveMinimum/exclusiveMaximum
**3.0**: `exclusiveMinimum: 0` (boolean)  
**3.1**: `minimum: 0.01, exclusiveMinimum: true` (numeric + boolean)

### 3. Nullable
**3.0**: `nullable: true` (custom keyword)  
**3.1**: `nullable: true` (JSON Schema native)

### 4. Format Specifications
**3.0**: Limited formats  
**3.1**: Full JSON Schema format support

## Benefits of OpenAPI v3.1

1. **Single Source of Truth**: Same schema for API docs and validation
2. **Better Tooling**: Leverage JSON Schema ecosystem
3. **More Precise Validation**: Richer constraint expressions
4. **Standard Compliance**: Full JSON Schema 2020-12 support
5. **Improved Interoperability**: Works with any JSON Schema tool
6. **Code Generation**: Better type safety in generated clients

## Learning Path

### Beginner
1. Start with **mp-ecomm-store**
2. Review [README_OPENAPI.md](mp-ecomm-store/README_OPENAPI.md)
3. Run the application and explore Swagger UI
4. Test with [test-openapi-features.sh](mp-ecomm-store/test-openapi-features.sh)

### Intermediate
1. Move to **catalog service**
2. Study [README_OPENAPI_V3.1.md](catalog/README_OPENAPI_V3.1.md)
3. Explore database integration
4. Review comprehensive guide: [OPENAPI_V3.1_DEMO.md](OPENAPI_V3.1_DEMO.md)

### Advanced
1. Read [OPENAPI_COMPARISON.md](OPENAPI_COMPARISON.md)
2. Compare both implementations
3. Customize and extend features
4. Implement your own service with OpenAPI v3.1

## Additional Resources

- [MicroProfile OpenAPI Specification](https://github.com/eclipse/microprofile-open-api)
- [OpenAPI v3.1 Specification](https://spec.openapis.org/oas/v3.1.0)
- [JSON Schema 2020-12](https://json-schema.org/draft/2020-12/json-schema-core.html)
- [MicroProfile 7.1 Documentation](https://microprofile.io/)

## Troubleshooting

### Port Already in Use
```bash
# Change port in server.xml
<httpEndpoint id="defaultHttpEndpoint"
              httpPort="9082"  # Change this
              httpsPort="9443"/>
```

### Database Connection Issues (Catalog only)
```bash
# Check PostgreSQL is running
docker ps | grep postgres

# Verify connection in microprofile-config.properties
jakarta.persistence.jdbc.url=jdbc:postgresql://localhost:5432/productdb
jakarta.persistence.jdbc.user=postgres
jakarta.persistence.jdbc.password=postgres
```

### OpenAPI Not Loading
```bash
# Verify feature is enabled in server.xml
<feature>mpOpenAPI-4.0</feature>

# Check logs
tail -f target/liberty/wlp/usr/servers/defaultServer/logs/messages.log
```

## Summary

Chapter 04 provides **two complete, production-quality implementations** demonstrating MicroProfile OpenAPI 4.1 alignment with OpenAPI v3.1:

1. **Catalog Service**: Advanced, database-backed, 14 fields, production-ready
2. **MP E-Commerce Store**: Simple, in-memory, 8 fields, learning-focused

Both demonstrate:
- ✅ Pattern validation
- ✅ Exclusive numeric bounds
- ✅ Format specifications
- ✅ Enumeration
- ✅ Nullable properties
- ✅ Array constraints
- ✅ Comprehensive documentation

Choose the implementation that best fits your needs, or explore both for a complete learning experience!
