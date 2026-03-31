# Testing @NumberFormat and @DateFormat

## Implementation Complete ✅

The Product entity now includes formatted scalar fields using MicroProfile GraphQL annotations.

## What Was Implemented:

### 1. **@NumberFormat on Price Field**
```java
@NonNull
@Description("Product price in USD")
@NumberFormat(value = "$ #,##0.00", locale = "en-US")
private Double price;
```

**Result**: Price values are returned as formatted strings like `"$ 999.99"` instead of raw numbers.

### 2. **@DateFormat on ReleaseDate Field**
```java
@Description("Product release date")
@DateFormat(value = "dd MMM yyyy")
private LocalDate releaseDate;
```

**Result**: Dates are returned as formatted strings like `"31 Mar 2026"` instead of ISO-8601 format.

### 3. **@DateFormat on Query Method**
```java
@Query("productReleaseDate")
@Description("Returns the release date of a product in formatted string")
@DateFormat(value = "dd MMM yyyy")
public LocalDate getProductReleaseDate(@Name("id") Long id) {
    Product product = productService.findById(id);
    return product != null ? product.getReleaseDate() : null;
}
```

**Result**: Method return values are also formatted according to the annotation.

## Testing in GraphiQL

### 1. Query Product with Formatted Fields

```graphql
query {
  products {
    id
    name
    price           # Returns formatted: "$ 999.99"
    releaseDate     # Returns formatted: "31 Mar 2026"
    category
    stockQuantity
  }
}
```

**Expected Response:**
```json
{
  "data": {
    "products": [
      {
        "id": 1,
        "name": "Laptop",
        "price": "$ 999.99",
        "releaseDate": "31 Mar 2026",
        "category": "Electronics",
        "stockQuantity": 50
      }
    ]
  }
}
```

### 2. Query Specific Product Release Date

```graphql
query {
  productReleaseDate(id: 1)  # Returns formatted date
}
```

**Expected Response:**
```json
{
  "data": {
    "productReleaseDate": "31 Mar 2026"
  }
}
```

### 3. Compare Formatted vs Raw Data

```graphql
query {
  product(id: 1) {
    name
    price           # Formatted: "$ 999.99"
    releaseDate     # Formatted: "31 Mar 2026"
  }
}
```

## Important Notes:

### ⚠️ Type Change in GraphQL Schema

When `@NumberFormat` or `@DateFormat` is applied:
- Field type changes from `Float` → `String` (for numbers)
- Field type changes from `Date` → `String` (for dates)

### GraphQL Schema Generated:

```graphql
type Product {
  id: BigInteger
  name: String!
  price: String!          # Changed to String due to @NumberFormat
  releaseDate: String     # Changed to String due to @DateFormat
  category: String
  stockQuantity: Int
  # ...
}

type Query {
  productReleaseDate(id: BigInteger): String  # Returns formatted String
  # ...
}
```

## Format Patterns:

### NumberFormat Patterns:
- `"$ #,##0.00"` → `"$ 1,234.56"`
- `"#,##0.00"` → `"1,234.56"`
- `"#.00"` → `"1234.56"`

### DateFormat Patterns:
- `"dd MMM yyyy"` → `"31 Mar 2026"`
- `"yyyy-MM-dd"` → `"2026-03-31"`
- `"MM/dd/yyyy"` → `"03/31/2026"`
- `"MMMM d, yyyy"` → `"March 31, 2026"`

## Benefits:

✅ **Client-Side Simplification**: Clients receive pre-formatted data  
✅ **Consistent Formatting**: Server controls presentation format  
✅ **Locale Support**: NumberFormat supports locale-specific formatting  
✅ **Less Client Code**: No formatting logic needed on client  
✅ **Backward Compatible**: Can add formatting without breaking schema structure  

## Testing Steps:

1. Start the application:
   ```bash
   cd /workspaces/microprofile-tutorial/code/chapter12/catalog
   mvn liberty:dev
   ```

2. Open GraphiQL: http://localhost:9080/graphql-ui

3. Run the queries above to see formatted output!

## Schema Introspection:

Check the actual types in the schema:

```graphql
query {
  __type(name: "Product") {
    fields {
      name
      type {
        name
        kind
      }
    }
  }
}
```

You'll see `price` and `releaseDate` have type `String` instead of `Float` and `Date`.

---

🎉 **Implementation Complete & Ready to Test!**
