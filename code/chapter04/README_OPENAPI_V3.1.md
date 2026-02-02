# Chapter 04 - MicroProfile OpenAPI 4.1 Implementation Summary

## Overview

This chapter demonstrates **MicroProfile OpenAPI 4.1** alignment with **OpenAPI v3.1 specification** and **JSON Schema 2020-12**, showcasing enhanced API documentation, validation, and schema capabilities.

## Files Modified/Created

### Enhanced Java Files

1. **`Product.java`** - Entity with comprehensive OpenAPI v3.1 annotations
   - Added 10 new fields with JSON Schema 2020-12 features
   - Demonstrates: pattern validation, numeric constraints, format specifications, nullable handling
   - Added lifecycle callbacks (@PrePersist, @PreUpdate)
   - Full schema documentation with examples

2. **`ProductResource.java`** - REST endpoints with advanced OpenAPI features
   - Enhanced with parameter validation
   - Array schema constraints
   - Search functionality with pagination
   - Comprehensive @Parameter annotations
   - Response schemas with format specifications

3. **`ProductRestApplication.java`** - OpenAPI definition
   - Comprehensive @OpenAPIDefinition with full metadata
   - Server definitions
   - Tags and external documentation
   - Rich API description with Markdown formatting

### Documentation Files

4. **`OPENAPI_V3.1_DEMO.md`** - Comprehensive feature demonstration guide
   - Complete overview of JSON Schema 2020-12 features
   - Testing instructions
   - Code examples
   - Comparison tables
   - Best practices

5. **`OPENAPI_COMPARISON.md`** - OpenAPI 3.0 vs 3.1 comparison
   - Side-by-side feature comparison
   - Migration guide
   - Real-world examples
   - Validation tool compatibility

### Test Script

6. **`test-openapi-features.sh`** - Automated testing script
   - Tests all major OpenAPI v3.1 features
   - Validates JSON Schema constraints
   - Demonstrates API endpoints
   - Verifies schema validation

## Key Features Implemented

### 1. JSON Schema 2020-12 Validation

#### Pattern-Based String Validation
```java
@Schema(
    pattern = "^[A-Z]{3}-[A-Z0-9]+-[A-Z0-9]+$",
    minLength = 5,
    maxLength = 50
)
private String sku;
```

**Validates:** SKU format like `APL-IPH15P-256`

#### Numeric Constraints with Exclusive Bounds
```java
@Schema(
    minimum = "0.01",
    maximum = "999999.99",
    exclusiveMinimum = true,
    multipleOf = 0.01
)
private Double price;
```

**Validates:** Price > $0.01, ≤ $999,999.99, rounded to cents

#### String Length Constraints
```java
@Schema(
    minLength = 1,
    maxLength = 100,
    pattern = "^[a-zA-Z0-9\\s\\-]+$"
)
private String name;
```

**Validates:** Name 1-100 chars, alphanumeric with spaces/hyphens

### 2. Format Specifications

```java
// Integer formats
@Schema(type = SchemaType.INTEGER, format = "int64")
private Long id;

@Schema(type = SchemaType.INTEGER, format = "int32")
private Integer stockQuantity;

// Numeric formats
@Schema(type = SchemaType.NUMBER, format = "double")
private Double price;

// Date/Time formats
@Schema(type = SchemaType.STRING, format = "date-time")
private LocalDateTime createdAt;
```

### 3. Enhanced Schema Features

```java
// Nullable properties
@Schema(nullable = true, maxLength = 500)
private String description;

// Enumeration
@Schema(enumeration = {"ELECTRONICS", "CLOTHING", "BOOKS", "HOME_GARDEN", "SPORTS", "TOYS"})
private String category;

// Default values
@Schema(defaultValue = "0", minimum = "0")
private Integer stockQuantity;

// Read-only properties
@Schema(readOnly = true, format = "date-time")
private LocalDateTime createdAt;
```

