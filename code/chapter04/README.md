# Chapter 04 - MicroProfile OpenAPI v3.1 Demonstration

## 📚 Quick Navigation

👉 **Start Here**: [IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md) - Complete overview of both implementations

### 📖 Documentation

| Document | Description | Lines | Best For |
|----------|-------------|-------|----------|
| [IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md) | Complete overview, comparison, and getting started guide | 400+ | Everyone - start here! |
| [OPENAPI_V3.1_DEMO.md](OPENAPI_V3.1_DEMO.md) | Comprehensive feature demonstration with examples | 500+ | Deep dive into features |
| [OPENAPI_COMPARISON.md](OPENAPI_COMPARISON.md) | OpenAPI 3.0 vs 3.1 detailed comparison | 400+ | Migration and differences |
| [QUICK_REFERENCE.md](QUICK_REFERENCE.md) | Quick reference card with common patterns | 200+ | Quick lookup |

### 🚀 Implementations

| Service | Type | Complexity | Port | Best For |
|---------|------|------------|------|----------|
| [catalog/](catalog/) | Database-backed | Advanced | 9081 | Production use, comprehensive learning |
| [mp-ecomm-store/](mp-ecomm-store/) | In-memory | Simple | 5050 | Quick demos, getting started |

## 🎯 What's Demonstrated

This chapter demonstrates **MicroProfile OpenAPI 4.1** alignment with **OpenAPI v3.1** and **JSON Schema 2020-12** through two complete implementations.

### Core Features

✅ **Pattern Validation**: Regex-based string validation (e.g., SKU format)  
✅ **Exclusive Numeric Bounds**: `exclusiveMinimum` for precise constraints  
✅ **Format Specifications**: `int32`, `int64`, `double`, `date-time`  
✅ **Enumeration**: Type-safe enum values  
✅ **Nullable Properties**: JSON Schema-native nullable handling  
✅ **Array Constraints**: `minItems`, `maxItems`  
✅ **Numeric Precision**: `multipleOf` for decimal precision  
✅ **Default Values**: Schema-level defaults  
✅ **Rich Examples**: Comprehensive documentation  

## 🏃 Quick Start

### 1. Choose Your Implementation

**Option A: Simple In-Memory Store** (Recommended for beginners)
```bash
cd mp-ecomm-store
mvn clean package liberty:dev
```
Access at: http://localhost:5050/mp-ecomm-store/openapi/ui

**Option B: Database-Backed Catalog** (Recommended for production learning)
```bash
cd catalog
mvn clean package liberty:dev
```
Access at: http://localhost:9081/openapi/ui

### 2. Explore the OpenAPI Documentation

Once running, access:
- **Swagger UI**: http://localhost:PORT/openapi/ui (interactive)
- **OpenAPI JSON**: http://localhost:PORT/openapi (raw specification)

### 3. Test the Features

Both implementations include test scripts:
```bash
# In catalog/ or mp-ecomm-store/ directory
chmod +x test-openapi-features.sh
./test-openapi-features.sh
```

## 📊 Implementation Comparison

| Feature | Catalog | MP E-Commerce Store |
|---------|---------|---------------------|
| **Storage** | PostgreSQL | In-memory ArrayList |
| **Fields** | 14 (comprehensive) | 8 (essential) |
| **JPA** | ✅ Full implementation | ❌ Not applicable |
| **Timestamps** | ✅ Auto-managed | ❌ No timestamps |
| **Lifecycle** | ✅ @PrePersist, @PreUpdate | ❌ No lifecycle |
| **Complexity** | Advanced | Simple |
| **Setup Time** | 5 minutes (needs DB) | 2 minutes (no DB) |

### When to Use Each

**Use Catalog Service if you:**
- Want to see production-ready implementation
- Need database persistence examples
- Want to learn JPA/Hibernate integration
- Prefer comprehensive field validation

**Use MP E-Commerce Store if you:**
- Want quick demonstration
- Don't want to set up a database
- Prefer simpler, easier-to-understand code
- Focus on OpenAPI annotations only

**Use Both if you:**
- Want complete learning experience
- Want to compare approaches
- Need different complexity levels

## 🎓 Learning Path

### Beginner Path (Recommended)

1. **Read**: [IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md)
2. **Start**: Run [mp-ecomm-store/](mp-ecomm-store/)
3. **Explore**: Swagger UI at http://localhost:5050/mp-ecomm-store/openapi/ui
4. **Test**: Run `mp-ecomm-store/test-openapi-features.sh`
5. **Study**: Review [mp-ecomm-store/README_OPENAPI.md](mp-ecomm-store/README_OPENAPI.md)

### Intermediate Path

1. **Move to**: [catalog/](catalog/) service
2. **Study**: [catalog/README_OPENAPI_V3.1.md](catalog/README_OPENAPI_V3.1.md)
3. **Deep Dive**: [OPENAPI_V3.1_DEMO.md](OPENAPI_V3.1_DEMO.md)
4. **Compare**: Side-by-side comparison of both implementations
5. **Test**: Advanced features with test scripts

### Advanced Path

1. **Read**: [OPENAPI_COMPARISON.md](OPENAPI_COMPARISON.md)
2. **Analyze**: Differences between OpenAPI 3.0 and 3.1
3. **Customize**: Extend implementations with new features
4. **Integrate**: Add to your own projects
5. **Reference**: Use [QUICK_REFERENCE.md](QUICK_REFERENCE.md) for patterns

