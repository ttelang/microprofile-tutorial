# Sample GraphQL Queries for MicroProfile GraphQL Catalog

This file contains example GraphQL queries and mutations that you can run against the GraphQL Catalog API.

## Queries

### Get All Products

```graphql
query {
  products {
    id
    name
    price
    category
    stockQuantity
  }
}
```

### Get Product by ID

```graphql
query {
  product(id: 1) {
    id
    name
    description
    price
    category
    stockQuantity
  }
}
```

### Get Product with Computed Fields

```graphql
query {
  product(id: 1) {
    id
    name
    price
    priceWithTax
    priceCategory
    availabilityStatus
  }
}
```

### Get Product with Reviews

```graphql
query {
  product(id: 1) {
    id
    name
    price
    reviews {
      id
      reviewerName
      rating
      comment
      createdAt
    }
    averageRating
  }
}
```

### Get Multiple Products with Reviews (Batch Loading)

```graphql
query {
  products {
    id
    name
    price
    reviews {
      reviewerName
      rating
      comment
    }
  }
}
```

### Get Product with Top Reviews

```graphql
query {
  product(id: 1) {
    id
    name
    topReviews(limit: 3) {
      reviewerName
      rating
      comment
    }
  }
}
```

### Search Products

```graphql
query {
  searchProducts(searchTerm: "laptop", category: "Electronics") {
    id
    name
    price
    stockQuantity
  }
}
```

### Search by Term Only

```graphql
query {
  searchProducts(searchTerm: "mouse") {
    id
    name
    price
  }
}
```

### Get Catalog Statistics

```graphql
query {
  productCount
  averagePrice
  categories
}
```

### Complex Query with All Features

```graphql
query {
  products {
    id
    name
    description
    price
    priceWithTax
    priceCategory
    availabilityStatus
    category
    stockQuantity
    reviews {
      reviewerName
      rating
      comment
    }
    averageRating
    topReviews(limit: 2) {
      rating
      comment
    }
  }
  productCount
  averagePrice
  categories
}
```

## Mutations

### Create Product

```graphql
mutation {
  createProduct(input: {
    name: "Wireless Charger"
    description: "Fast wireless charging pad"
    price: 39.99
    category: "Electronics"
    stockQuantity: 100
  }) {
    id
    name
    price
    priceWithTax
    priceCategory
  }
}
```

### Update Product

```graphql
mutation {
  updateProduct(
    id: 1
    input: {
      name: "Gaming Laptop Pro"
      description: "Professional gaming laptop with RTX 4080"
      price: 1499.99
      category: "Electronics"
      stockQuantity: 25
    }
  ) {
    id
    name
    price
    stockQuantity
  }
}
```

### Delete Product

```graphql
mutation {
  deleteProduct(id: 6)
}
```

### Create and Query in Same Request

```graphql
mutation {
  createProduct(input: {
    name: "USB Cable"
    description: "USB-C to USB-C cable"
    price: 12.99
    category: "Accessories"
    stockQuantity: 500
  }) {
    id
    name
    price
    priceCategory
    availabilityStatus
  }
}

query {
  productCount
}
```

## Variables

You can use variables to make queries more dynamic:

### Query with Variables

```graphql
query GetProduct($productId: ID!) {
  product(id: $productId) {
    id
    name
    price
    reviews {
      rating
      comment
    }
  }
}
```

Variables:
```json
{
  "productId": "1"
}
```

### Mutation with Variables

```graphql
mutation CreateProduct($input: ProductInput!) {
  createProduct(input: $input) {
    id
    name
    price
  }
}
```

Variables:
```json
{
  "input": {
    "name": "Smart Watch",
    "description": "Fitness tracking smart watch",
    "price": 199.99,
    "category": "Wearables",
    "stockQuantity": 75
  }
}
```

## Aliases

Use aliases to query the same field with different arguments:

```graphql
query {
  budget: searchProducts(searchTerm: "mouse") {
    id
    name
    price
  }
  premium: searchProducts(searchTerm: "laptop") {
    id
    name
    price
  }
}
```

## Fragments

Use fragments to reuse common field selections:

```graphql
fragment ProductBasic on Product {
  id
  name
  price
  category
}

fragment ProductDetailed on Product {
  ...ProductBasic
  description
  stockQuantity
  priceWithTax
  availabilityStatus
}

query {
  products {
    ...ProductBasic
  }
  product(id: 1) {
    ...ProductDetailed
    reviews {
      rating
      comment
    }
  }
}
```

## Error Handling Examples

### Query Non-Existent Product

```graphql
query {
  product(id: 9999) {
    id
    name
  }
}
```

Expected response with error:
```json
{
  "data": {
    "product": null
  },
  "errors": [
    {
      "message": "Product not found",
      "path": ["product"],
      "extensions": {
        "productId": 9999,
        "errorCode": "PRODUCT_NOT_FOUND",
        "classification": "DataFetchingException"
      }
    }
  ]
}
```

### Update Non-Existent Product

```graphql
mutation {
  updateProduct(
    id: 9999
    input: {
      name: "Test"
      price: 1.0
    }
  ) {
    id
  }
}
```

## Using with curl

### Simple Query

```bash
curl -X POST http://localhost:5060/graphql-catalog/graphql \
  -H "Content-Type: application/json" \
  -d '{"query": "{ products { id name price } }"}'
```

### Query with Variables

```bash
curl -X POST http://localhost:5060/graphql-catalog/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "query GetProduct($id: ID!) { product(id: $id) { id name price } }",
    "variables": {"id": "1"}
  }'
```

### Mutation

```bash
curl -X POST http://localhost:5060/graphql-catalog/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "mutation { createProduct(input: { name: \"Test\", price: 9.99, category: \"Test\" }) { id name } }"
  }'
```

## Schema Introspection

### Get Schema Types

```graphql
query {
  __schema {
    types {
      name
      kind
    }
  }
}
```

### Get Specific Type Information

```graphql
query {
  __type(name: "Product") {
    name
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

### Get Query Operations

```graphql
query {
  __schema {
    queryType {
      fields {
        name
        description
        args {
          name
          type {
            name
          }
        }
      }
    }
  }
}
```
