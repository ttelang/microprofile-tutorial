# OpenAPI 3.0 vs 3.1 - Feature Comparison

## Quick Reference Guide

This document provides a side-by-side comparison of key differences between OpenAPI 3.0 and OpenAPI 3.1, demonstrating why the upgrade to OpenAPI v3.1 (with JSON Schema 2020-12) is significant.

## Major Differences

### 1. Schema Foundation

| Aspect | OpenAPI 3.0 | OpenAPI 3.1 |
|--------|-------------|-------------|
| **Schema Specification** | Extended subset of JSON Schema Draft 5 | Full JSON Schema 2020-12 |
| **Compatibility** | Custom OpenAPI-specific extensions | 100% compatible with JSON Schema |
| **Tooling** | Limited to OpenAPI-specific tools | Any JSON Schema tool works |

### 2. Nullable Handling

#### OpenAPI 3.0 (Custom Extension)
```yaml
properties:
  description:
    type: string
    nullable: true  # Custom OpenAPI extension
```

```java
@Schema(nullable = true)  // OpenAPI-specific
private String description;
```

#### OpenAPI 3.1 (Native JSON Schema)
```yaml
properties:
  description:
    type: string
    nullable: true  # Native JSON Schema in 3.1
```

```java
@Schema(nullable = true)  // Now standard JSON Schema
private String description;
```

**Key Difference**: In 3.1, `nullable` is part of the JSON Schema standard, enabling better interoperability.

### 3. Exclusive Minimum/Maximum

#### OpenAPI 3.0 (Boolean Flag)
```yaml
properties:
  price:
    type: number
    minimum: 0
    exclusiveMinimum: true  # Boolean flag - confusing!
```

```java
@Schema(minimum = "0", exclusiveMinimum = true)
private Double price;
```

**Problem**: The boolean approach was confusing - does `exclusiveMinimum: true` apply to `minimum` or is it a separate value?

#### OpenAPI 3.1 (Numeric Value - More Intuitive)
```yaml
properties:
  price:
    type: number
    minimum: 0.01
    exclusiveMinimum: true  # Much clearer: price > 0.01
```

```java
@Schema(
    minimum = "0.01",
    exclusiveMinimum = true,  // Clearer: price > 0.01
    maximum = "999999.99"
)
private Double price;
```

**Benefit**: More intuitive - `exclusiveMinimum: true` with `minimum: 0.01` clearly means "greater than 0.01".

### 4. Pattern Validation

#### OpenAPI 3.0 (Limited)
```yaml
properties:
  sku:
    type: string
    pattern: "^[A-Z]{3}-[A-Z0-9]+-[A-Z0-9]+$"  # Basic support
```

#### OpenAPI 3.1 (Full Regex Support)
```yaml
properties:
  sku:
    type: string
    pattern: "^[A-Z]{3}-[A-Z0-9]+-[A-Z0-9]+$"  # Full regex with anchors
    minLength: 5
    maxLength: 50
```

```java
@Schema(
    pattern = "^[A-Z]{3}-[A-Z0-9]+-[A-Z0-9]+$",
    minLength = 5,
    maxLength = 50
)
private String sku;
```

**Benefit**: Full regex support with proper anchor handling and better validation.

### 5. Examples

#### OpenAPI 3.0 (Single Example)
```yaml
properties:
  category:
    type: string
    example: "ELECTRONICS"
```

#### OpenAPI 3.1 (Multiple Examples)
```yaml
properties:
  category:
    type: string
    examples:
      - "ELECTRONICS"
      - "CLOTHING"
      - "BOOKS"
```

### 6. Const Keyword

#### OpenAPI 3.0 (Not Supported)
```yaml
properties:
  type:
    type: string
    enum: ["product"]  # Only way to specify constant
```

#### OpenAPI 3.1 (Const Support)
```yaml
properties:
  type:
    const: "product"  # More semantic for constant values
```

### 7. Schema Composition