## 📋 REST API Reference

Both implementations provide identical REST APIs:

### Endpoints

| Method | Endpoint | Description | Features Demonstrated |
|--------|----------|-------------|----------------------|
| GET | `/products` | List all products | Array constraints (minItems, maxItems) |
| GET | `/products/{id}` | Get by ID | Path parameter validation (format: int64) |
| POST | `/products` | Create product | Full schema validation, all constraints |
| GET | `/products/search` | Search products | Query parameters, pagination, filtering |

### Example Requests

```bash
# Get all products
curl http://localhost:5050/mp-ecomm-store/api/products

# Get product by ID
curl http://localhost:5050/mp-ecomm-store/api/products/1

# Create product (demonstrates validation)
curl -X POST http://localhost:5050/mp-ecomm-store/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Mechanical Keyboard",
    "price": 129.99,
    "sku": "ELC-KB001-RGB",
    "category": "ELECTRONICS"
  }'

# Search with filters
curl "http://localhost:5050/mp-ecomm-store/api/products/search?category=ELECTRONICS&minPrice=20&maxPrice=100"
```

## 🔍 Validation Examples

### SKU Pattern Validation
```
Pattern: ^[A-Z]{3}-[A-Z0-9]+-[A-Z0-9]+$

✅ Valid:
  - ELC-MS001-BLK
  - CLO-TSH123-RED
  - BKS-JAVA-2024

❌ Invalid:
  - elc-ms001-blk      (lowercase not allowed)
  - E-MS001-BLK        (needs 3 letters before first dash)
  - ELC_MS001_BLK      (must use dashes, not underscores)
```

### Price Validation
```
Constraints: minimum=0.01, exclusiveMinimum=true, multipleOf=0.01

✅ Valid:
  - 0.02    (> 0.01 and multiple of 0.01)
  - 29.99   (valid)
  - 100.00  (valid)

❌ Invalid:
  - 0.00    (not > 0.01)
  - 0.01    (not > 0.01, must be exclusive)
  - 29.999  (not multiple of 0.01)
  - -10.00  (negative)
```

### Category Enumeration
```
Allowed Values: ELECTRONICS, CLOTHING, BOOKS, HOME, SPORTS

✅ Valid:
  - ELECTRONICS
  - CLOTHING
  - BOOKS

❌ Invalid:
  - electronics  (case-sensitive, must be uppercase)
  - TOYS         (not in enumeration)
  - ""           (empty string)
```

## 🎁 What You Get

### Two Complete Services
- ✅ Full source code
- ✅ Comprehensive OpenAPI annotations
- ✅ Liberty server configuration
- ✅ Maven build files
- ✅ Test scripts
- ✅ Detailed README files

### Rich Documentation
- ✅ 4 comprehensive guides (2000+ lines total)
- ✅ Code examples and snippets
- ✅ Validation rules and patterns
- ✅ Testing procedures
- ✅ Troubleshooting tips
- ✅ Learning paths

### Testing Tools
- ✅ Automated test scripts
- ✅ Swagger UI integration
- ✅ Example curl commands
- ✅ Sample data

## 🔗 External Resources

- [MicroProfile OpenAPI Specification](https://github.com/eclipse/microprofile-open-api)
- [OpenAPI v3.1.0 Specification](https://spec.openapis.org/oas/v3.1.0)
- [JSON Schema 2020-12](https://json-schema.org/draft/2020-12/json-schema-core.html)
- [MicroProfile 7.1](https://microprofile.io/)
- [Open Liberty](https://openliberty.io/)

## 🐛 Troubleshooting

### Port Already in Use
```bash
# Check what's using the port
lsof -i :5050  # or :9081

# Kill the process
kill -9 <PID>

# Or change port in server.xml
<httpEndpoint httpPort="9082" httpsPort="9443"/>
```

### Build Failures
```bash
# Clean everything
mvn clean

# Rebuild
mvn package

# Check Java version (requires Java 21)
java -version
```

### Database Issues (Catalog only)
```bash
# Verify PostgreSQL is running
docker ps | grep postgres

# Check connection in microprofile-config.properties
jakarta.persistence.jdbc.url=jdbc:postgresql://localhost:5432/productdb
```

### OpenAPI Not Loading
```bash
# Verify feature in server.xml
<feature>mpOpenAPI-4.0</feature>

# Check server logs
tail -f target/liberty/wlp/usr/servers/defaultServer/logs/messages.log
```

## 💡 Key Takeaways

1. **OpenAPI v3.1 aligns with JSON Schema 2020-12** - Single source of truth
2. **Better validation** - More precise constraints (exclusiveMinimum, pattern, multipleOf)
3. **Improved tooling** - Leverage entire JSON Schema ecosystem
4. **Production ready** - Both implementations suitable for real applications
5. **Well documented** - Comprehensive guides and examples

## 🎯 Next Steps

1. **Run one or both implementations**
2. **Explore Swagger UI** - Try the interactive documentation
3. **Run test scripts** - See validation in action
4. **Read the guides** - Deep dive into features
5. **Customize** - Add your own endpoints and validations
6. **Integrate** - Use in your own MicroProfile projects

---

**Need Help?** Start with [IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md) for a complete overview!

**Ready to Go?** Choose an implementation above and run `mvn liberty:dev`!
