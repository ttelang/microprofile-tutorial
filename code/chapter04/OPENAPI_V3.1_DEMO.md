# MicroProfile OpenAPI 4.1 - OpenAPI v3.1 Demonstration

This chapter demonstrates **MicroProfile OpenAPI 4.1** alignment with **OpenAPI v3.1** specification and **JSON Schema 2020-12**, showcasing advanced schema validation, improved nullable handling, and enhanced API documentation capabilities.

## Overview

MicroProfile OpenAPI 4.1 brings full OpenAPI v3.1 support, which represents a significant evolution from OpenAPI 3.0 by fully aligning with JSON Schema 2020-12. This alignment provides:

- **Single Source of Truth**: Same schema definitions work for both OpenAPI documentation and JSON Schema validation
- **Better Validation**: More precise constraint expressions and validation rules
- **Improved Tooling**: Leverage the entire JSON Schema ecosystem
- **Standard Compliance**: Full compatibility with JSON Schema 2020-12 specification

## Key Features Demonstrated

### 1. JSON Schema 2020-12 Validation Features

#### Pattern-Based String Validation
```java
@Schema(
    description = "Stock Keeping Unit - unique product identifier",
    pattern = "^[A-Z]{3}-[A-Z0-9]+-[A-Z0-9]+$",
    example = "APL-IPH15P-256"
)
private String sku;
```

**Example values:**
- ✅ Valid: `APL-IPH15P-256`, `SAM-GAL-S24-512`
- ❌ Invalid: `apple-123`, `APL_IPH_256`, `123-ABC-XYZ`

#### Numeric Constraints with Exclusive Bounds
```java
@Schema(
    description = "Product price in USD",
    minimum = "0.01",
    maximum = "999999.99",
    multipleOf = 0.01,
    exclusiveMinimum = true
)
private Double price;
```

**Key differences from OpenAPI 3.0:**
- OpenAPI 3.0: `exclusiveMinimum: true` (boolean flag)
- OpenAPI 3.1: `minimum: "0.01", exclusiveMinimum: true` (more intuitive)

#### String Length Constraints
```java
@Schema(
    description = "Product name",
    minLength = 1,
    maxLength = 100,
    pattern = "^[a-zA-Z0-9\\s\\-]+$"
)
private String name;
```

### 2. Format Specifications

OpenAPI v3.1 provides standardized format specifications for precise data validation:

#### Numeric Formats
```java
// Integer formats
@Schema(type = SchemaType.INTEGER, format = "int32")  // -2^31 to 2^31-1
@Schema(type = SchemaType.INTEGER, format = "int64")  // -2^63 to 2^63-1

// Decimal formats
@Schema(type = SchemaType.NUMBER, format = "double")  // Double precision
@Schema(type = SchemaType.NUMBER, format = "float")   // Single precision
```

#### Date/Time Formats
```java
@Schema(
    type = SchemaType.STRING,
    format = "date-time",  // ISO 8601: 2026-02-01T14:20:00Z
    readOnly = true
)
private LocalDateTime createdAt;
```

**Supported formats:**
- `date-time`: `2026-02-01T14:20:00Z`
- `date`: `2026-02-01`
- `time`: `14:20:00`

#### Other Standard Formats
- `email`: Email address validation
- `uri` / `url`: URI/URL validation
- `uuid`: UUID format (e.g., `123e4567-e89b-12d3-a456-426614174000`)
- `binary`: Binary data
- `byte`: Base64 encoded data

### 3. Enhanced Schema Features

#### Nullable Handling (Aligned with JSON Schema)
```java
@Schema(
    description = "Product description",
    nullable = true,  // Can be null
    maxLength = 500
)
private String description;
```

**OpenAPI 3.0 vs 3.1:**
- 3.0: `nullable: true` was a custom extension
- 3.1: `nullable` is native JSON Schema, fully standardized

#### Enumeration Support
```java
@Schema(
    description = "Product category",
    enumeration = {"ELECTRONICS", "CLOTHING", "BOOKS", "HOME_GARDEN", "SPORTS", "TOYS"},
    example = "ELECTRONICS"
)
private String category;
```

#### Default Values
```java
@Schema(
    description = "Available quantity in stock",
    defaultValue = "0",
    minimum = "0"
)
private Integer stockQuantity;
```

#### Read-Only Properties
```java
@Schema(
    description = "Unique identifier",
    readOnly = true,  // Cannot be modified by clients
    minimum = "1"
)
private Long id;
```

### 4. Array Schema Constraints

```java
@APIResponse(
    content = @Content(
        schema = @Schema(
            type = SchemaType.ARRAY,
            implementation = Product.class,
            minItems = 0,
            maxItems = 10000,
            uniqueItems = false
        )
    )
)
```

