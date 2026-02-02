# MicroProfile E-Commerce Store - OpenAPI v3.1 Demonstration

This is an enhanced version of the Chapter 2 E-Commerce Store, demonstrating **MicroProfile OpenAPI 4.1** alignment with **OpenAPI v3.1** and **JSON Schema 2020-12**.

## Overview

This simple REST API showcases how MicroProfile OpenAPI 4.1 provides full OpenAPI v3.1 support with JSON Schema 2020-12 validation features, offering:

- **Precise validation** with patterns, formats, and constraints
- **Rich documentation** with examples and descriptions
- **Standard compliance** with JSON Schema 2020-12
- **Better tooling** by leveraging the JSON Schema ecosystem

## Project Structure

```
mp-ecomm-store/
├── src/main/java/io/microprofile/tutorial/store/product/
│   ├── entity/
│   │   └── Product.java              # Enhanced with OpenAPI v3.1 annotations
│   ├── resource/
│   │   └── ProductResource.java      # REST endpoints with schema validation
│   └── ProductRestApplication.java   # OpenAPI definition
├── pom.xml
└── README.md
```

## Key Features Demonstrated

### 1. Product Entity with JSON Schema 2020-12

The `Product` entity demonstrates:

```java
@Schema(
    description = "Product entity",
    example = """
        {
            "id": 1,
            "name": "iPhone 15 Pro",
            "description": "Apple iPhone 15 Pro with 256GB storage",
            "price": 999.99,
            "sku": "APL-IPH15P-256",
            "category": "ELECTRONICS",
            "stockQuantity": 50,
            "inStock": true
        }
        """
)
public class Product {
    // Enhanced with OpenAPI v3.1 schema annotations
}
```

#### Field Validations

| Field | Validation | Description |
|-------|------------|-------------|
| `id` | `format: int64`, `minimum: 1`, `readOnly` | Unique identifier |
| `name` | `minLength: 1`, `maxLength: 100`, `pattern` | Alphanumeric with spaces/hyphens |
| `description` | `maxLength: 500`, `nullable` | Optional description |
| `price` | `minimum: 0.01`, `exclusiveMinimum`, `multipleOf: 0.01` | Price > $0.01, rounded to cents |
| `sku` | `pattern: ^[A-Z]{3}-[A-Z0-9]+-[A-Z0-9]+$` | Format: XXX-XXXXX-XXXX |
| `category` | `enumeration` | ELECTRONICS, CLOTHING, BOOKS, etc. |
| `stockQuantity` | `minimum: 0`, `defaultValue: 0` | Non-negative quantity |
| `inStock` | `defaultValue: true` | Availability flag |

### 2. REST Endpoints with Advanced Validation

#### GET /api/products
Returns all products with array schema constraints
```bash
curl http://localhost:5050/mp-ecomm-store/api/products
```

**Response Schema:**
- Type: Array
- Min Items: 0
- Max Items: 10000
- Item Schema: Product

#### GET /api/products/{id}
Get product by ID with validated path parameter
```bash
curl http://localhost:5050/mp-ecomm-store/api/products/1
```

**Path Parameter:**
- `id`: int64, minimum: 1

#### POST /api/products
Create a new product with full validation
```bash
curl -X POST http://localhost:5050/mp-ecomm-store/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Samsung Galaxy S24",
    "description": "Samsung Galaxy S24 Ultra",
    "price": 1199.99,
    "sku": "SAM-GAL-S24-512",
    "category": "ELECTRONICS",
    "stockQuantity": 30,
    "inStock": true
  }'
```

#### GET /api/products/search
Search products with multiple validated parameters
```bash
# Search by name
curl "http://localhost:5050/mp-ecomm-store/api/products/search?name=iPhone"

# Search by price range (demonstrates exclusiveMinimum)
curl "http://localhost:5050/mp-ecomm-store/api/products/search?minPrice=500&maxPrice=1500"

# Search by category (demonstrates enumeration)
curl "http://localhost:5050/mp-ecomm-store/api/products/search?category=ELECTRONICS"

# Search with pagination
curl "http://localhost:5050/mp-ecomm-store/api/products/search?page=0&size=10"

# Combined search
curl "http://localhost:5050/mp-ecomm-store/api/products/search?name=iPhone&minPrice=900&category=ELECTRONICS&page=0&size=10"
```

