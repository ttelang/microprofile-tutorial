# Chapter 04: MicroProfile OpenAPI

This chapter demonstrates **MicroProfile OpenAPI 4.1** features for automatically generating API documentation from Java annotations.

## Overview

This example shows how to use MicroProfile OpenAPI to:
- Document RESTful endpoints with annotations
- Generate OpenAPI 3.1-compliant specifications
- Visualize APIs using Swagger UI
- Support modern Java features (Records)
- Document security schemes
- Handle asynchronous operations with callbacks

## MicroProfile OpenAPI 4.1 Features Demonstrated

### 1. Core Annotations

**Basic Endpoint Documentation (`ProductResource.java`)**:
- `@Operation` - Describe what endpoints do
- `@APIResponse` / `@APIResponses` - Document response codes and content
- `@Tag` - Group related endpoints
- `@Content` - Specify media types and schemas
- `@Schema` - Define data models

### 2. Parameter Documentation

**Query Parameter Documentation (`searchProducts()` endpoint)**:
- `@Parameter` - Document query parameters with descriptions and examples
- Demonstrates filtering by name, description, price range

### 3. Request Body Documentation

**Request Payload Documentation (`createProduct()` endpoint)**:
- `@RequestBody` - Full documentation of request payloads
- Content type specification
- Required vs. optional request bodies

### 4. Security Schemes

**Security Documentation (`SecuredProductApplication.java`)**:
Defines four security schemes:
- **API Key** - Header-based authentication
- **Bearer Token (JWT)** - HTTP Bearer authentication
- **OAuth2** - Authorization Code flow with scopes
- **Basic Auth** - HTTP Basic authentication

**Applied Security (`updateProduct()`, `deleteProduct()` endpoints)**:
- `@SecurityRequirement` - Apply security to specific operations
- Swagger UI displays lock icons and allows credential entry

### 5. OpenAPI 3.1 Features

**Java Records Support (`ProductRecord.java`)**:
- Automatic schema generation from record components
- Combined with `@Schema` and validation annotations
- Modern, immutable data carriers

**Nullable Handling**:
- `@Schema(nullable = true)` generates proper type arrays
- Aligned with JSON Schema 2020-12

### 6. Bean Validation Integration

**Automatic Constraint Documentation (`Product.java`)**:
- `@NotNull`, `@NotBlank` - Required fields
- `@Size(min, max)` - String length constraints
- `@DecimalMin` - Minimum value constraints
- Generated OpenAPI includes: `required`, `minLength`, `maxLength`, `minimum`

### 7. Advanced Features

**Asynchronous Operations (`processProductAsync()` endpoint)**:
- `@Callback` - Document webhook callbacks
- `@CallbackOperation` - Define callback HTTP requests
- Demonstrates async processing patterns

**Conditional Validation (`ConditionalProduct.java`, `/conditional` endpoint)**:
- `@DependentRequired` - JSON Schema dependent requirements
- If `discount` is provided, `discountReason` is required

**Optional Fields (`ProductWithOptional.java`, `/optional/{id}` endpoint)**:
- Demonstrates `Optional<T>` field handling
- Proper nullable schema generation

### 8. Extension and Customization

**Custom Filter (`ExtensionFilter.java`)**:
- Implements `OASFilter` interface
- Processes vendor extensions (`x-custom-timeout`, `x-rate-limit`)
- Uses new `hasExtension()` and `getExtension()` methods
- **Enabled in `microprofile-config.properties`**

**Custom Model Reader (`CustomModelReader.java`)**:
- Implements `OASModelReader` interface
- Sets JSON Schema dialect for OpenAPI 3.1
- Provides custom API metadata
- **Enabled in `microprofile-config.properties`**

## Configuration

**`microprofile-config.properties`**:
```properties
# Enable OpenAPI scanning
mp.openapi.scan=true

# Enable custom filter and model reader
mp.openapi.filter=io.microprofile.tutorial.store.product.ExtensionFilter
mp.openapi.model.reader=io.microprofile.tutorial.store.product.CustomModelReader

# Examples (commented):
# mp.openapi.scan.exclude.packages=io.myapp.internal
# mp.openapi.servers=https://api.example.com/v1
```

## Project Structure

