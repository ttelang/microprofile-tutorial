# Polymorphic GraphQL Queries - Ready to Test

## 🚀 Quick Start

1. Start the application:
   ```bash
   cd /workspaces/microprofile-tutorial/code/chapter12/catalog
   mvn liberty:dev
   ```

2. Open GraphiQL in your browser:
   ```
   http://localhost:9080/graphql-ui
   ```

3. Copy and paste any of the queries below!

---

## 📋 Query 1: Recent Items Feed (Mixed Content)

**Use Case**: Activity feed showing both products and reviews together

```graphql
query RecentActivityFeed {
  recentItems(limit: 10) {
    id
    __typename
    
    ... on Product {
      name
      price
      category
      stockQuantity
      availabilityStatus
    }
    
    ... on Review {
      reviewerName
      rating
      comment
      createdAt
      productId
    }
  }
}
```

**What it demonstrates**: 
- Returns a mixed list of Products and Reviews
- `__typename` tells you which type each item is
- Inline fragments (`... on Product`) access type-specific fields

---

## 🔍 Query 2: Unified Entity Lookup

**Use Case**: Find any entity by ID without knowing its type

```graphql
query FindAnyEntity {
  findById(id: 1) {
    id
    __typename
    
    ... on Product {
      name
      price
      description
      category
    }
    
    ... on Review {
      reviewerName
      rating
      comment
    }
  }
}
```

**Try different IDs**: Change `id: 1` to `id: 2`, `id: 3`, etc.

---

## 🔄 Query 3: Multiple Lookups at Once

**Use Case**: Fetch multiple entities of different types in one request

```graphql
query MultipleLookups {
  product1: findById(id: 1) {
    id
    __typename
    ... on Product {
      name
      price
    }
  }
  
  product2: findById(id: 2) {
    id
    __typename
    ... on Product {
      name
      price
    }
  }
  
  review1: findById(id: 101) {
    id
    __typename
    ... on Review {
      reviewerName
      rating
    }
  }
}
```

**What it demonstrates**: 
- Query aliases (product1, product2, review1)
- Single request for multiple entities
- Polymorphism handles different types transparently

---

## 📊 Query 4: Rich Recent Items with Related Data

**Use Case**: Activity feed with full details including computed fields

```graphql
query RichActivityFeed {
  recentItems(limit: 5) {
    id
    __typename
    
    ... on Product {
      name
      price
      priceWithTax
      category
      stockQuantity
      availabilityStatus
      
      # Field resolvers
      priceCategory
      averageRating
    }
    
    ... on Review {
      reviewerName
      rating
      comment
      createdAt
      productId
    }
  }
}
```

**What it demonstrates**:
- Computed fields (priceWithTax, availabilityStatus)
- Field resolvers (priceCategory, averageRating)
- Rich polymorphic data

---

## 🎯 Query 5: Conditional Recent Items

**Use Case**: Smart recent items with different detail levels

```graphql
query ConditionalRecentItems {
  recent: recentItems(limit: 10) {
    # Common fields for all types
    id
    __typename
    
    # Products: full details
    ... on Product {
      name
      price
      category
      reviews {
        rating
        reviewerName
      }
    }
    
    # Reviews: just core info
    ... on Review {
      rating
      comment
      reviewerName
    }
  }
}
```

**What it demonstrates**:
- Different detail levels for different types
- Nested queries (reviews inside products)

---

## 🔎 Query 6: Type-Filtered Activity Feed

**Use Case**: Get recent items but handle types differently

```graphql
query TypeFilteredFeed {
  allRecent: recentItems(limit: 20) {
    id
    __typename
    
    ... on Product {
      name
      category
    }
    
    ... on Review {
      reviewerName
      rating
    }
  }
  
  # Also get specific type data
  products {
    id
    name
    price
  }
  
  # For comparison
  product(id: 1) {
    reviews {
      id
      rating
      comment
    }
  }
}
```

**What it demonstrates**:
- Combining polymorphic and type-specific queries
- Flexibility of GraphQL query structure

---

## 🧪 Query 7: Testing Type Discrimination

**Use Case**: Verify __typename works correctly