**Query Parameters:**
- `name`: String (pattern validated, 1-100 chars)
- `minPrice`: Double (exclusiveMinimum, multipleOf: 0.01)
- `maxPrice`: Double (inclusive)
- `category`: Enum (ELECTRONICS, CLOTHING, etc.)
- `page`: Integer (default: 0, minimum: 0)
- `size`: Integer (default: 20, range: 1-100)

## JSON Schema 2020-12 Features Showcased

### 1. Pattern Validation
```java
@Schema(
    pattern = "^[A-Z]{3}-[A-Z0-9]+-[A-Z0-9]+$",
    example = "APL-IPH15P-256"
)
private String sku;
```

### 2. Exclusive Minimum (OpenAPI 3.1 Enhancement)
```java
@Schema(
    minimum = "0.01",
    exclusiveMinimum = true,  // Price > $0.01 (not >=)
    multipleOf = 0.01
)
private Double price;
```

**OpenAPI 3.0 vs 3.1:**
- 3.0: `minimum: 0, exclusiveMinimum: true` (confusing!)
- 3.1: `minimum: "0.01", exclusiveMinimum: true` (clear!)

### 3. Format Specifications
```java
@Schema(type = SchemaType.INTEGER, format = "int64")
private Long id;

@Schema(type = SchemaType.NUMBER, format = "double")
private Double price;

@Schema(type = SchemaType.INTEGER, format = "int32")
private Integer stockQuantity;
```

### 4. Enumeration with Nullable
```java
@Schema(
    enumeration = {"ELECTRONICS", "CLOTHING", "BOOKS", "HOME_GARDEN", "SPORTS", "TOYS", "FOOD", "BEAUTY"},
    nullable = true
)
private String category;
```

### 5. Array Constraints
```java
@Schema(
    type = SchemaType.ARRAY,
    implementation = Product.class,
    minItems = 0,
    maxItems = 10000
)
```

## Building and Running

### Prerequisites
- Java 21
- Maven 3.8+

### Build
```bash
cd /workspaces/microprofile-tutorial/code/chapter04/mp-ecomm-store
mvn clean package
```

### Run
```bash
mvn liberty:run
```

The application will start on:
- HTTP: `http://localhost:5050/mp-ecomm-store`

### Access OpenAPI Specification

```bash
# JSON format
curl http://localhost:5050/mp-ecomm-store/openapi

# YAML format
curl http://localhost:5050/mp-ecomm-store/openapi?format=yaml

# Save to file
curl http://localhost:5050/mp-ecomm-store/openapi > openapi.json
```

### Interactive API Documentation

Open your browser to:
```
http://localhost:5050/mp-ecomm-store/openapi/ui
```

The interactive UI displays:
- All JSON Schema constraints
- Pattern validation examples
- Numeric constraint boundaries
- Enumeration dropdowns
- Format specifications
- Example values
- Rich descriptions

## Sample Data

The application comes with 3 sample products:

1. **iPhone 15 Pro**
   - ID: 1
   - Price: $999.99
   - SKU: APL-IPH15P-256
   - Category: ELECTRONICS

2. **MacBook Air M3**
   - ID: 2
   - Price: $1,299.99
   - SKU: APL-MBA-M3-13
   - Category: ELECTRONICS

3. **Samsung Galaxy S24**
   - ID: 3
   - Price: $1,199.99
   - SKU: SAM-GAL-S24-512
   - Category: ELECTRONICS

## Validation Examples

### Valid Product
```json
{
  "name": "Sony WH-1000XM5",
  "description": "Sony WH-1000XM5 Wireless Noise-Canceling Headphones",
  "price": 399.99,
  "sku": "SON-WH1-XM5-BLK",
  "category": "ELECTRONICS",
  "stockQuantity": 15,
  "inStock": true
}
```

### Invalid Examples

**Invalid SKU (fails pattern):**
```json
{"sku": "invalid-sku"}
```
❌ Error: Must match `^[A-Z]{3}-[A-Z0-9]+-[A-Z0-9]+$`

**Invalid Price (fails minimum):**
```json
{"price": 0.00}
```
❌ Error: Must be > 0.01 (exclusiveMinimum)

**Invalid Category (not in enum):**
```json
{"category": "INVALID"}
```
❌ Error: Must be one of: ELECTRONICS, CLOTHING, BOOKS, etc.

