# Quick Start Guide for Tutorial Fixes

**Purpose:** Fast-track guide to implement critical fixes identified in the sequential alignment review.

## 🚨 Start Here: Phase 1 Critical Fixes (14 hours)

### Fix 1: Chapter 2 Build Failure (30 minutes)

**Problem:** Code requires Java 21, but docs teach Java 17. Build fails.

**File:** `/code/chapter02/mp-ecomm-store/pom.xml`

**Change:**
```xml
<!-- Line 19-20: Change from -->
<release>21</release>

<!-- To -->
<release>17</release>
```

**Test:**
```bash
cd code/chapter02/mp-ecomm-store
mvn clean compile
# Should succeed without errors
```

---

### Fix 2: Chapter 2 Port Standardization (15 minutes)

**Problem:** Docs say port 9080, code uses 5050. Confusing for learners.

**File:** `/code/chapter02/mp-ecomm-store/pom.xml`

**Change:**
```xml
<!-- Lines 23-24: Change from -->
<liberty.var.default.http.port>5050</liberty.var.default.http.port>
<liberty.var.default.https.port>5051</liberty.var.default.https.port>

<!-- To -->
<liberty.var.default.http.port>9080</liberty.var.default.http.port>
<liberty.var.default.https.port>9443</liberty.var.default.https.port>
```

**Also update:**
- `/code/chapter02/mp-ecomm-store/README.adoc` - Change all URLs from :5050 to :9080

---

### Fix 3: Version Documentation Updates (4 hours)

**Problem:** Docs say MicroProfile 6.1, code uses 7.1, server.xml uses 7.0.

**Files to update:**

1. **`modules/ROOT/pages/chapter02/chapter02-00.adoc`**
   - Line 224: Change "latest MicroProfile released version was 6.1" → "7.1"

2. **`modules/ROOT/pages/chapter02/chapter02-01.adoc`**
   - Update example pom.xml snippets to show version 7.1

3. **`modules/ROOT/pages/chapter02/chapter02-02.adoc`**
   - Line 13: Change "MicroProfile 6.1" → "MicroProfile 7.1"
   - Line 20: Change version in example from 6.1 → 7.1

4. **`code/chapter02/mp-ecomm-store/README.adoc`**
   - Change "MicroProfile 6.1 features" → "MicroProfile 7.1 features"

**Search & Replace Command:**
```bash
# Find all instances
grep -r "MicroProfile 6.1" modules/ROOT/pages/chapter02/
grep -r "microprofile>6.1" modules/ROOT/pages/chapter02/

# After manual review, replace
find modules/ROOT/pages/chapter02/ -type f -name "*.adoc" -exec sed -i 's/MicroProfile 6\.1/MicroProfile 7.1/g' {} +
find modules/ROOT/pages/chapter02/ -type f -name "*.adoc" -exec sed -i 's/microprofile>6\.1/microprofile>7.1/g' {} +
```

---

### Fix 4: Chapter 3 Documentation Rewrite (8 hours)

**Problem:** Docs show `ProductRepository` class that doesn't exist. Code uses `ProductService`.

**Files to update:**

1. **`modules/ROOT/pages/chapter03/chapter03.adoc`**

**Current (wrong):**
```asciidoc
Create ProductRepository class:
[source,java]
----
public class ProductRepository {
    // This class doesn't exist in code!
}
----
```

**Fixed (right):**
```asciidoc
Create ProductService class:
[source,java]
----
@ApplicationScoped
public class ProductService {
    private final List<Product> products = new ArrayList<>();
    
    @PostConstruct
    public void init() {
        products.add(new Product(1L, "iPhone", "Apple iPhone 15", 999.99));
        products.add(new Product(2L, "MacBook", "Apple MacBook Pro", 2499.99));
    }
    
    public List<Product> getAllProducts() {
        return new ArrayList<>(products);
    }
}
----
```

2. **Add Interceptor Documentation**

**Add new section:**
```asciidoc
=== Adding Logging with Interceptors

Jakarta EE Core Profile includes CDI interceptors for cross-cutting concerns.

Create a logging interceptor:

[source,java]
----
@Interceptor
@Logged
@Priority(Interceptor.Priority.APPLICATION)
public class LoggingInterceptor {
    @AroundInvoke
    public Object log(InvocationContext context) throws Exception {
        System.out.println("Calling: " + context.getMethod().getName());
        return context.proceed();
    }
}
----

Define the annotation:

[source,java]
----
@InterceptorBinding
@Target({TYPE, METHOD})
@Retention(RUNTIME)
public @interface Logged {
}
----

Enable in beans.xml:

[source,xml]
----
<beans xmlns="https://jakarta.ee/xml/ns/jakartaee"
       version="4.0"
       bean-discovery-mode="all">
    <interceptors>
        <class>io.microprofile.tutorial.store.product.interceptor.LoggingInterceptor</class>
    </interceptors>
</beans>
----

Use the interceptor:

[source,java]
----
@ApplicationScoped
@Logged  // <-- Logs all method calls
public class ProductService {
    // ... existing code
}
----
```

---

## 🔧 Quick Verification Checklist

