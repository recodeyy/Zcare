# ZCare Pharmacy Management System — Complete Technical Audit

> **Audit Date:** 2026-04-09  
> **Auditor:** Senior Full-Stack + System Architect (Spring Boot / Database / Scalable SaaS)  
> **Repository:** [recodeyy/Zcare](https://github.com/recodeyy/Zcare)  
> **Spring Boot Version:** 3.5.12 | **Java:** 17 | **Database:** PostgreSQL

---

## Table of Contents

1. [Architecture Analysis](#1-architecture-analysis)
2. [Feature Implementation Status per Module](#2-feature-implementation-status-per-module)
3. [Code Quality](#3-code-quality)
4. [Database Design](#4-database-design)
5. [End-to-End API / Data Flow Tracing](#5-end-to-end-api--data-flow-tracing)
6. [Dependencies & External Integrations](#6-dependencies--external-integrations)
7. [Scalability / Production Readiness](#7-scalability--production-readiness)
8. [Completion Percentage Estimates](#8-completion-percentage-estimates-by-area)
9. [Critical Missing Components](#9-critical-missing-components)
10. [Phased Roadmap to Production](#10-phased-roadmap-to-production)

---

## 1. Architecture Analysis

### 1.1 Spring Boot Structure & Layers

The application follows a classic **three-layer monolith** pattern:

```
src/main/java/com/pharmacy/
├── PharmacyApplication.java          ← Entry point
├── config/       (cross-cutting: Security, JPA, Swagger, CORS, Seeder)
├── controller/   (HTTP layer: 4 controllers)
├── service/      (business logic: 5 services)
├── repository/   (data access: 4 JPA repos)
├── model/        (JPA entities: 4 entities)
├── dto/          (9 DTOs)
├── security/     (JWT filter + util)
└── exception/    (global handler + 2 custom exceptions)
```

**Package naming inconsistency:** The base package is `com.pharmacy` but the Maven `groupId` in `pom.xml` (line 8) is `com.zcare`. This is minor naming debt but could cause confusion in multi-module future work.

**App main class** (`PharmacyApplication.java`): Uses `@SpringBootApplication` + `@ConfigurationPropertiesScan` — clean and correct.

### 1.2 Module Breakdown

| Module | Controller | Service | Entities |
|--------|-----------|---------|---------|
| **Auth** | `AuthController` | `AuthService`, `UserDetailsServiceImpl` | `User` |
| **Medicine / Inventory** | `MedicineController` | `MedicineService` | `Medicine` |
| **Billing / Orders** | `BillingController` | `BillingService` | `CustomerOrder`, `OrderItem` |
| **User Management** | `UserController` | `UserService` | `User` |

> **Notable gap:** `BillingController` (`controller/BillingController.java`) only exposes `POST /api/orders`. There is **no GET endpoint** to list or retrieve orders anywhere in the codebase.

### 1.3 API Patterns & Request Lifecycle

- **REST over HTTP/JSON** with standard verb mapping (GET / POST / PUT / DELETE / PATCH).
- **Stateless JWT** authentication: every request passes through `JwtAuthFilter` → `JwtUtil.extractUsername` → `UserDetailsServiceImpl.loadByUsername` → `SecurityContextHolder`.
- Request lifecycle:

  ```
  HTTP Request
    → JwtAuthFilter (security/JwtAuthFilter.java)
    → DispatcherServlet
    → Controller (@RestController)
    → Service (@Service)
    → Repository (JpaRepository)
    → PostgreSQL
    → Response (DTO)
  ```

- **Spring Validation** (`@Valid`, Bean Validation annotations on DTOs) runs before the service layer.
- **`@RestControllerAdvice`** (`exception/GlobalExceptionHandler.java`) centralises exception→JSON mapping.

### 1.4 Monolith vs Microservice Readiness

The system is a **single-module Spring Boot monolith**. There are no module boundaries, shared libraries, or service-to-service communication patterns. Feature packages (`medicine`, `billing`, `auth`) are plain sub-packages — not bounded contexts. The codebase is **not microservice-ready** in its current form; extraction would require significant refactoring of shared entities and data ownership.

---

## 2. Feature Implementation Status per Module

### 2.1 Auth Module

| Feature | Status | Location |
|---------|--------|---------|
| User registration | ✅ Implemented | `AuthController.register`, `AuthService.register` |
| Login / JWT issuance | ✅ Implemented | `AuthController.authenticate`, `AuthService.authenticate` |
| JWT validation filter | ✅ Implemented | `security/JwtAuthFilter.java` |
| Role-based access (`ADMIN`, `PHARMACIST`) | ✅ Implemented | `config/SecurityConfig.java`, `@PreAuthorize` on controllers |
| Role hierarchy (`ADMIN > PHARMACIST`) | ✅ Implemented | `SecurityConfig.roleHierarchy()` |
| Token refresh / revocation | ❌ Missing | No refresh token endpoint, no token blacklist |
| `CUSTOMER` role | ⚠️ Partial | Defined in `User.Role` enum but no dedicated endpoints |
| Password change / reset | ❌ Missing | No endpoint exists |
| Email verification | ❌ Missing | No email infrastructure |
| Account lockout / brute-force protection | ❌ Missing | — |

> **`RegisterRequest` mismatch:** The DTO (`dto/RegisterRequest.java`) exposes a `role` field with `@NotNull` validation, but `AuthService.register` ignores it and hardcodes `User.Role.PHARMACIST`. This is a documentation–code mismatch that can confuse API consumers.

### 2.2 Medicine / Inventory Module

| Feature | Status | Location |
|---------|--------|---------|
| CRUD (create / read / update / delete) | ✅ Implemented | `MedicineController`, `MedicineService` |
| Stock quantity tracking | ✅ Implemented | `Medicine.stockQuantity`; `BillingService` deducts on order |
| Expiry date tracking | ✅ Implemented | `Medicine.expiryDate` + `getExpiredMedicines()` |
| Low-stock alerts (query) | ✅ Implemented | `GET /api/medicines/low-stock?threshold=N` |
| Name search | ✅ Implemented | `GET /api/medicines/search?name=X` |
| Supplier tracking | ❌ Missing | `manufacturer` string exists; no Supplier entity, PO tracking, or reorder logic |
| Batch / lot tracking | ❌ Missing | — |
| Barcode / SKU | ❌ Missing | — |
| Category filter endpoint | ❌ Missing | README documents `GET /api/medicines/category/{cat}` — **not implemented** |
| `/api/medicines/expiring-soon` | ❌ Missing | README documents it; controller only has already-expired query |
| `PATCH /api/medicines/{id}/stock` | ❌ Missing | README documents it — **not implemented** |
| Consistent auditing | ⚠️ Inconsistent | `Medicine` uses manual `@PrePersist`/`@PreUpdate`; `User` uses Spring Data `@CreatedDate`/`@LastModifiedDate` |

### 2.3 Orders / Billing Module

| Feature | Status | Location |
|---------|--------|---------|
| Create order (multi-item) | ✅ Implemented | `POST /api/orders` → `BillingService.createOrder` |
| Stock deduction on order | ✅ Implemented | `BillingService.createOrder` |
| Total amount calculation | ✅ Implemented | `BillingService` sums `price * quantity` |
| Order item line-price snapshot | ✅ Implemented | `OrderItem.price` stores line total at order time |
| Insufficient-stock validation | ⚠️ Wrong exception | Throws `IllegalArgumentException` → HTTP 400 (should be 409); `InsufficientStockException` exists but is never thrown |
| Get all orders (list) | ❌ Missing | No GET endpoint |
| Get order by ID | ❌ Missing | No GET endpoint |
| Order status (pending / confirmed / dispensed / cancelled) | ❌ Missing | No status field on `CustomerOrder` |
| Order history per user | ❌ Missing | `createdBy` is a plain `String`; no FK to `User` |
| Order cancellation / return | ❌ Missing | — |
| Invoice / PDF generation | ❌ Missing | — |
| Tax calculation | ❌ Missing | — |
| Discounts / promotions | ❌ Missing | — |
| Payment tracking (cash / card / insurance) | ❌ Missing | — |
| Prescription linkage | ❌ Missing | — |

### 2.4 User Management Module

| Feature | Status | Location |
|---------|--------|---------|
| List all users (admin) | ✅ Implemented | `GET /api/users` |
| Get user by ID (admin) | ✅ Implemented | `GET /api/users/{id}` |
| Delete user (admin) | ✅ Implemented | `DELETE /api/users/{id}` |
| Update role (admin) | ✅ Implemented | `PATCH /api/users/{id}/role` |
| Activate / deactivate user | ✅ Implemented | `PATCH /api/users/{id}/status` |
| `UserResponse` DTO isolation | ❌ Broken | `UserController` returns raw `User` entity — **exposes hashed password**. `UserResponse.java` is defined (`dto/UserResponse.java`) but never used |
| User profile (self) | ❌ Missing | No `GET /api/users/me` |
| User self-update | ❌ Missing | — |

---

## 3. Code Quality

### 3.1 TODOs / Placeholders

- No `TODO` or `FIXME` comments found in source.
- `config/SwaggerConfig.java` description mentions "Prescription Management, Reports & Analytics" — features that don't exist yet.
- `README.md` documents 3–4 endpoints (`/category/{cat}`, `/expiring-soon`, `PATCH /stock`, etc.) that are unimplemented.

### 3.2 Separation of Concerns

- Clean layering overall — controllers are thin, business logic in services.
- **Issue:** `BillingService` directly modifies `Medicine.stockQuantity` inline during order creation (`service/BillingService.java`). This crosses bounded-context boundaries; stock management belongs in `MedicineService`.
- **Issue:** `UserController` returns raw `User` entity (which implements `UserDetails` and contains a hashed password). `UserResponse` DTO exists but is unused — a clear oversight (`controller/UserController.java`).
- **Issue:** `config/DataSeeder.java` logs plaintext default credentials at `INFO` level — a security antipattern in production.

### 3.3 Error Handling & Exception Management

| Scenario | Handling | Quality |
|----------|---------|---------|
| `BadCredentialsException` | `GlobalExceptionHandler` → 401 | ✅ |
| `ResourceNotFoundException` | → 404 | ✅ |
| `DataIntegrityViolationException` | → 409 | ✅ |
| `ExpiredJwtException` | → 401 | ✅ |
| `SignatureException` | → 401 | ✅ |
| Insufficient stock | `IllegalArgumentException` → 400 | ⚠️ Wrong status (should be 409); `InsufficientStockException` exists but is never used |
| Bean validation failures (`MethodArgumentNotValidException`) | **Not handled** | ❌ Falls through to Spring's default handler — field-level messages are lost |
| `createResponse` message | Always `"Something went wrong"` | ❌ **Bug:** `body.put("message", "Something went wrong")` ignores the `message` parameter entirely |

> **Critical bug in `exception/GlobalExceptionHandler.java`:** The `createResponse` private method accepts a `message` parameter but always writes `"Something went wrong"` to the response body — the specific error message never reaches the client.

### 3.4 Logging

- `@Slf4j` used in `MedicineService`, `JwtAuthFilter`, `DataSeeder`, `GlobalExceptionHandler` — appropriate.
- `security/JwtUtil.java` — `getSigningKey()` silently pads short keys without a log warning.
- `config/DataSeeder.java` logs default credentials (`admin / admin123`) at `INFO` level — visible in production logs.
- No structured / MDC logging (correlation IDs, request tracing).

### 3.5 Naming Conventions

- Generally follows Java conventions (PascalCase classes, camelCase methods). ✅
- `BillingService` handles orders, not billing-specific logic (invoices / tax) — misleading name.
- Package is `com.pharmacy` while `artifactId` is `zcare-backend` and `groupId` is `com.zcare` — inconsistent branding.

---

## 4. Database Design

### 4.1 Entities & Relationships

```
users
  id PK, username UNIQUE, password, fullName, email UNIQUE,
  role ENUM, createdAt, updatedAt, active
        ↑
        (no FK — only a plain String createdBy)
        ↓
orders
  id PK, orderDate, totalAmount, createdBy VARCHAR
        ↓  1 : N
order_items
  id PK, order_id FK→orders, medicine_id FK→medicines,
  quantity, price (line total)
        ↑
medicines
  id PK, name, category, price, stockQuantity, expiryDate,
  manufacturer, createdAt, updatedAt
```

> **Critical gap:** `CustomerOrder.createdBy` is a plain `String` (`model/CustomerOrder.java`). There is no foreign key from `orders` to `users`, which means:
> - No referential integrity for order–user linkage.
> - Deleting a user leaves orphaned `createdBy` strings in orders.
> - No efficient "all orders by user" query — requires a full table scan on `orders`.

### 4.2 Normalization

- Schema is roughly **3NF**.
- `OrderItem.price` stores the computed line total (`medicine.price * quantity`) — correct for historical price snapshotting, but the column name `price` (not `lineTotal`) is misleading.

### 4.3 JPA Usage

| Practice | Status |
|---------|--------|
| `GenerationType.IDENTITY` for PK | ✅ Correct for PostgreSQL |
| `CascadeType.ALL` + `orphanRemoval` on `CustomerOrder.items` | ✅ Correct |
| `FetchType.LAZY` on order-item associations | ✅ Good for performance |
| `@JsonManagedReference` / `@JsonBackReference` | ✅ Prevents infinite JSON recursion |
| Auditing consistency | ❌ `User` uses `@EntityListeners`; `Medicine` uses manual `@PrePersist`/`@PreUpdate` |
| `Medicine.updatedAt` nullability | ❌ No `nullable = false` on the column |
| `CustomerOrder` auditing | ❌ No `@EntityListeners` |

### 4.4 Indexes

No explicit `@Index` annotations on any entity. Required for production:

| Column | Reason |
|--------|--------|
| `medicines.name` | Used in `LIKE` search (`findByNameContainingIgnoreCase`) |
| `medicines.expiryDate` | Used in expiry threshold query |
| `medicines.stockQuantity` | Used in low-stock query |
| `orders.createdBy` (or FK `user_id`) | Needed for user-order history |
| `users.username` | Has `UNIQUE` constraint — implicit index ✅ |

### 4.5 Transactions

| Location | Status |
|---------|--------|
| `BillingService.createOrder` | ✅ `@Transactional` — stock deduction and order save are atomic |
| `MedicineService.updateMedicine` | ✅ `@Transactional` |
| `MedicineService.addMedicine` / `deleteMedicine` | ⚠️ Not `@Transactional` — minor risk |

> **Race condition:** `BillingService.createOrder` checks stock then updates it without pessimistic or optimistic locking. Under concurrent requests, two threads can both pass the check and oversell. `BillingIntegrationTest.shouldHandleConcurrentOrdersWithConflict` exercises this exact scenario — it is likely non-deterministic without a locking strategy (`service/BillingService.java`).

### 4.6 DDL Strategy

`spring.jpa.hibernate.ddl-auto=update` is set in the production properties file (`src/main/resources/application.properties`) — **dangerous**. Production should use `validate` with Flyway or Liquibase managing schema migrations.

---

## 5. End-to-End API / Data Flow Tracing

### Flow: `POST /api/orders` (Create Order)

```
1. HTTP POST /api/orders
   Body: [{"medicineId": 1, "quantity": 2}, ...]
   Authorization: Bearer <jwt>

2. security/JwtAuthFilter.doFilterInternal
   → jwtUtil.extractUsername(jwt)
   → userDetailsService.loadUserByUsername(username)
   → jwtUtil.isTokenValid(jwt, userDetails)
   → SecurityContextHolder.set(authToken)

3. config/SecurityConfig — authorizeHttpRequests
   → anyRequest().authenticated()              ✓
   → @PreAuthorize("hasRole('PHARMACIST')")    ✓

4. controller/BillingController.createOrder(items, authentication)
   → authentication.getName() → username (used as createdBy)
   → billingService.createOrder(items, createdBy)

5. service/BillingService.createOrder  [@Transactional]
   For each OrderItemRequest:
     a. medicineRepository.findById(id)       → 404 if not found
     b. quantity > stockQuantity?             → IllegalArgumentException (400)
     c. itemTotal = medicine.price * quantity
     d. medicine.stockQuantity -= quantity    (in-memory; persisted by save)
     e. build OrderItem and add to order

   order.totalAmount = sum
   customerOrderRepository.save(order)        (cascades → OrderItem saves)

6. BillingService.toResponse(savedOrder)
   → CustomerOrderResponse + List<OrderItemResponse>

7. HTTP 200 OK  — JSON body
```

> **Gap:** No `GET /api/orders` endpoint exists — orders are written but not retrievable through the API.

---

## 6. Dependencies & External Integrations

### 6.1 Core Dependencies (`pom.xml`)

| Dependency | Version | Purpose |
|-----------|---------|---------|
| `spring-boot-starter-parent` | **3.5.12** | Base platform |
| `spring-boot-starter-web` | Managed | REST API |
| `spring-boot-starter-security` | Managed | Authentication / authorisation |
| `spring-boot-starter-data-jpa` | Managed | ORM |
| `spring-boot-starter-validation` | Managed | Bean Validation |
| `postgresql` | Managed | DB driver |
| `jjwt-api/impl/jackson` | **0.12.3** | JWT (modern JJWT API) |
| `springdoc-openapi-starter-webmvc-ui` | **2.3.0** | Swagger UI |
| `lombok` | Managed | Boilerplate reduction |
| `spring-boot-starter-test` + `spring-security-test` | Managed | Testing |

### 6.2 Missing Dependencies

| Dependency | Reason Needed |
|-----------|--------------|
| **H2** (`scope: test`) | `application-test.properties` configures H2 for tests — no H2 dep in `pom.xml`; tests will fail |
| **Flyway / Liquibase** | Database schema migration management |
| **Spring Boot Actuator** | Health endpoint referenced in `SecurityConfig.PUBLIC_URLS` but Actuator is not on classpath |
| **Caffeine / Redis** | Caching |
| **Email / SMS library** | Notifications |
| **Payment SDK** | Payment integration |
| **Spring DevTools** | Faster dev reload |

### 6.3 External Integrations

**None currently exist.** No email, SMS, payment gateway, or external pharmacy data source integrations are implemented.

**CI/CD** (`.github/workflows/ci-cd.yml`) deploys to **Railway** via `railway up` — a PaaS that handles container provisioning.

---

## 7. Scalability / Production Readiness

### 7.1 Multi-tenancy

**Not implemented.** Single-tenant system. No `tenantId` on any entity, no schema-per-tenant, no row-level security. Scaling to SaaS requires a complete data model redesign.

### 7.2 Performance

| Concern | Status |
|--------|--------|
| HikariCP connection pool | ✅ Configured (max 10, min idle 5) |
| Lazy loading on associations | ✅ |
| N+1 queries | ⚠️ Risk: `BillingService.toResponse` iterates `order.getItems()` — each `item.getMedicine()` in the stream may fire an extra query |
| LIKE search without index | ❌ `findByNameContainingIgnoreCase` will full-scan `medicines` table |
| Pagination | ❌ `getAllMedicines()` and `getAllUsers()` return the entire table with no pagination |
| Bulk operations | ❌ None |

### 7.3 Caching

**None.** No `@Cacheable`, no Spring Cache abstraction, no Redis / Caffeine.

### 7.4 Background Jobs

**None.** No `@Scheduled` tasks for expiry alerts, low-stock notifications, or report generation.

### 7.5 Rate Limiting

**None.** No rate limiting on auth endpoints — brute-force risk on `POST /api/auth/login`.

### 7.6 Security Posture

| Item | Status |
|------|--------|
| Passwords BCrypt-hashed | ✅ |
| JWT signed with HMAC-SHA | ✅ |
| CSRF disabled (stateless JWT API) | ✅ Appropriate |
| CORS configured | ✅ (localhost:3000, localhost:5173) |
| Content-Security-Policy header | ✅ Basic `default-src 'self'` |
| Frame options `sameOrigin` | ✅ |
| HTTPS enforced | ❌ No HTTPS configuration |
| JWT secret hardcoded in `application.properties` | ❌ Weak default visible in source |
| Default credentials logged at INFO | ❌ `config/DataSeeder.java` logs `admin / admin123` |
| `UserController` exposes `User` entity with `password` field | ❌ Must use `UserResponse` DTO |
| Short JWT key silently padded | ❌ `security/JwtUtil.getSigningKey()` pads without warning |
| `MethodArgumentNotValidException` not handled | ❌ Validation errors produce cryptic responses |
| Token revocation / blacklist | ❌ Missing |

### 7.7 Configuration Management

- `application-dev.properties` uses `${DB_PASSWORD:password}` and `${JWT_SECRET:change_me...}` — partial environment variable support.
- Main `application.properties` has a hardcoded DB password and JWT secret — not appropriate for any shared environment.
- **No `Dockerfile`** or **`docker-compose.yml`** in the repository.
- No `.env.example` file.

---

## 8. Completion Percentage Estimates by Area

| Area | Estimate | Notes |
|------|---------|-------|
| **Auth (JWT login / register / roles)** | ~75% | Core works; missing refresh tokens, password reset, brute-force protection |
| **Medicine CRUD & inventory** | ~65% | Core CRUD works; several documented endpoints unimplemented; no supplier entity; no pagination |
| **Orders (create)** | ~40% | Creation works; zero read endpoints; no status field; no history; no cancellation |
| **Billing (invoices / tax / discounts)** | ~5% | Price summing exists; no invoice, no tax, no discounts, no payment |
| **User management** | ~55% | Admin operations work; exposes password in responses; no self-service profile |
| **Exception handling** | ~55% | Most cases covered; validation errors broken; message always "Something went wrong" |
| **Database design** | ~50% | Basic schema works; missing FK order→user; missing indexes; wrong DDL strategy |
| **Testing** | ~40% | Integration tests exist and are well-structured; no unit tests; missing GET order / user mgmt tests |
| **Security hardening** | ~45% | JWT auth solid; many production gaps (HTTPS, secrets, rate limiting, token revocation) |
| **CI/CD** | ~50% | Pipeline exists; no Docker; no staging env; no post-deploy smoke tests |
| **Documentation** | ~40% | README is detailed but contains stale / incorrect endpoint docs |
| **Overall** | **~45%** | Solid foundation; significant work needed before production |

---

## 9. Critical Missing Components

Ranked by severity:

### 🔴 Critical

1. **GET endpoints for orders missing** — `BillingController` has no GET. Data is written but not retrievable via the API.  
   📄 `src/main/java/com/pharmacy/controller/BillingController.java`

2. **`UserController` exposes hashed passwords** — Returns raw `User` entity. `UserResponse` DTO exists but is unused.  
   📄 `src/main/java/com/pharmacy/controller/UserController.java`  
   📄 `src/main/java/com/pharmacy/dto/UserResponse.java`

3. **`GlobalExceptionHandler.createResponse` bug** — `body.put("message", "Something went wrong")` ignores the `message` parameter — no specific error ever reaches the caller.  
   📄 `src/main/java/com/pharmacy/exception/GlobalExceptionHandler.java`

4. **`MethodArgumentNotValidException` not handled** — `@Valid` validation failures return cryptic Spring default errors; field-level messages are lost.  
   📄 `src/main/java/com/pharmacy/exception/GlobalExceptionHandler.java`

5. **Race condition on stock deduction** — No pessimistic or optimistic locking (`@Version`) on `Medicine.stockQuantity`. Concurrent orders can oversell.  
   📄 `src/main/java/com/pharmacy/service/BillingService.java`

### 🟠 High

6. **`InsufficientStockException` defined but never used** — `BillingService` throws `IllegalArgumentException` for insufficient stock → HTTP 400 (should be 409). `InsufficientStockException` is dead code.  
   📄 `src/main/java/com/pharmacy/exception/InsufficientStockException.java`

7. **No FK from `CustomerOrder` to `User`** — `createdBy` is a plain `String`. No referential integrity, no cascades, no efficient user-order history query.  
   📄 `src/main/java/com/pharmacy/model/CustomerOrder.java`

8. **H2 not in `pom.xml`** — `application-test.properties` specifies H2 for tests but no H2 dependency exists — tests will fail to start.  
   📄 `pom.xml`

9. **`ddl-auto=update` in production profile** — Schema migrations should be managed by Flyway / Liquibase.  
   📄 `src/main/resources/application.properties`

### 🟡 Medium

10. **No pagination on list endpoints** — `getAllMedicines()` and `getAllUsers()` load the entire table into memory.

11. **No Dockerfile / docker-compose** — Cannot containerise the application without additional work.

12. **README documents non-existent endpoints** — `/api/medicines/category/{cat}`, `/api/medicines/expiring-soon`, `PATCH /api/medicines/{id}/stock` are documented but not implemented.

---

## 10. Phased Roadmap to Production

### Phase 1 — Critical Bug Fixes *(1–2 sprints)*

1. Fix `GlobalExceptionHandler.createResponse` to pass `message` into the response body instead of hardcoding `"Something went wrong"`.
2. Add `MethodArgumentNotValidException` handler to return field-level validation errors.
3. Replace raw `User` return in `UserController` with the existing `UserResponse` DTO — eliminate password exposure.
4. Replace `IllegalArgumentException` for insufficient stock with `InsufficientStockException` and map it to HTTP 409 in `GlobalExceptionHandler`.
5. Add missing GET endpoints to `BillingController`: `GET /api/orders`, `GET /api/orders/{id}`, `GET /api/orders` filtered by user.
6. Add H2 dependency with `<scope>test</scope>` to `pom.xml` (or migrate tests to Testcontainers + PostgreSQL).

### Phase 2 — Data Model & Integrity *(1 sprint)*

7. Add `@ManyToOne` FK from `CustomerOrder` to `User` (replace `createdBy` string with a proper join column).
8. Add `@Version` (optimistic locking) on `Medicine.stockQuantity`, or use `SELECT FOR UPDATE` pessimistic locking in `BillingService`, to prevent race conditions.
9. Add `@Index` annotations for `medicines.name`, `medicines.expiryDate`, `medicines.stockQuantity`, `orders.user_id`.
10. Standardise auditing: apply `@EntityListeners(AuditingEntityListener.class)` to `Medicine` and `CustomerOrder`; remove manual `@PrePersist`/`@PreUpdate`.
11. Introduce **Flyway** for schema migration management; change `ddl-auto=validate`.

### Phase 3 — Security Hardening *(1 sprint)*

12. Externalise all secrets — remove hardcoded DB password and JWT secret from `application.properties`; require environment variables.
13. Remove default credential logging from `config/DataSeeder.java`.
14. Add JWT refresh token endpoint and token revocation (DB or Redis blacklist).
15. Implement rate limiting on `/api/auth/**` (Bucket4j or Spring Security's built-in request throttle).
16. Configure HTTPS / TLS (via Railway environment config or a reverse proxy).

### Phase 4 — Feature Completion *(2–3 sprints)*

17. Implement documented but missing medicine endpoints: `GET /api/medicines/expiring-soon`, `GET /api/medicines/category/{cat}`, `PATCH /api/medicines/{id}/stock`.
18. Add `OrderStatus` enum to `CustomerOrder` (PENDING / CONFIRMED / DISPENSED / CANCELLED) with status transitions.
19. Implement a `Supplier` entity and purchase-order tracking.
20. Add `Pageable` pagination to all list endpoints.
21. Implement password change (`PATCH /api/users/me/password`) and user self-service profile.
22. Add order cancellation with stock reversion (transactional).

### Phase 5 — Billing Completeness *(2 sprints)*

23. Introduce an `Invoice` entity linked to `CustomerOrder`.
24. Add configurable tax calculation (per category or globally).
25. Implement a discount / promotion model.
26. Add a `PaymentRecord` entity with payment method (CASH / CARD / INSURANCE).
27. Generate PDF invoices (JasperReports or iText).

### Phase 6 — Production Infrastructure *(1–2 sprints)*

28. Add a multi-stage `Dockerfile` (Maven build → JRE runtime image).
29. Add `docker-compose.yml` for local development (app + PostgreSQL).
30. Add Spring Boot Actuator with secured health / metrics endpoints.
31. Integrate structured logging with correlation IDs (MDC + Logback / Logstash).
32. Add Spring Cache with **Caffeine** (medicine catalogue) or **Redis** (distributed deployments).
33. Implement `@Scheduled` background jobs: expiry alerts, low-stock notifications.
34. Expand CI/CD: add Docker image build + push to registry, add post-deploy smoke tests, add `develop` → staging branch strategy.

### Phase 7 — SaaS / Multi-tenancy *(Future)*

35. Design tenant model (schema-per-tenant vs row-level with `tenantId` discriminator).
36. Add tenant-aware request context propagation.
37. Introduce module boundaries (or separate services) for Medicine, Billing, and Auth.
38. Add an API gateway / rate-limiting tier.
39. Implement observability stack (metrics → Prometheus / Grafana, tracing → Zipkin / OpenTelemetry).

---

## Summary

ZCare is a **well-structured Spring Boot 3 monolith** with a clean layered architecture, solid JWT authentication, and a good integration-test foundation. The most critical gaps are:

- **No order retrieval API** — orders are created but cannot be fetched.
- **Password leakage** through the user-management endpoint.
- **Global error handler bug** that masks all specific error messages.
- **No concurrent-access protection** on stock deduction.
- **Several documented API endpoints that don't exist**.
- **Missing referential integrity** between orders and users.

The system sits at approximately **~45% production readiness** and requires focused work across the phases above before serving real pharmacy workloads.

---

*Generated by automated technical audit — recodeyy/Zcare repository — 2026-04-09*