#### OpenAPI 3.0 (Limited)
```yaml
allOf:
  - $ref: '#/components/schemas/BaseProduct'
  - type: object
    properties:
      additionalField: string
```

#### OpenAPI 3.1 (Enhanced)
```yaml
allOf:
  - $ref: '#/components/schemas/BaseProduct'
  - type: object
    properties:
      additionalField: string
    unevaluatedProperties: false  # New in 3.1
```

## JSON Schema 2020-12 Features in OpenAPI 3.1

### New Constraints Available

| Feature | Description | Example |
|---------|-------------|---------|
| `minContains` / `maxContains` | Array item count constraints | `minContains: 1` |
| `prefixItems` | Position-based array validation | `prefixItems: [schema1, schema2]` |
| `unevaluatedProperties` | Strict property validation | `unevaluatedProperties: false` |
| `dependentSchemas` | Conditional schema dependencies | Complex validation rules |
| `$dynamicRef` / `$dynamicAnchor` | Dynamic references | Advanced schema composition |

### Format Enhancements

#### OpenAPI 3.1 Standard Formats
```java
// Date/Time formats
@Schema(format = "date-time")  // 2026-02-01T14:20:00Z
@Schema(format = "date")       // 2026-02-01
@Schema(format = "time")       // 14:20:00
@Schema(format = "duration")   // P3Y6M4DT12H30M5S

// String formats
@Schema(format = "email")      // user@example.com
@Schema(format = "hostname")   // www.example.com
@Schema(format = "uri")        // https://example.com/path
@Schema(format = "uuid")       // 123e4567-e89b-12d3-a456-426614174000

// Numeric formats
@Schema(format = "int32")      // 32-bit integer
@Schema(format = "int64")      // 64-bit integer
@Schema(format = "float")      // Single precision
@Schema(format = "double")     // Double precision
```

## Real-World Examples

### Example 1: Price with Decimal Precision

#### OpenAPI 3.0
```yaml
price:
  type: number
  minimum: 0
  exclusiveMinimum: true
```

**Issues:**
- Ambiguous exclusive minimum
- No decimal precision control
- No maximum constraint

#### OpenAPI 3.1
```yaml
price:
  type: number
  format: double
  minimum: 0.01
  exclusiveMinimum: true
  maximum: 999999.99
  multipleOf: 0.01
```

```java
@Schema(
    type = SchemaType.NUMBER,
    format = "double",
    minimum = "0.01",
    exclusiveMinimum = true,
    maximum = "999999.99",
    multipleOf = 0.01
)
private Double price;
```

**Benefits:**
- Clear exclusive minimum (price > $0.01)
- Decimal precision (rounded to cents)
- Maximum value constraint
- Format specification

### Example 2: Enum vs Enumeration

#### OpenAPI 3.0
```yaml
category:
  type: string
  enum:
    - ELECTRONICS
    - CLOTHING
    - BOOKS
```

#### OpenAPI 3.1 (MicroProfile)
```java
@Schema(
    description = "Product category",
    enumeration = {"ELECTRONICS", "CLOTHING", "BOOKS", "HOME_GARDEN", "SPORTS", "TOYS"},
    example = "ELECTRONICS",
    nullable = true
)
private String category;
```

**Benefits:**
- Native nullable support
- Better documentation
- Clear examples
- Type-safe validation

### Example 3: Array Validation

#### OpenAPI 3.0
```yaml
products:
  type: array
  items:
    $ref: '#/components/schemas/Product'
```

**Missing:** Item count constraints, uniqueness

#### OpenAPI 3.1
```yaml
products:
  type: array
  items:
    $ref: '#/components/schemas/Product'
  minItems: 0
  maxItems: 10000
  uniqueItems: false
```

```java
@Schema(
    type = SchemaType.ARRAY,
    implementation = Product.class,
    minItems = 0,
    maxItems = 10000,
    uniqueItems = false
)
```

**Benefits:**
- Size constraints
- Uniqueness control
- Better validation