### 4. Array Schema Constraints

```java
@Schema(
    type = SchemaType.ARRAY,
    implementation = Product.class,
    minItems = 0,
    maxItems = 10000
)
```

### 5. Advanced Parameter Validation

```java
@Parameter(
    description = "Minimum price (exclusive)",
    schema = @Schema(
        type = SchemaType.NUMBER,
        format = "double",
        minimum = "0.00",
        exclusiveMinimum = true,
        maximum = "999999.99",
        multipleOf = 0.01,
        nullable = true,
        example = "500.00"
    )
)
@QueryParam("minPrice") Double minPrice
```

## Product Entity Schema

### Complete Field List with Validation

| Field | Type | Constraints | Format | Features |
|-------|------|-------------|--------|----------|
| `id` | Long | min: 1 | int64 | readOnly |
| `name` | String | 1-100 chars, pattern | - | required |
| `description` | String | max 500 chars | - | nullable |
| `price` | Double | 0.01-999999.99 | double | exclusiveMin, multipleOf 0.01 |
| `sku` | String | 5-50 chars, pattern | - | unique, nullable |
| `category` | String | enum | - | nullable |
| `stockQuantity` | Integer | min: 0 | int32 | default: 0 |
| `rating` | Double | 0.0-5.0 | double | nullable |
| `weight` | Double | > 0.001 kg | double | exclusiveMin, nullable |
| `active` | Boolean | - | - | default: true |
| `manufacturer` | String | max 100 chars | - | nullable |
| `warrantyMonths` | Integer | 0-120 | int32 | nullable |
| `createdAt` | LocalDateTime | - | date-time | readOnly |
| `updatedAt` | LocalDateTime | - | date-time | readOnly |

## API Endpoints

### 1. GET /api/products
**Returns:** All products (array with minItems/maxItems constraints)

### 2. GET /api/products/{id}
**Parameters:** 
- `id` (path): int64, minimum: 1

### 3. POST /api/products
**Request Body:** Product entity with full validation

### 4. PUT /api/products/{id}
**Request Body:** Updated product data

### 5. DELETE /api/products/{id}
**Returns:** 204 No Content

### 6. GET /api/products/search
**Parameters:**
- `name`: String (pattern validated)
- `description`: String
- `minPrice`: Double (exclusiveMinimum)
- `maxPrice`: Double
- `category`: Enum
- `page`: Integer (default: 0)
- `size`: Integer (default: 20, max: 100)

## Testing

### Build and Run

```bash
cd /workspaces/microprofile-tutorial/code/chapter04/catalog
mvn clean package liberty:run
```

### Access OpenAPI Specification

```bash
# JSON format
curl http://localhost:5050/catalog/openapi

# YAML format
curl http://localhost:5050/catalog/openapi?format=yaml

# Interactive UI
open http://localhost:5050/catalog/openapi/ui
```

### Run Test Script

```bash
cd /workspaces/microprofile-tutorial/code/chapter04/catalog
./test-openapi-features.sh
```

### Manual API Tests

```bash
# Get all products
curl http://localhost:5050/catalog/api/products

# Search by price range
curl "http://localhost:5050/catalog/api/products/search?minPrice=500&maxPrice=2000"

# Search by category
curl "http://localhost:5050/catalog/api/products/search?category=ELECTRONICS"

# Create product
curl -X POST http://localhost:5050/catalog/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Samsung Galaxy S24",
    "price": 1299.99,
    "sku": "SAM-GAL-S24-512",
    "category": "ELECTRONICS"
  }'
```

## Benefits Demonstrated

### 1. Single Source of Truth
- Same schema for OpenAPI docs and JSON validation
- No duplication between documentation and validation

### 2. Better Validation
- Pattern matching for SKU format
- Exclusive bounds for price ranges
- Decimal precision control (multipleOf)
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

### 5. Enhanced Developer Experience
- Clear validation rules
- Rich examples
- Better error messages
- Type-safe code generation

