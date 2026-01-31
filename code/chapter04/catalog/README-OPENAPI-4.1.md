# Chapter 04 - MicroProfile OpenAPI 4.1

This chapter demonstrates the features of MicroProfile OpenAPI 4.1, including:

## New Features Demonstrated

### 1. OpenAPI v3.1 Compatibility
The generated OpenAPI documents use the `openapi: 3.1.0` version, bringing better JSON Schema support and improved nullable handling.

### 2. Java Records Support
See `ProductRecord.java` - demonstrates how Java Records are automatically documented with proper schema generation.

Example endpoint: `GET /api/products/record/{id}`

### 3. Optional<T> Fields
See `ProductWithOptional.java` - shows how Optional fields are properly marked as nullable in the OpenAPI schema.

Example endpoint: `GET /api/products/optional/{id}`

### 4. @Target Annotation Enhancement
See `ConditionalProduct.java` - demonstrates the `@DependentRequired` annotation with proper @Target metadata.

### 5. JSON Schema Dialect Support
See `CustomModelReader.java` - shows how to specify the JSON Schema dialect using `jsonSchemaDialect` property.

To enable this reader, add to `META-INF/microprofile-config.properties`:
```
mp.openapi.model.reader=io.microprofile.tutorial.store.product.CustomModelReader
```

### 6. Extensible Interface Methods
See `ExtensionFilter.java` - demonstrates the new `getExtension()` and `hasExtension()` methods.

To enable this filter, add to `META-INF/microprofile-config.properties`:
```
mp.openapi.filter=io.microprofile.tutorial.store.product.ExtensionFilter
```

### 7. Async Operations with Callbacks
See the `processProductAsync()` method in `ProductResource.java` - demonstrates how to document asynchronous operations with callback URLs.

Example endpoint: `POST /api/products/async-process`

### 8. Security Schemes
See `SecuredProductApplication.java` - comprehensive example of documenting multiple security schemes including:
- API Key authentication
- Bearer Token (JWT)
- OAuth2 with authorization code flow
- Basic HTTP authentication

## Building and Running

This project is configured to use Java 21 as specified in the pom.xml. However, the MicroProfile OpenAPI 4.1 features demonstrated here work with Java 17+ (which includes support for Java Records).

If you have Java 21 installed:

```bash
java -version  # Should show version 21
mvn clean package
mvn liberty:dev
```

If you only have Java 17, you can modify the pom.xml to use Java 17 instead (Java Records are supported since Java 16 and finalized in Java 17).

## Viewing the OpenAPI Documentation

Once the application is running, you can view the OpenAPI document at:

- YAML format: http://localhost:5050/openapi
- Swagger UI: http://localhost:5050/openapi/ui

## Key OpenAPI 4.1 Improvements

1. **Better nullable handling**: Uses JSON Schema's type arrays instead of deprecated `nullable` keyword
2. **JSON Schema 2020-12 alignment**: More robust schema validation
3. **Enhanced extension support**: New methods for working with vendor extensions
4. **Improved documentation for async APIs**: Better callback support
5. **Comprehensive security documentation**: Multiple authentication mechanisms

## References

- [MicroProfile OpenAPI 4.1 Specification](https://download.eclipse.org/microprofile/microprofile-open-api-4.1/microprofile-openapi-spec-4.1.html)
- [OpenAPI v3.1 Specification](https://spec.openapis.org/oas/v3.1.0.html)
- [JSON Schema 2020-12](https://json-schema.org/draft/2020-12/json-schema-core.html)