### 5. Advanced Parameter Validation

Query parameters with full JSON Schema validation:

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

## Comparison: OpenAPI 3.0 vs OpenAPI 3.1

| Feature | OpenAPI 3.0 | OpenAPI 3.1 (JSON Schema 2020-12) |
|---------|-------------|-----------------------------------|
| **Schema Specification** | JSON Schema subset (draft-05) | Full JSON Schema 2020-12 |
| **Nullable** | `nullable: true` (custom extension) | Native JSON Schema support |
| **exclusiveMinimum** | Boolean flag: `minimum: 0, exclusiveMinimum: true` | Numeric value: `minimum: "0.01", exclusiveMinimum: true` |
| **Pattern** | Basic regex support | Full regex with anchors and advanced features |
| **Examples** | Single `example` property | Multiple `examples` with `exampleSetFlag` |
| **Const** | Not supported | `const` keyword for constant values |
| **$schema** | Not allowed in schemas | Allowed and recommended |
| **Webhooks** | Not supported | Native webhook support |
| **Schema Composition** | Limited `allOf`, `anyOf`, `oneOf` | Full composition with better semantics |

## Testing the API

### 1. Build and Run

```bash
cd /workspaces/microprofile-tutorial/code/chapter04/catalog
mvn clean package liberty:run
```

### 2. Access OpenAPI Specification

```bash
# View in JSON format
curl http://localhost:5050/catalog/openapi

# View in YAML format
curl http://localhost:5050/catalog/openapi?format=yaml

# Save to file
curl http://localhost:5050/catalog/openapi > openapi.json
```

### 3. Interactive API Documentation

Open your browser to:
```
http://localhost:5050/catalog/openapi/ui
```

The interactive UI will show:
- All JSON Schema constraints in action
- Pattern validation examples
- Numeric constraint boundaries
- Enumeration dropdowns
- Format specifications
- Example values
- Rich descriptions

### 4. Test API Endpoints

#### Get all products
```bash
curl http://localhost:5050/catalog/api/products
```

#### Get product by ID (demonstrates int64 format)
```bash
curl http://localhost:5050/catalog/api/products/1
```

#### Search with price range (demonstrates exclusiveMinimum)
```bash
# Products with price > $500 and <= $2000
curl "http://localhost:5050/catalog/api/products/search?minPrice=500&maxPrice=2000"
```

#### Search by category (demonstrates enumeration)
```bash
curl "http://localhost:5050/catalog/api/products/search?category=ELECTRONICS"
```

#### Search with pagination (demonstrates default values)
```bash
curl "http://localhost:5050/catalog/api/products/search?page=0&size=10"
```

#### Search by name (demonstrates pattern matching)
```bash
curl "http://localhost:5050/catalog/api/products/search?name=iPhone"
```

#### Create product (demonstrates validation)
```bash
curl -X POST http://localhost:5050/catalog/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Samsung Galaxy S24",
    "description": "Samsung Galaxy S24 Ultra with 512GB storage",
    "price": 1299.99,
    "sku": "SAM-GAL-S24-512",
    "category": "ELECTRONICS",
    "stockQuantity": 30,
    "rating": 4.7,
    "weight": 0.234,
    "manufacturer": "Samsung Electronics",
    "warrantyMonths": 24
  }'
```

## JSON Schema Validation Tools

### Using OpenAPI CLI Tools

Install and validate:
```bash
npm install -g @redocly/cli
redocly lint http://localhost:5050/catalog/openapi
```

### Using Spectral for Linting

```bash
npm install -g @stoplight/spectral-cli
spectral lint http://localhost:5050/catalog/openapi
```

### Using JSON Schema Validator Libraries

#### JavaScript (Ajv - supports JSON Schema 2020-12)
```bash
npm install ajv ajv-formats
```

```javascript
const Ajv = require("ajv");
const addFormats = require("ajv-formats");

const ajv = new Ajv();
addFormats(ajv);

// Use OpenAPI schemas directly with Ajv
const validate = ajv.compile(productSchema);
const valid = validate(productData);
```

#### Python (jsonschema)
```bash
pip install jsonschema
```

```python
import jsonschema
from jsonschema import validate

# Use OpenAPI schemas directly
validate(instance=product_data, schema=product_schema)
```

#### Java (JSON Schema Validator)
```xml
<dependency>
    <groupId>com.networknt</groupId>
    <artifactId>json-schema-validator</artifactId>
    <version>1.0.87</version>
</dependency>
```

## Benefits of OpenAPI v3.1

### 1. Single Source of Truth
Use the same schema for:
- API documentation
- Client code generation
- Server-side validation
- Contract testing
- Mock server generation