**Invalid Name (too long):**
```json
{"name": "A".repeat(101)}
```
❌ Error: maxLength is 100

## OpenAPI v3.1 Benefits

### 1. Single Source of Truth
- Same schema for API documentation and JSON validation
- No duplication between docs and validation rules

### 2. Better Validation
- More intuitive `exclusiveMinimum` syntax
- Precise decimal control with `multipleOf`
- Full regex pattern support
- Format specifications for all types

### 3. Improved Tooling
- Works with any JSON Schema 2020-12 validator
- Better code generation
- Enhanced IDE support
- Richer documentation

### 4. Standard Compliance
- Full JSON Schema 2020-12 specification
- No proprietary extensions
- Better interoperability
- Future-proof

## Comparison: OpenAPI 3.0 vs 3.1

| Feature | OpenAPI 3.0 | OpenAPI 3.1 |
|---------|-------------|-------------|
| Schema Foundation | JSON Schema subset | Full JSON Schema 2020-12 |
| Nullable | Custom extension | Native support |
| exclusiveMinimum | Boolean flag | Numeric value (intuitive) |
| Pattern | Basic regex | Full regex support |
| Tooling | OpenAPI-specific | Any JSON Schema tool |

## Testing with JSON Schema Validators

### JavaScript (Ajv)
```bash
npm install ajv ajv-formats

# Use the OpenAPI schema directly with Ajv
```

### Python (jsonschema)
```bash
pip install jsonschema

# Validate against OpenAPI schemas
```

### Java (JSON Schema Validator)
```xml
<dependency>
    <groupId>com.networknt</groupId>
    <artifactId>json-schema-validator</artifactId>
    <version>1.0.87</version>
</dependency>
```

## Code Generation

The OpenAPI v3.1 spec generates better client code:

### TypeScript
```typescript
interface Product {
  readonly id: number;  // format: int64, readOnly
  name: string;  // required, 1-100 chars, pattern
  price: number;  // 0.01-999999.99, multipleOf: 0.01
  sku?: string;  // pattern: ^[A-Z]{3}-[A-Z0-9]+-[A-Z0-9]+$
  category?: "ELECTRONICS" | "CLOTHING" | "BOOKS" | "HOME_GARDEN" | "SPORTS" | "TOYS" | "FOOD" | "BEAUTY";
  inStock?: boolean;  // default: true
}
```

### Java
```java
public class Product {
    @Min(1)
    private Long id;
    
    @Pattern(regexp = "^[a-zA-Z0-9\\s\\-]+$")
    @Size(min = 1, max = 100)
    private String name;
    
    @DecimalMin("0.01")
    @DecimalMax("999999.99")
    private Double price;
    
    // Full Bean Validation annotations
}
```

## Differences from Chapter 2

This enhanced version adds:
- ✅ Comprehensive OpenAPI v3.1 annotations
- ✅ JSON Schema 2020-12 validation
- ✅ Additional fields (sku, category, stockQuantity, inStock)
- ✅ Search endpoint with advanced filtering
- ✅ POST endpoint for creating products
- ✅ GET by ID endpoint
- ✅ Full API documentation with examples
- ✅ Interactive OpenAPI UI

## Related Documentation

- **Chapter 4 Catalog Service**: More complex example with database persistence
- **OPENAPI_V3.1_DEMO.md**: Complete feature demonstration
- **OPENAPI_COMPARISON.md**: Detailed 3.0 vs 3.1 comparison
- **QUICK_REFERENCE.md**: Quick reference card

## Resources

- [OpenAPI v3.1 Specification](https://spec.openapis.org/oas/v3.1.0)
- [JSON Schema 2020-12](https://json-schema.org/draft/2020-12/json-schema-core.html)
- [MicroProfile OpenAPI](https://github.com/eclipse/microprofile-open-api)

## Summary

This e-commerce store demonstrates how MicroProfile OpenAPI 4.1's alignment with OpenAPI v3.1 and JSON Schema 2020-12 provides:

✅ More precise validation with patterns, formats, and constraints  
✅ Better documentation with rich examples and descriptions  
✅ Improved tooling by leveraging JSON Schema ecosystem  
✅ Standard compliance with JSON Schema 2020-12  
✅ Enhanced developer experience with type-safe code generation  
✅ Single source of truth for API contracts and validation  

A simple yet powerful demonstration of modern API specification capabilities!
