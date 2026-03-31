# GraphQL Error Handling Examples

This document demonstrates all error handling patterns implemented in the catalog service, aligned with Chapter 12 documentation.

## 1. Runtime Exception Handling (ProductNotFoundException)

### Implementation
```java
public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(Long id) {
        super("Product not found: " + id);
    }
}
```

### Test Query
```bash
curl -X POST http://localhost:5060/graphql-catalog/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ product(id: 999) { id name } }"}'
```

### Expected Response
```json
{
  "errors": [{
    "message": "Product not found: 999",
    "locations": [{"line": 1, "column": 3}],
    "path": ["product"],
    "extensions": {
      "exception": "io.microprofile.tutorial.graphql.product.exception.ProductNotFoundException",
      "classification": "DataFetchingException"
    }
  }],
  "data": {"product": null}
}
```

## 2. Custom GraphQLException (InsufficientStockException)

### Implementation

**Note**: The MicroProfile GraphQL 2.0 API does not provide a mutable `getExtensions()` method on GraphQLException (as shown in some documentation examples). Instead, we include detailed information in the error message and store data as instance fields.

```java
public class InsufficientStockException extends GraphQLException {
    private final Long productId;
    private final int requestedQuantity;
    private final int availableQuantity;
    
    public InsufficientStockException(Long productId, int requested, int available) {
        super(String.format("Insufficient stock for product %d: requested %d, available %d",
                          productId, requested, available),
              GraphQLException.ExceptionType.DataFetchingException);
        this.productId = productId;
        this.requestedQuantity = requested;
        this.availableQuantity = available;
    }
}
```

### Test Query - Insufficient Stock
```bash
curl -X POST http://localhost:5060/graphql-catalog/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"mutation { orderProduct(productId: 1, quantity: 10) { id productId quantity status } }"}'
```

### Expected Response
```json
{
  "errors": [{
    "message": "Insufficient stock for product 1: requested 10, available 0",
    "locations": [{"line": 1, "column": 12}],
    "path": ["orderProduct"],
    "extensions": {
      "exception": "io.microprofile.tutorial.graphql.product.exception.InsufficientStockException",
      "classification": "DataFetchingException",
      "code": "insufficient-stock"
    }
  }],
  "data": {"orderProduct": null}
}
```

### Test Query - Sufficient Stock
```bash
curl -X POST http://localhost:5060/graphql-catalog/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"mutation { orderProduct(productId: 2, quantity: 5) { id productId quantity status } }"}'
```

### Expected Response
```json
{
  "data": {
    "orderProduct": {
      "id": 1,
      "productId": 2,
      "quantity": 5,
      "status": "PENDING"
    }
  }
}
```

## 3. Partial Results Pattern (specialPrice Field)

### Implementation
```java
@Description("Special promotional price (may not be available for all products)")
public Double specialPrice(@Source Product product) throws GraphQLException {
    try {
        pricingService.calculateSpecialPrice(product.getId());
        return product.getPrice() * 0.9;
    } catch (Exception e) {
        throw new GraphQLException(
            "Failed to calculate special price",
            GraphQLException.ExceptionType.DataFetchingException);
    }
}
```

### Test Query
```bash
curl -X POST http://localhost:5060/graphql-catalog/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ products { id name price specialPrice } }"}'
```

### Expected Response
```json
{
  "errors": [{
    "message": "Failed to calculate special price",
    "locations": [{"line": 1, "column": 28}],
    "path": ["products", 2, "specialPrice"],
    "extensions": {
      "exception": "org.eclipse.microprofile.graphql.GraphQLException",
      "classification": "DataFetchingException",
      "code": "graph-q-l"
    }
  }],
  "data": {
    "products": [
      {"id": 1, "name": "Laptop", "price": "999.99", "specialPrice": 899.991},
      {"id": 2, "name": "Mouse", "price": "29.99", "specialPrice": 26.991},
      {"id": 3, "name": "Keyboard", "price": "89.99", "specialPrice": null},
      {"id": 4, "name": "Monitor", "price": "399.99", "specialPrice": 359.991},
      {"id": 5, "name": "Headphones", "price": "199.99", "specialPrice": 179.991}
    ]
  }
}
```

**Note**: Product 3 (Keyboard) has `specialPrice: null` with an error in the `errors` array, while all other products return their special prices successfully. This demonstrates partial results - the query doesn't fail completely.

## 4. External Service Integration (stockLevel Field)

### Implementation
```java
@Description("Current stock level from inventory service")
public int stockLevel(@Source Product product) {
    return inventoryService.getStockLevel(product.getId());
}
```

### Test Query
```bash
curl -X POST http://localhost:5060/graphql-catalog/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ products { id name stockLevel } }"}'
```

### Expected Response
```json
{
  "data": {
    "products": [
      {"id": 1, "name": "Laptop", "stockLevel": 0},
      {"id": 2, "name": "Mouse", "stockLevel": 50},
      {"id": 3, "name": "Keyboard", "stockLevel": 5},
      {"id": 4, "name": "Monitor", "stockLevel": 0},
      {"id": 5, "name": "Headphones", "stockLevel": 50}
    ]
  }
}
```

## Stock Level Reference

Based on InventoryService implementation:
- Product 1 (id%3 == 1): 0 units (OUT_OF_STOCK)
- Product 2 (id%3 == 2): 50 units (IN_STOCK)
- Product 3 (id%3 == 0): 5 units (LOW_STOCK)
- Product 4 (id%3 == 1): 0 units (OUT_OF_STOCK)
- Product 5 (id%3 == 2): 50 units (IN_STOCK)

## Configuration

Error visibility is controlled in `microprofile-config.properties`:

```properties
mp.graphql.defaultErrorMessage=An error occurred processing your request
mp.graphql.hideErrorMessage=java.lang.NullPointerException
mp.graphql.showErrorMessage=io.microprofile.tutorial.graphql.product.exception.ProductNotFoundException
```

## Error Response Structure

All GraphQL errors follow this structure:

```json
{
  "errors": [{
    "message": "Error description",
    "locations": [{"line": 1, "column": 12}],
    "path": ["fieldName"],
    "extensions": {
      "exception": "fully.qualified.ExceptionClass",
      "classification": "DataFetchingException",
      "code": "error-code"
    }
  }],
  "data": {
    "fieldName": null
  }
}
```

## Key Patterns Demonstrated

1. **RuntimeException**: Unchecked exceptions automatically converted to GraphQL errors
2. **GraphQLException**: Checked exception for controlled error responses with detailed messages
3. **Partial Results**: Some fields succeed, others fail - query returns both data and errors
4. **Field Resolvers**: Using `@Source` to add computed fields that may throw exceptions
5. **Method Signatures**: Checked exceptions must be declared with `throws` clause
6. **Error Paths**: GraphQL provides precise error location in response structure