### 2. Better Validation
More precise constraints:
- `exclusiveMinimum` as numeric value (more intuitive)
- `multipleOf` for decimal precision
- Full regex pattern support
- Strict format validation

### 3. Improved Tooling
Leverage existing JSON Schema tools:
- Validators (Ajv, jsonschema, etc.)
- Code generators
- Testing frameworks
- Documentation generators

### 4. Standard Compliance
- Full JSON Schema 2020-12 specification
- No proprietary extensions needed
- Better interoperability
- Future-proof

### 5. Enhanced Developer Experience
- Richer API documentation
- Better IDE support
- Type-safe code generation
- Clear validation errors

## Product Entity Schema Features

The `Product` entity demonstrates all major JSON Schema 2020-12 features:

| Field | Schema Feature | Example |
|-------|---------------|---------|
| `id` | `readOnly`, `minimum`, `format: int64` | `1` |
| `name` | `minLength`, `maxLength`, `pattern` | `"iPhone 15 Pro"` |
| `description` | `nullable`, `maxLength` | `"Apple iPhone..."` |
| `price` | `minimum`, `maximum`, `exclusiveMinimum`, `multipleOf` | `999.99` |
| `sku` | `pattern`, `minLength`, `maxLength` | `"APL-IPH15P-256"` |
| `category` | `enumeration`, `nullable` | `"ELECTRONICS"` |
| `stockQuantity` | `minimum`, `defaultValue`, `format: int32` | `50` |
| `rating` | `minimum`, `maximum`, `nullable` | `4.8` |
| `weight` | `minimum`, `exclusiveMinimum`, `nullable` | `0.187` |
| `active` | `defaultValue`, `type: boolean` | `true` |
| `manufacturer` | `maxLength`, `nullable` | `"Apple Inc."` |
| `warrantyMonths` | `minimum`, `maximum`, `nullable` | `12` |
| `createdAt` | `readOnly`, `format: date-time` | `"2026-01-15T10:30:00"` |
| `updatedAt` | `readOnly`, `format: date-time` | `"2026-02-01T14:20:00"` |

## Code Generation

OpenAPI v3.1 schemas generate better client code:

### TypeScript (openapi-typescript)
```typescript
interface Product {
  id: number;  // int64, minimum: 1
  name: string;  // minLength: 1, maxLength: 100, pattern: ^[a-zA-Z0-9\s\-]+$
  price: number;  // minimum: 0.01, maximum: 999999.99, multipleOf: 0.01
  category?: "ELECTRONICS" | "CLOTHING" | "BOOKS" | "HOME_GARDEN" | "SPORTS" | "TOYS";
  active?: boolean;  // default: true
  readonly createdAt?: string;  // format: date-time
}
```

### Java (openapi-generator)
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
    
    // ... with full Bean Validation annotations
}
```

## Best Practices

### 1. Always Specify Format
```java
@Schema(type = SchemaType.NUMBER, format = "double")  // ✅ Good
@Schema(type = SchemaType.NUMBER)  // ❌ Ambiguous
```

### 2. Use Patterns for String Validation
```java
@Schema(pattern = "^[A-Z]{3}-[A-Z0-9]+-[A-Z0-9]+$")  // ✅ Precise
@Schema()  // ❌ No validation
```

### 3. Document with Examples
```java
@Schema(
    description = "Product price in USD",
    example = "999.99"  // ✅ Shows expected format
)
```

### 4. Use Read-Only for Server-Generated Fields
```java
@Schema(readOnly = true)  // ✅ Prevents client modification
private Long id;
```

### 5. Specify Nullable Explicitly
```java
@Schema(nullable = true)  // ✅ Clear intent
private String description;
```

## Additional Resources

- [OpenAPI v3.1 Specification](https://spec.openapis.org/oas/v3.1.0)
- [JSON Schema 2020-12](https://json-schema.org/draft/2020-12/json-schema-core.html)
- [MicroProfile OpenAPI](https://github.com/eclipse/microprofile-open-api)
- [JSON Schema Validator](https://www.jsonschemavalidator.net/)

## Summary

This demonstration shows how MicroProfile OpenAPI 4.1's alignment with OpenAPI v3.1 and JSON Schema 2020-12 provides:

✅ **More precise validation** with patterns, formats, and constraints  
✅ **Better documentation** with rich examples and descriptions  
✅ **Improved tooling** by leveraging JSON Schema ecosystem  
✅ **Standard compliance** with JSON Schema 2020-12  
✅ **Enhanced developer experience** with type-safe code generation  
✅ **Single source of truth** for API contracts and validation  

The combination of these features makes OpenAPI v3.1 a significant improvement over 3.0, providing better interoperability, more robust validation, and enhanced API development capabilities.