```
catalog/
├── src/main/java/
│   └── io/microprofile/tutorial/store/product/
│       ├── entity/
│       │   ├── Product.java                  # Main product POJO with Bean Validation
│       │   ├── ProductRecord.java            # Java Record demonstration
│       │   ├── ProductWithOptional.java      # Optional<T> fields
│       │   ├── ConditionalProduct.java       # @DependentRequired demonstration
│       │   ├── AsyncRequest.java             # Async callback request
│       │   └── ProcessResult.java            # Async callback response
│       ├── resource/
│       │   └── ProductResource.java          # REST endpoints with OpenAPI annotations
│       ├── service/
│       │   └── ProductService.java           # Business logic layer
│       ├── repository/
│       │   └── ProductRepository.java        # Simple in-memory storage
│       ├── ProductRestApplication.java       # JAX-RS application
│       ├── SecuredProductApplication.java    # Security schemes definition
│       ├── ExtensionFilter.java              # Custom OASFilter
│       └── CustomModelReader.java            # Custom OASModelReader
└── pom.xml
```

## Building and Running

### Prerequisites
- JDK 17+
- Maven 3.8+

### Build
```bash
mvn clean package
```

### Run in Development Mode
```bash
mvn liberty:dev
```

The server starts on **port 5050** with application context `/catalog`.

## Accessing the API Documentation

### OpenAPI Specification
- **YAML format**: http://localhost:5050/openapi
- **JSON format**: http://localhost:5050/openapi?format=JSON

### Swagger UI
- **Interactive documentation**: http://localhost:5050/openapi/ui

The Swagger UI provides:
- Interactive API explorer
- Try-it-out functionality for testing endpoints
- Security scheme credential entry
- Model schemas with validation rules
- Response code documentation

## Testing the API

### Create a Product
```bash
curl -X POST http://localhost:5050/catalog/api/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Laptop","description":"Gaming laptop","price":1499.99}'
```

### List All Products
```bash
curl http://localhost:5050/catalog/api/products
```

### Get Product by ID
```bash
curl http://localhost:5050/catalog/api/products/1
```

### Search Products
```bash
curl "http://localhost:5050/catalog/api/products/search?name=laptop&minPrice=1000"
```

### Test Conditional Validation
```bash
# This will fail - discount without discountReason
curl -X POST http://localhost:5050/catalog/api/products/conditional \
  -H "Content-Type: application/json" \
  -d '{"id":1,"name":"Laptop","price":999.99,"discount":100.0}'

# This will succeed - discount with discountReason
curl -X POST http://localhost:5050/catalog/api/products/conditional \
  -H "Content-Type: application/json" \
  -d '{"id":1,"name":"Laptop","price":999.99,"discount":100.0,"discountReason":"Black Friday Sale"}'
```

## Key Learning Points

### Annotation-Driven Documentation
OpenAPI documentation lives alongside your code. When you update an endpoint, you update its documentation at the same time.

### Convenience Over Verbosity
MicroProfile OpenAPI provides convenience annotations (`@RequestBodySchema`, `@APIResponseSchema`) and simplifies common patterns.

### Integration with Standards
- **Bean Validation**: Constraints automatically appear in OpenAPI schemas
- **JSON Schema**: OpenAPI 3.1 aligns with JSON Schema 2020-12
- **Security Standards**: Support for OAuth2, JWT, API keys, and more

### Visualization and Testing
Swagger UI turns your OpenAPI specification into an interactive web interface, making it easy for developers to explore and test your API.

## OpenAPI 3.1 vs 3.0 Improvements

MicroProfile OpenAPI 4.x supports OpenAPI 3.1, which provides:
- Better JSON Schema alignment
- Improved nullable handling with type arrays
- Webhooks and callbacks support
- JSON Schema vocabulary extensibility

## Further Reading

- [MicroProfile OpenAPI 4.1 Specification](https://download.eclipse.org/microprofile/microprofile-open-api-4.1/microprofile-openapi-spec-4.1.html)
- [OpenAPI 3.1 Specification](https://spec.openapis.org/oas/v3.1.0)
- [JSON Schema 2020-12](https://json-schema.org/draft/2020-12/json-schema-core.html)
- [MicroProfile Documentation](https://microprofile.io)

## Summary

This chapter demonstrates how MicroProfile OpenAPI enables:
- ✅ **Automatic documentation generation** from Java annotations
- ✅ **Standards-compliant** OpenAPI 3.1 specifications
- ✅ **Modern Java support** (Records, Optional)
- ✅ **Security documentation** (OAuth2, JWT, API keys)
- ✅ **Callback patterns** for async operations
- ✅ **Bean Validation integration** for constraint documentation
- ✅ **Extension points** for customization (filters, model readers)

The result is API documentation that stays synchronized with your code, supports rich tooling ecosystems, and provides an excellent developer experience.