## OpenAPI 3.0 vs 3.1 Highlights

| Feature | OpenAPI 3.0 | OpenAPI 3.1 |
|---------|-------------|-------------|
| Schema Foundation | JSON Schema subset | Full JSON Schema 2020-12 |
| Nullable | Custom extension | Native support |
| exclusiveMinimum | Boolean flag | Numeric value (intuitive) |
| Pattern | Basic | Full regex support |
| Examples | Single example | Multiple examples |
| Const | Not supported | Supported |
| Tooling | OpenAPI-specific | Any JSON Schema tool |

## Validation Examples

### Valid Product
```json
{
  "name": "iPhone 15 Pro",
  "description": "Apple iPhone 15 Pro with 256GB storage",
  "price": 999.99,
  "sku": "APL-IPH15P-256",
  "category": "ELECTRONICS",
  "stockQuantity": 50,
  "rating": 4.8,
  "weight": 0.187,
  "manufacturer": "Apple Inc.",
  "warrantyMonths": 12
}
```

### Invalid Examples

**Invalid SKU (fails pattern):**
```json
{"sku": "invalid-format"}  // ❌ Must match ^[A-Z]{3}-[A-Z0-9]+-[A-Z0-9]+$
```

**Invalid Price (fails minimum):**
```json
{"price": 0.00}  // ❌ Must be > 0.01 (exclusiveMinimum)
```

**Invalid Category (not in enum):**
```json
{"category": "INVALID"}  // ❌ Must be ELECTRONICS, CLOTHING, etc.
```

**Invalid Name (too long):**
```json
{"name": "A".repeat(101)}  // ❌ maxLength: 100
```

## Code Generation Support

### TypeScript Client
```typescript
interface Product {
  readonly id: number;  // format: int64, readOnly
  name: string;  // required, 1-100 chars, pattern
  price: number;  // 0.01-999999.99, multipleOf: 0.01
  sku?: string;  // pattern: ^[A-Z]{3}-[A-Z0-9]+-[A-Z0-9]+$
  category?: "ELECTRONICS" | "CLOTHING" | "BOOKS" | "HOME_GARDEN" | "SPORTS" | "TOYS";
  readonly createdAt?: string;  // format: date-time
}
```

### Java Client
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
    
    // Full Bean Validation support
}
```

## Next Steps

1. **Explore the Interactive UI**: Visit http://localhost:5050/catalog/openapi/ui
2. **Review the OpenAPI Spec**: Check the generated JSON/YAML
3. **Test Validation**: Try invalid requests to see validation in action
4. **Generate Client Code**: Use openapi-generator with the spec
5. **Integrate Validators**: Use Ajv or other JSON Schema validators

## Additional Resources

- [OpenAPI v3.1 Specification](https://spec.openapis.org/oas/v3.1.0)
- [JSON Schema 2020-12](https://json-schema.org/draft/2020-12/json-schema-core.html)
- [MicroProfile OpenAPI](https://github.com/eclipse/microprofile-open-api)
- [OPENAPI_V3.1_DEMO.md](./OPENAPI_V3.1_DEMO.md) - Complete feature guide
- [OPENAPI_COMPARISON.md](./OPENAPI_COMPARISON.md) - 3.0 vs 3.1 comparison

## Summary

This implementation demonstrates how **MicroProfile OpenAPI 4.1** leverages **OpenAPI v3.1** and **JSON Schema 2020-12** to provide:

✅ More precise validation with patterns, formats, and constraints  
✅ Better documentation with rich examples and descriptions  
✅ Improved tooling by leveraging JSON Schema ecosystem  
✅ Standard compliance with JSON Schema 2020-12  
✅ Enhanced developer experience with type-safe code generation  
✅ Single source of truth for API contracts and validation  

The catalog service now serves as a comprehensive reference for implementing modern API specifications with full schema validation support.