## Validation Tool Compatibility

### OpenAPI 3.0
- ✅ OpenAPI-specific validators only
- ❌ Cannot use standard JSON Schema validators
- ❌ Limited tool ecosystem

### OpenAPI 3.1
- ✅ OpenAPI validators
- ✅ **Any JSON Schema 2020-12 validator**
- ✅ Ajv, jsonschema, JSON Schema Validator
- ✅ Much larger tool ecosystem

## Code Generation Improvements

### OpenAPI 3.0 Generated Code
```typescript
// Less precise types
interface Product {
  id?: number;
  name?: string;
  price?: number;
  category?: string;
}
```

### OpenAPI 3.1 Generated Code
```typescript
// More precise types with validation
interface Product {
  readonly id: number;  // readOnly, format: int64
  name: string;  // required, minLength: 1, maxLength: 100
  price: number;  // min: 0.01, max: 999999.99, multipleOf: 0.01
  category?: "ELECTRONICS" | "CLOTHING" | "BOOKS" | "HOME_GARDEN" | "SPORTS" | "TOYS";
  readonly createdAt?: string;  // format: date-time, readOnly
}
```

**Benefits:**
- More accurate types
- Better IDE autocomplete
- Compile-time validation
- Clear nullability

## Migration Guide: 3.0 → 3.1

### Step 1: Update Schema Version
```yaml
# Before (OpenAPI 3.0)
openapi: 3.0.3

# After (OpenAPI 3.1)
openapi: 3.1.0
```

### Step 2: Update Exclusive Minimum/Maximum
```yaml
# Before (3.0)
minimum: 0
exclusiveMinimum: true

# After (3.1) - More intuitive
minimum: 0.01
exclusiveMinimum: true
```

### Step 3: Add Format Specifications
```java
// Before (3.0)
@Schema(type = SchemaType.NUMBER)

// After (3.1) - Add format
@Schema(type = SchemaType.NUMBER, format = "double")
```

### Step 4: Enhance Array Schemas
```java
// Before (3.0)
@Schema(type = SchemaType.ARRAY, implementation = Product.class)

// After (3.1) - Add constraints
@Schema(
    type = SchemaType.ARRAY,
    implementation = Product.class,
    minItems = 0,
    maxItems = 1000
)
```

## Performance Comparison

| Aspect | OpenAPI 3.0 | OpenAPI 3.1 |
|--------|-------------|-------------|
| **Schema Validation** | Custom validator needed | Standard JSON Schema validators |
| **Code Generation** | OpenAPI-specific generators | Any JSON Schema generator |
| **Documentation** | OpenAPI UI only | OpenAPI UI + JSON Schema tools |
| **Testing** | Limited tools | Full JSON Schema testing ecosystem |

## Summary

### Why OpenAPI 3.1 is Better

✅ **Full JSON Schema 2020-12 compatibility**
- Use any JSON Schema tool
- Single source of truth
- Better validation

✅ **More intuitive syntax**
- Clearer exclusive bounds
- Better nullable handling
- Improved examples

✅ **Richer validation**
- Decimal precision (multipleOf)
- Pattern validation
- Format specifications
- Array constraints

✅ **Better tooling**
- Larger ecosystem
- More validators
- Better generators
- Enhanced testing

✅ **Future-proof**
- Standard compliance
- Active development
- Industry adoption
- Long-term support

### Key Takeaways

1. **OpenAPI 3.1 = OpenAPI + JSON Schema 2020-12**
2. **More precise validation with better syntax**
3. **Leverage the entire JSON Schema ecosystem**
4. **Better code generation and type safety**
5. **Single source of truth for API contracts**

### Recommendation

🎯 **Use OpenAPI 3.1 for new projects** - Better validation, tooling, and future support

🔄 **Migrate from 3.0 to 3.1** - Relatively straightforward with significant benefits

📚 **Learn JSON Schema 2020-12** - It's the foundation of OpenAPI 3.1 schemas
