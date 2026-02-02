# OpenAPI v3.1 Quick Reference Card

## MicroProfile OpenAPI 4.1 - JSON Schema 2020-12 Features

### Quick Start

```bash
# Build and run
cd /workspaces/microprofile-tutorial/code/chapter04/catalog
mvn clean package liberty:run

# View OpenAPI spec
curl http://localhost:5050/catalog/openapi

# Interactive UI
http://localhost:5050/catalog/openapi/ui

# Run tests
./test-openapi-features.sh
```

### Essential Annotations

#### 1. String Validation
```java
@Schema(
    description = "Product name",
    minLength = 1,
    maxLength = 100,
    pattern = "^[a-zA-Z0-9\\s\\-]+$",
    example = "iPhone 15 Pro"
)
private String name;
```

#### 2. Numeric Validation
```java
@Schema(
    description = "Product price",
    type = SchemaType.NUMBER,
    format = "double",
    minimum = "0.01",
    maximum = "999999.99",
    exclusiveMinimum = true,
    multipleOf = 0.01,
    example = "999.99"
)
private Double price;
```

#### 3. Integer Validation
```java
@Schema(
    description = "Stock quantity",
    type = SchemaType.INTEGER,
    format = "int32",
    minimum = "0",
    defaultValue = "0",
    example = "50"
)
private Integer stockQuantity;
```

#### 4. Enum Validation
```java
@Schema(
    description = "Product category",
    enumeration = {"ELECTRONICS", "CLOTHING", "BOOKS"},
    example = "ELECTRONICS",
    nullable = true
)
private String category;
```

#### 5. Date/Time Validation
```java
@Schema(
    description = "Creation timestamp",
    type = SchemaType.STRING,
    format = "date-time",
    readOnly = true,
    example = "2026-02-01T14:20:00"
)
private LocalDateTime createdAt;
```

#### 6. Array Validation
```java
@Schema(
    type = SchemaType.ARRAY,
    implementation = Product.class,
    minItems = 0,
    maxItems = 1000
)
```

#### 7. Parameter Validation
```java
@Parameter(
    description = "Page number",
    schema = @Schema(
        type = SchemaType.INTEGER,
        format = "int32",
        minimum = "0",
        defaultValue = "0",
        example = "0"
    )
)
@QueryParam("page") @DefaultValue("0") Integer page
```

### Format Specifications

| Type | Format | Example | Description |
|------|--------|---------|-------------|
| INTEGER | int32 | 2147483647 | 32-bit integer |
| INTEGER | int64 | 9223372036854775807 | 64-bit integer |
| NUMBER | float | 3.14 | Single precision |
| NUMBER | double | 3.14159265359 | Double precision |
| STRING | date-time | 2026-02-01T14:20:00Z | ISO 8601 |
| STRING | date | 2026-02-01 | Date only |
| STRING | time | 14:20:00 | Time only |
| STRING | email | user@example.com | Email |
| STRING | uri | https://example.com | URI |
| STRING | uuid | 123e4567-e89b-... | UUID |

### Common Patterns

#### SKU Pattern
```java
@Schema(pattern = "^[A-Z]{3}-[A-Z0-9]+-[A-Z0-9]+$")
```
Matches: `APL-IPH15P-256`, `SAM-GAL-S24-512`

#### Email Pattern
```java
@Schema(format = "email")
```

#### URL Pattern
```java
@Schema(format = "uri")
```

#### Phone Number Pattern
```java
@Schema(pattern = "^\\+?[1-9]\\d{1,14}$")
```
Matches: `+12025551234`, `5551234`

#### Alphanumeric Pattern
```java
@Schema(pattern = "^[a-zA-Z0-9]+$")
```

### Constraint Comparison

| Feature | OpenAPI 3.0 | OpenAPI 3.1 |
|---------|-------------|-------------|
| **Exclusive Min** | `minimum: 0, exclusiveMinimum: true` | `minimum: "0.01", exclusiveMinimum: true` |
| **Nullable** | Custom extension | Native JSON Schema |
| **Format** | Limited | Full JSON Schema formats |
| **Pattern** | Basic regex | Full regex with anchors |

### Real-World Examples

#### Product Price
```java
@Schema(
    minimum = "0.01",        // Must be at least $0.01
    maximum = "999999.99",   // Max $999,999.99
    exclusiveMinimum = true, // Price > $0.01 (not >=)
    multipleOf = 0.01       // Rounded to cents
)
private Double price;
```

#### Product Rating
```java
@Schema(
    minimum = "0.0",    // Min 0 stars
    maximum = "5.0",    // Max 5 stars
    nullable = true     // Can be null (no rating)
)
private Double rating;
```

#### Stock Quantity
```java
@Schema(
    minimum = "0",         // Cannot be negative
    defaultValue = "0"     // Defaults to 0
)
private Integer stockQuantity;
```

### Testing Commands

```bash
# Get all products
curl http://localhost:5050/catalog/api/products

# Get by ID
curl http://localhost:5050/catalog/api/products/1

# Search by price (demonstrates exclusiveMinimum)
curl "http://localhost:5050/catalog/api/products/search?minPrice=500&maxPrice=2000"

# Search by category (demonstrates enum)
curl "http://localhost:5050/catalog/api/products/search?category=ELECTRONICS"

# Pagination (demonstrates defaults)
curl "http://localhost:5050/catalog/api/products/search?page=0&size=10"

# Create product
curl -X POST http://localhost:5050/catalog/api/products \
  -H "Content-Type: application/json" \
  -d '{"name": "Test", "price": 99.99}'
```

### Validation Tools

```bash
# OpenAPI linting
npm install -g @redocly/cli
redocly lint http://localhost:5050/catalog/openapi

# Spectral
npm install -g @stoplight/spectral-cli
spectral lint http://localhost:5050/catalog/openapi
```

### Key Benefits

✅ **Single Source of Truth** - Same schema for docs and validation  
✅ **Better Validation** - Precise constraints with JSON Schema 2020-12  
✅ **Improved Tooling** - Works with any JSON Schema validator  
✅ **Standard Compliance** - Full JSON Schema specification  
✅ **Type Safety** - Better code generation  
✅ **Developer Experience** - Richer documentation and examples  

### Common Mistakes to Avoid

❌ **Don't** use pattern without anchors: `pattern = "[A-Z]+"`  
✅ **Do** use anchors: `pattern = "^[A-Z]+$"`

❌ **Don't** forget format: `type = SchemaType.NUMBER`  
✅ **Do** specify format: `type = SchemaType.NUMBER, format = "double"`

❌ **Don't** use vague descriptions  
✅ **Do** include examples and constraints

❌ **Don't** skip nullable when applicable  
✅ **Do** explicitly mark: `nullable = true`

❌ **Don't** use ambiguous constraints  
✅ **Do** use exclusiveMinimum for > comparisons

### Documentation Files

1. **README_OPENAPI_V3.1.md** - Implementation summary
2. **OPENAPI_V3.1_DEMO.md** - Complete feature demonstration
3. **OPENAPI_COMPARISON.md** - 3.0 vs 3.1 comparison
4. **test-openapi-features.sh** - Automated tests

### Resources

- [OpenAPI v3.1 Spec](https://spec.openapis.org/oas/v3.1.0)
- [JSON Schema 2020-12](https://json-schema.org/draft/2020-12/json-schema-core.html)
- [MicroProfile OpenAPI](https://github.com/eclipse/microprofile-open-api)