```graphql
query TypeDiscrimination {
  items1to5: recentItems(limit: 5) {
    id
    __typename
  }
  
  lookup1: findById(id: 1) {
    __typename
  }
  
  lookup2: findById(id: 2) {
    __typename
  }
}
```

**Expected output**: 
- `__typename` will be either "Product" or "Review"
- Use this to understand which IDs map to which types

---

## 💡 Query 8: Practical Search Results

**Use Case**: Unified search across products and reviews (simulated)

```graphql
query UnifiedSearchResults {
  # Get products matching criteria
  products: searchProducts(searchTerm: "laptop", category: "Electronics") {
    id
    name
    price
  }
  
  # Get recent items (simulating mixed search results)
  mixedResults: recentItems(limit: 10) {
    id
    __typename
    
    ... on Product {
      name
      price
      description
      category
    }
    
    ... on Review {
      reviewerName
      rating
      comment
    }
  }
}
```

---

## 🎨 Query 9: Fragment Reuse with Polymorphic Types

**Use Case**: Reusable fragments for common patterns

```graphql
fragment ProductBasicInfo on Product {
  id
  name
  price
  category
}

fragment ReviewBasicInfo on Review {
  id
  reviewerName
  rating
  comment
}

query ReusableFragments {
  recent: recentItems(limit: 10) {
    id
    __typename
    ...ProductBasicInfo
    ...ReviewBasicInfo
  }
  
  specific: findById(id: 1) {
    ...ProductBasicInfo
    ...ReviewBasicInfo
  }
}
```

**What it demonstrates**:
- Fragment reuse with polymorphic queries
- Cleaner, more maintainable queries

---

## 🏆 Query 10: Complete Real-World Example

**Use Case**: Dashboard with mixed content types

```graphql
query DashboardData {
  # Recent activity feed
  recentActivity: recentItems(limit: 5) {
    id
    __typename
    
    ... on Product {
      name
      price
      priceCategory
      availabilityStatus
    }
    
    ... on Review {
      reviewerName
      rating
      comment
      createdAt
    }
  }
  
  # Summary statistics
  totalProducts: productCount
  avgPrice: averagePrice
  
  # Featured product with reviews
  featured: product(id: 1) {
    name
    price
    description
    reviews {
      reviewerName
      rating
      comment
    }
    averageRating
  }
}
```

**What it demonstrates**:
- Real-world dashboard use case
- Mixing polymorphic and specific queries
- Complex nested data retrieval

---

## 📝 Testing Tips

### 1. **Check Available Data First**

```graphql
query CheckData {
  products {
    id
    name
  }
  
  product(id: 1) {
    reviews {
      id
      rating
    }
  }
}
```

### 2. **Introspection for Schema**

```graphql
query IntrospectIdentifiable {
  __type(name: "Identifiable") {
    name
    kind
    description
    possibleTypes {
      name
      description
    }
  }
}
```

### 3. **See All Available Queries**

In GraphiQL, use the "Docs" panel on the right to explore:
- `recentItems` query
- `findById` query
- The `Identifiable` interface
- `Product` and `Review` types

---

## 🎯 Expected Benefits You'll See

✅ **Single Request**: Get Products and Reviews together  
✅ **Type Safety**: Schema validates all queries  
✅ **Flexibility**: Choose which fields to retrieve for each type  
✅ **Performance**: Fewer round trips to server  
✅ **Developer Experience**: Auto-completion in GraphiQL  

---

## 🐛 Troubleshooting

**Issue**: Queries return empty arrays  
**Solution**: Make sure data exists. Try creating some products first:

```graphql
mutation CreateTestProduct {
  createProduct(input: {
    name: "Test Laptop"
    description: "A test product"
    price: 999.99
    category: "Electronics"
    stockQuantity: 10
  }) {
    id
    name
    price
  }
}
```

**Issue**: `recentItems` or `findById` not found  
**Solution**: Verify the ProductGraphQLApi.java includes these queries (it should per our implementation)

---

## 🚀 Next Steps

1. Try each query in sequence
2. Modify them to explore different scenarios
3. Create your own queries combining different features
4. Observe how GraphQL handles type discrimination automatically

Happy testing! 🎉