After Phase 1 fixes, verify:

- [ ] Chapter 2 builds successfully: `mvn clean package`
- [ ] Chapter 2 runs on port 9080: `http://localhost:9080/mp-ecomm-store/api/products`
- [ ] All chapter docs reference MicroProfile 7.1 (not 6.1)
- [ ] Chapter 3 docs describe classes that exist in code
- [ ] Interceptors documented in Chapter 3

---

## 📝 Testing Your Fixes

### Test Chapter 2
```bash
cd code/chapter02/mp-ecomm-store
mvn clean package
mvn liberty:run

# In another terminal:
curl http://localhost:9080/mp-ecomm-store/api/products
# Should return JSON with products
```

### Test Chapter 3
```bash
cd code/chapter03/mp-ecomm-store
mvn clean package
mvn liberty:run

# In another terminal:
curl http://localhost:9080/mp-ecomm-store/api/products
# Should return JSON with products
# Console should show: "Calling: getAllProducts"
```

---

## 🎯 Phase 2 Preview (Next Steps)

Once Phase 1 is complete, Phase 2 addresses:

1. **Fix Chapter 5 Regression** (12 hours)
   - Keep Chapter 4's sophisticated catalog code
   - ADD configuration features
   - Don't replace complex code with simple code

2. **Restore Feature Continuity** (16 hours)
   - Add health + metrics to Chapter 8 payment service
   - Keep telemetry in Chapter 9
   - Maintain all features through Chapter 10-11

3. **Fix Chapter 8 Pattern Introduction** (6 hours)
   - Split fault tolerance patterns into subsections
   - Document annotation execution order
   - Provide progressive examples

---

## 💡 Pro Tips

### For Version Changes
- Use `grep -r "version" pom.xml` to find all version references
- Update both code AND documentation together
- Test builds after each change

### For Documentation Updates
- Match code snippets exactly to actual code
- Include file paths in documentation
- Add "What's new" sections between chapters

### For Testing
- Test each chapter independently
- Verify ports are correct (9080 for HTTP)
- Check console output for errors

### For Code Changes
- Make minimal modifications
- Test before committing
- Document why changes were made

---

## 📞 Getting Help

If you encounter issues:

1. Check the detailed reports:
   - `docs/reviews/TUTORIAL_ALIGNMENT_REVIEW_COMPLETE.md`
   - `docs/reviews/series-level-integrity-report.md`
   - `docs/reviews/FINDINGS_VISUAL_SUMMARY.md`

2. Verify your changes:
   - Build succeeds: `mvn clean package`
   - Server starts: `mvn liberty:run`
   - API responds: `curl http://localhost:9080/...`

3. Common errors:
   - **Port in use:** Change to different port or stop other process
   - **Java version:** Ensure JDK 17 is active (`java -version`)
   - **Maven not found:** Install Maven 3.9+

---

## 📊 Progress Tracking

Use this checklist to track Phase 1 completion:

### Chapter 2 Fixes
- [ ] Java version changed to 17
- [ ] Ports changed to 9080/9443  
- [ ] README updated for port 9080
- [ ] Build succeeds
- [ ] Server runs on 9080
- [ ] API accessible

### Version Documentation Fixes
- [ ] chapter02-00.adoc updated (6.1 → 7.1)
- [ ] chapter02-01.adoc updated (pom examples)
- [ ] chapter02-02.adoc updated (version refs)
- [ ] README.adoc updated (6.1 → 7.1)
- [ ] All chapters verified for 6.1 refs
- [ ] Version tables added to chapters

### Chapter 3 Fixes
- [ ] ProductRepository removed from docs
- [ ] ProductService documented
- [ ] Interceptor section added
- [ ] LoggingInterceptor explained
- [ ] @Logged annotation explained
- [ ] beans.xml configuration shown
- [ ] Usage examples provided
- [ ] Build succeeds
- [ ] Logging visible in console

---

## 🚀 Time Estimates

| Task | Time | Difficulty |
|------|------|------------|
| Ch2 Java version | 30 min | Easy |
| Ch2 port change | 15 min | Easy |
| Ch2 README update | 30 min | Easy |
| Version docs search/replace | 2 hours | Easy |
| Version docs verification | 2 hours | Medium |
| Ch3 service docs | 4 hours | Medium |
| Ch3 interceptor docs | 4 hours | Medium |
| Testing all changes | 1 hour | Easy |
| **Total Phase 1** | **14 hours** | **Medium** |

---

## ✅ Success Criteria

Phase 1 is complete when:

1. ✅ All Chapter 2-11 code builds without errors
2. ✅ All documentation references MicroProfile 7.1 (not 6.1)
3. ✅ Chapter 2 server runs on port 9080
4. ✅ Chapter 3 documentation matches actual code
5. ✅ Interceptors fully documented in Chapter 3
6. ✅ All version inconsistencies resolved
7. ✅ No build failures due to Java version mismatch

---

**Next:** After completing Phase 1, see `TUTORIAL_ALIGNMENT_REVIEW_COMPLETE.md` for Phase 2-4 details.

**Questions?** Refer to the comprehensive review reports in `docs/reviews/`
