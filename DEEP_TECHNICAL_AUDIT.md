# 🔬 ZCare — Deep Technical Audit Report

> **Auditor:** Senior Full-Stack Architect + Backend Engineer + Database Expert  
> **Date:** 2026-04-09  
> **Repository:** `recodeyy/Zcare`  
> **Stack:** Spring Boot 3.5.12 · Java 17 · PostgreSQL · JWT · Springdoc/Swagger · Railway CI/CD  
> **Scope:** Backend-only (no frontend repository detected)

---

## Table of Contents

1. [System Overview](#1-system-overview)
2. [Critical Issues — High Priority](#2-critical-issues--high-priority)
3. [Missing Connections](#3-missing-connections)
4. [Backend Analysis](#4-backend-analysis)
5. [Database Analysis](#5-database-analysis)
6. [Frontend Integration Issues](#6-frontend-integration-issues)
7. [Performance Issues](#7-performance-issues)
8. [Security Issues](#8-security-issues)
9. [Confusing Areas](#9-confusing-areas)
10. [Production Readiness Score](#10-production-readiness-score)
11. [Action Plan](#11-action-plan)

---

## 1. System Overview

### Architecture Summary

ZCare is a **single-module Spring Boot 3 REST monolith** using standard three-layer architecture:

```
[HTTP Client / Future Frontend]
          ↓  REST JSON
[Spring Security Filter Chain]
   JwtAuthFilter (OncePerRequestFilter)
          ↓
[Controller Layer]  — 4 controllers (Auth, Medicine, Billing, User)
          ↓
[Service Layer]     — 5 services (Auth, Medicine, Billing, User, UserDetails)
          ↓
[Repository Layer]  — 4 JPA repositories (Spring Data)
          ↓
[PostgreSQL Database]  — 4 tables (users, medicines, orders, order_items)
```

### Data Flow (Text Diagram)

#### Auth Flow
```
POST /api/auth/login
  → AuthController.authenticate()
  → AuthService.authenticate()
  → AuthenticationManager.authenticate()  [validates username+password vs BCrypt hash]
  → UserRepository.findByUsername()
  → JwtUtil.generateToken()               [builds JWT with role claim]
  ← AuthResponse { token, username, role }
```

#### Authenticated Request Flow
```
Any Protected Request
  → JwtAuthFilter.doFilterInternal()
      → JwtUtil.extractUsername(jwt)
      → UserDetailsServiceImpl.loadUserByUsername()  [DB hit on EVERY request]
      → JwtUtil.isTokenValid()
      → SecurityContextHolder.set(authToken)
  → Controller (@PreAuthorize role check)
  → Service → Repository → PostgreSQL
  ← ResponseEntity<DTO>
```

#### Order Creation Flow
```
POST /api/orders  [PHARMACIST only]
  → BillingController.createOrder(items, authentication)
  → BillingService.createOrder(items, createdBy)   [@Transactional]
      For each item:
        → MedicineRepository.findById()             [N queries for N items]
        → inline stock check (no locking)
        → medicine.setStockQuantity(qty - ordered)  [dirty entity update]
        → build OrderItem
  → CustomerOrderRepository.save(order)            [cascades → order_items]
  ← CustomerOrderResponse
```

### Key Observations at a Glance

| Area | Status |
|------|--------|
| Auth (JWT login/register) | ✅ Functional |
| Role-based access control | ✅ Functional |
| Medicine CRUD | ✅ Functional |
| Order creation | ✅ Functional |
| Order retrieval | ❌ **Zero GET endpoints** |
| Error message delivery | ❌ **Always "Something went wrong"** |
| Password exposure | ❌ **Hashed password in all user API responses** |
| Concurrent stock safety | ❌ **Race condition — overselling possible** |
| H2 test dependency | ❌ **Missing — all tests fail to start** |

---

## 2. Critical Issues — High Priority

---

### 🔴 CRITICAL-1 — Error Handler Bug: All Error Messages Silently Discarded

**File:** `src/main/java/com/pharmacy/exception/GlobalExceptionHandler.java`

**Root Cause:**

```java
// Line in createResponse():
body.put("message", "Something went wrong");   // ← HARDCODED — ignores the `message` param
```

The `createResponse(Exception ex, String message, HttpStatus status)` method accepts a `message` parameter but NEVER uses it. Every error response — whether it's "Invalid username or password", "Medicine not found with id: 42", or "Insufficient stock" — will return the exact same body:

```json
{ "message": "Something went wrong", "status": 400 }
```

**Impact:**
- Frontend gets zero actionable information about what went wrong.
- API consumers cannot distinguish between "wrong credentials", "not found", or "server crash".
- Debugging in production is blind — the logs contain the real message but the response doesn't.
- Every test asserting on response body message content will pass against the wrong string.

**Fix:**
```java
private ResponseEntity<Map<String, Object>> createResponse(Exception ex, String message, HttpStatus status) {
    log.error("Exception: [{}], Message: [{}]", ex.getClass().getSimpleName(), ex.getMessage());
    Map<String, Object> body = new HashMap<>();
    body.put("timestamp", LocalDateTime.now());
    body.put("status", status.value());
    body.put("error", status.getReasonPhrase());
    body.put("message", message);   // ← USE THE PARAM
    return new ResponseEntity<>(body, status);
}
```

---

### 🔴 CRITICAL-2 — `UserController` Exposes Hashed Passwords in API Responses

**Files:** `src/main/java/com/pharmacy/controller/UserController.java`, `src/main/java/com/pharmacy/dto/UserResponse.java`

**Root Cause:**

`UserController.getAllUsers()` and `getUserById()` return raw `User` entity objects, which implement `UserDetails` and carry the `password` (BCrypt hash) field. The DTO that would prevent this (`UserResponse.java`) is **defined but never used**.

```java
// BROKEN — returns User entity including password field
public ResponseEntity<List<User>> getAllUsers() {
    return ResponseEntity.ok(userService.getAllUsers());
}

// CORRECT DTO exists but is dead code:
// dto/UserResponse.java — never referenced anywhere
```

**Impact (Critical):**
- Every admin API response leaks BCrypt hashes for ALL users.
- Even though BCrypt is one-way, hash exposure allows offline cracking attempts, rainbow table attacks on weak passwords, and signals system internals.
- Violates every data minimisation principle and most compliance frameworks (GDPR, HIPAA).

**Fix — `UserController`:**
```java
public ResponseEntity<List<UserResponse>> getAllUsers() {
    return ResponseEntity.ok(userService.getAllUsers().stream()
        .map(u -> new UserResponse(u.getId(), u.getUsername(), u.getFullName(),
                                   u.getEmail(), u.getRole(), u.isActive(),
                                   u.getCreatedAt()))
        .toList());
}
```
Wire `UserService` to return `List<UserResponse>` directly.

---

### 🔴 CRITICAL-3 — Zero Order Retrieval API: Write-Only Billing Module

**File:** `src/main/java/com/pharmacy/controller/BillingController.java`

**Root Cause:**

`BillingController` contains exactly one endpoint: `POST /api/orders`. There are no GET endpoints anywhere in the codebase to list or retrieve orders.

```java
@RestController
@RequestMapping("/api/orders")
public class BillingController {
    @PostMapping          // ← Only endpoint in the entire controller
    public ResponseEntity<CustomerOrderResponse> createOrder(...) { ... }
    // GET /api/orders       → 404
    // GET /api/orders/{id}  → 404
    // GET /api/orders/mine  → 404
}
```

**Impact:**
- Orders are permanently unqueryable after creation via API (data goes in, never comes out).
- No pharmacist can review order history.
- No admin can audit what was dispensed.
- Billing reconciliation is impossible without direct DB access.

**Fix:** Add to `BillingController`:
```java
@GetMapping
@PreAuthorize("hasRole('PHARMACIST')")
public ResponseEntity<List<CustomerOrderResponse>> getAllOrders() {
    return ResponseEntity.ok(billingService.getAllOrders());
}

@GetMapping("/{id}")
@PreAuthorize("hasRole('PHARMACIST')")
public ResponseEntity<CustomerOrderResponse> getOrder(@PathVariable Long id) {
    return ResponseEntity.ok(billingService.getOrderById(id));
}
```

---

### 🔴 CRITICAL-4 — `MethodArgumentNotValidException` Not Handled: Validation Errors Are Silent

**File:** `src/main/java/com/pharmacy/exception/GlobalExceptionHandler.java`

**Root Cause:**

`@Valid` annotations exist on all request bodies (e.g., `MedicineRequest`, `OrderItemRequest`, `RegisterRequest`) but `MethodArgumentNotValidException` is **not handled** by `GlobalExceptionHandler`. Spring's default error handling takes over and returns a 400 with `errors[]` array in Spring Boot's default format — inconsistent with the rest of the API.

**Impact:**
- Frontend receives validation errors in an unpredictable format (Spring Boot's default).
- If CRITICAL-1 is fixed, `@Valid` failures still use a different response structure.
- Field-level messages (e.g., "name must not be blank") are buried in nested `errors[].defaultMessage`.

**Fix — add to `GlobalExceptionHandler`:**
```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
    Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
        .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage,
                                  (a, b) -> a));
    Map<String, Object> body = new HashMap<>();
    body.put("timestamp", LocalDateTime.now());
    body.put("status", 400);
    body.put("error", "Validation Failed");
    body.put("fieldErrors", fieldErrors);
    return ResponseEntity.badRequest().body(body);
}
```

---

### 🔴 CRITICAL-5 — Race Condition on Stock Deduction: Overselling Under Concurrent Load

**File:** `src/main/java/com/pharmacy/service/BillingService.java`

**Root Cause:**

The stock check and deduction in `BillingService.createOrder` is a classic **read-modify-write race**:

```java
// Thread A and Thread B both execute these lines at nearly the same time:
if (request.getQuantity() > medicine.getStockQuantity()) { // Both see stock = 5, quantity = 5 → PASS
    throw new IllegalArgumentException("Insufficient stock...");
}
medicine.setStockQuantity(medicine.getStockQuantity() - request.getQuantity()); // Both deduct 5 → stock = -5
```

The `@Transactional` annotation only prevents partial writes within one transaction; it does **not** prevent two concurrent transactions from reading the same stale stock value before either writes.

`BillingIntegrationTest.shouldHandleConcurrentOrdersWithConflict` explicitly tests this and the comment warns it is non-deterministic — confirming the bug is known but unfixed.

**Impact:**
- Pharmacy can dispense medicines that are not in stock.
- Stock can go negative.
- Physical inventory mismatches billing records.
- Liability and regulatory risk in a pharmacy context.

**Fix — add `@Version` to `Medicine` entity (Optimistic Locking):**
```java
// Medicine.java
@Version
private Long version;
```
JPA will automatically throw `OptimisticLockException` if two transactions try to update the same medicine row concurrently. Handle this in `BillingService` or `GlobalExceptionHandler` with a retry or 409 response.

---

### 🟠 HIGH-6 — H2 Dependency Missing: All Tests Fail to Start

**File:** `pom.xml`, `src/test/resources/application-test.properties`

**Root Cause:**

`application-test.properties` configures H2 in-memory database:
```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
```

But `pom.xml` has **no H2 dependency**. The test classpath has no H2 JDBC driver, so every `@SpringBootTest` test will throw `ClassNotFoundException: org.h2.Driver` at startup.

**Fix — add to `pom.xml`:**
```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```
Or migrate tests to Testcontainers with a real PostgreSQL container (recommended for production-representative tests).

---

### 🟠 HIGH-7 — `InsufficientStockException` Defined But Never Used

**Files:** `src/main/java/com/pharmacy/exception/InsufficientStockException.java`, `src/main/java/com/pharmacy/service/BillingService.java`

**Root Cause:**

A dedicated `InsufficientStockException` class exists but `BillingService` throws `IllegalArgumentException` instead. The handler for `IllegalArgumentException` maps it to HTTP 400 (Bad Request). A stock-out is semantically HTTP 409 (Conflict) or 422 (Unprocessable Entity) — not a bad request from the client.

```java
// Current (wrong):
throw new IllegalArgumentException("Insufficient stock for medicine: " + medicine.getName());

// Should be:
throw new InsufficientStockException("Insufficient stock for medicine: " + medicine.getName());
```

**Impact:**
- Wrong HTTP status code (400 vs 409).
- Frontend/clients cannot differentiate "you sent a malformed request" from "stock unavailable" — both are 400.
- `InsufficientStockException` is completely dead code, creating confusion.

---

### 🟠 HIGH-8 — `ddl-auto=update` in Production Profile

**File:** `src/main/resources/application.properties`

**Root Cause:**

```properties
spring.jpa.hibernate.ddl-auto=update
```

This is in the **production** properties file (not dev/test). `update` mode means Hibernate modifies the live database schema on every application startup. This can:
- Drop columns with data if an entity field is removed.
- Fail silently with partial migrations leaving the schema in an inconsistent state.
- Not roll back if the application crashes mid-startup.

**Fix:** Set `ddl-auto=validate` in production. Use **Flyway** or **Liquibase** to manage schema migrations with versioned, auditable migration scripts.

---

## 3. Missing Connections

### MC-1 — No Order Read API (Backend → Frontend Gap)

| | Detail |
|---|---|
| **Where missing** | `BillingController` — no GET endpoints |
| **Expected** | `GET /api/orders` returns order history; `GET /api/orders/{id}` returns single order |
| **Actual** | `404 Not Found` on any GET to `/api/orders/**` |
| **Impact** | Frontend billing/history screen has no data source |
| **Fix** | Add `getAllOrders()` and `getOrderById()` to `BillingController` + `BillingService` |

### MC-2 — `CustomerOrder` Has No FK to `User` Table

| | Detail |
|---|---|
| **Where missing** | `CustomerOrder.createdBy` is `String` — not a `@ManyToOne` FK |
| **Expected** | `orders.user_id REFERENCES users(id)` |
| **Actual** | `orders.created_by VARCHAR` — plain username string |
| **Impact** | No referential integrity; no "orders by user" join query possible efficiently; deleting a user leaves orphaned strings in orders |
| **Fix** | Add `@ManyToOne @JoinColumn(name = "user_id") private User user;` to `CustomerOrder`; migrate `createdBy` to FK |

### MC-3 — `UserResponse` DTO Defined But Never Connected

| | Detail |
|---|---|
| **Where missing** | `UserController` returns raw `User` entity; `UserResponse.java` is dead code |
| **Expected** | `UserController` returns `UserResponse` (no password field) |
| **Actual** | Raw `User` entity including `password` hash returned to clients |
| **Fix** | Wire `UserService` and `UserController` to use `UserResponse` DTO |

### MC-4 — `CUSTOMER` Role Defined but Has Zero Endpoints

| | Detail |
|---|---|
| **Where missing** | `User.Role.CUSTOMER` enum value exists; no controller, service path, or role check references it |
| **Expected** | Customers can place orders, view their history, browse medicines |
| **Actual** | Assigning a user `CUSTOMER` role effectively bans them from all endpoints |
| **Fix** | Either remove `CUSTOMER` from the enum and clean up, or implement customer-facing endpoints |

### MC-5 — README Documents 4 Endpoints That Do Not Exist

| Documented Endpoint | Actual Status |
|---|---|
| `GET /api/medicines/category/{category}` | ❌ 404 |
| `GET /api/medicines/expiring-soon` | ❌ 404 (only `/expired` exists) |
| `PATCH /api/medicines/{id}/stock` | ❌ 404 |
| `GET /api/orders` | ❌ 404 |

**Fix:** Either implement these endpoints or remove them from README to avoid developer confusion.

### MC-6 — Spring Boot Actuator Referenced in SecurityConfig but Not on Classpath

| | Detail |
|---|---|
| **Where missing** | `SecurityConfig.PUBLIC_URLS` includes `/actuator/health` |
| **Expected** | Health endpoint accessible without auth |
| **Actual** | `spring-boot-starter-actuator` is not in `pom.xml` — the endpoint does not exist |
| **Impact** | CI/CD health checks and Railway deployment readiness checks will fail |
| **Fix** | Add `spring-boot-starter-actuator` to `pom.xml` |

---

## 4. Backend Analysis

### 4.1 API Design Issues

| Issue | Location | Detail |
|---|---|---|
| No pagination on list endpoints | `MedicineController.getAllMedicines()`, `UserController.getAllUsers()` | Returns entire table. 10,000 medicines → OOM risk |
| `PATCH` misused for full role update | `UserController.updateRole()` | PATCH is correct here semantically, but the method replaces the entire role without validation of allowed transitions |
| `OrderItemRequest` validation fires inside `BillingService`, not in DTO | `BillingService.createOrder()` | Manual `if (qty <= 0)` checks in service — should be `@Min(1)` on the DTO with `@Valid` at controller |
| `@Valid` on list items requires special syntax | `BillingController.createOrder(@RequestBody List<@Valid OrderItemRequest>)` | Validation of elements inside a list requires `@Validated` at class level AND `@Valid` on method param; current form may silently skip validation |
| HTTP 200 for resource creation | `AuthController.register()`, `MedicineController.addMedicine()` | Should return `201 Created` with `Location` header |
| No `GET /api/users/me` | — | Users have no way to view their own profile without admin role |

### 4.2 Controller/Service Problems

**`BillingService` violates single responsibility:**
```
BillingService.createOrder()
  ├── validates input                    (should be DTO-level)
  ├── fetches medicines                  (ok)
  ├── deducts stock from Medicine entity (→ should be MedicineService)
  ├── builds OrderItem entities          (ok)
  └── calculates total                   (ok)
```
`BillingService` directly mutates `Medicine.stockQuantity` — this crosses bounded-context boundaries. Stock management belongs in `MedicineService.deductStock(id, qty)`.

**`UserService.getUserById` throws `IllegalArgumentException` for not found:**
```java
return userRepository.findById(id)
    .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
```
`ResourceNotFoundException` exists for this exact case. Using `IllegalArgumentException` maps to HTTP 400 (bad request) when HTTP 404 (not found) is correct. This is inconsistent with `MedicineService` which correctly uses `ResourceNotFoundException`.

**`AuthService.register` ignores the `role` field in `RegisterRequest`:**
```java
// RegisterRequest.role is @NotNull but always ignored:
.role(User.Role.PHARMACIST)  // hardcoded regardless of what was sent
```
The `@NotNull` constraint on `RegisterRequest.role` forces clients to send a role value that is then silently discarded. This is a misleading API contract.

**`AuthService.authenticate` has a logical dead branch:**
```java
authenticationManager.authenticate(...);  // throws BadCredentialsException if wrong
User user = userRepository.findByUsername(request.getUsername())
    .orElseThrow(() -> new RuntimeException("User not found after authentication"));
```
If `authenticate()` succeeds, the user MUST exist (it was loaded during auth). The `orElseThrow` is dead code — and worse, it throws a raw `RuntimeException` (HTTP 500) if it were ever hit.

### 4.3 Middleware / Auth Flaws

**`JwtAuthFilter` makes a DB call on every authenticated request:**
```java
UserDetails userDetails = userDetailsService.loadUserByUsername(username);
```
This is a `SELECT * FROM users WHERE username = ?` on **every single API call**. JWT is supposed to eliminate server-side session state. The role is already embedded in the token claim (`claims.put("role", ...)`). The DB call exists to check `isEnabled()` (the `active` flag) — a valid reason — but it should be noted that this defeats JWT's stateless performance advantage and creates a DB bottleneck at scale.

**No rate limiting on `/api/auth/login`:**
An attacker can attempt unlimited password guesses. BCrypt slows individual attempts but doesn't prevent parallel attacks. No IP-based throttling, no account lockout after N failures.

**JWT secret key padding without warning:**
```java
// JwtUtil.getSigningKey()
if (keyBytes.length < 32) {
    byte[] paddedKey = new byte[32];
    System.arraycopy(keyBytes, 0, paddedKey, 0, keyBytes.length);
    return Keys.hmacShaKeyFor(paddedKey);
}
```
A short secret is silently padded with zero bytes, making the effective key entropy much lower than the key length suggests. No log warning is emitted.

**No token revocation / blacklist:**
Compromised tokens are valid until expiry (`jwt.expiration=86400000` = 24 hours). If an admin deactivates a user or a token is stolen, the attacker has up to 24 hours of valid access. The `isEnabled()` check in `JwtAuthFilter` partially mitigates user deactivation — but only because of the DB call noted above.

---

## 5. Database Analysis

### 5.1 Schema Issues

**Entity Relationship (Current State):**
```sql
users          (id, username, password, full_name, email, role, created_at, updated_at, active)
medicines      (id, name, category, price, stock_quantity, expiry_date, manufacturer, created_at, updated_at)
orders         (id, order_date, total_amount, created_by)          -- created_by is VARCHAR, not FK
order_items    (id, order_id FK→orders, medicine_id FK→medicines, quantity, price)
```

**Missing FK: `orders.created_by` should be `orders.user_id FK→users`**
- No `ON DELETE` behavior defined for referential integrity.
- Deleting a user leaves orphaned `created_by` strings that are unmatchable.
- `CustomerOrderRepository` has no `findByCreatedBy(String)` method — order history is impossible without a custom query.

**`Medicine.updatedAt` has no `nullable = false`:**
```java
@Column(name = "updated_at")   // ← no nullable = false
private LocalDateTime updatedAt;
```
New medicines will have `NULL` in `updated_at` until first update. This causes `NullPointerException` risk in any code that reads this field without null-checking.

**`CustomerOrder` has no audit fields:**
`orders` table has `order_date` (set via `@PrePersist`) but no `updated_at`. If an order is ever modified (future status field), there is no tracking of when it changed.

**`Medicine` uses manual `@PrePersist`/`@PreUpdate` instead of Spring Data auditing:**
```java
// Medicine.java — manual audit lifecycle
@PrePersist
protected void onCreate() { createdAt = LocalDateTime.now(); }
@PreUpdate
protected void onUpdate() { updatedAt = LocalDateTime.now(); }
```
`User.java` uses `@EntityListeners(AuditingEntityListener.class)` with `@CreatedDate`/`@LastModifiedDate` annotations. These two approaches are inconsistent across the same project and creates maintainability confusion.

### 5.2 Query Inefficiencies

**N+1 Query Risk in `BillingService.toResponse()`:**
```java
order.getItems().stream()
    .map(item -> OrderItemResponse.builder()
        .medicineId(item.getMedicine().getId())         // ← triggers SELECT on Medicine (lazy)
        .medicineName(item.getMedicine().getName())     // ← same medicine, second access (cached by Hibernate)
        ...
```
`OrderItem.medicine` is `FetchType.LAZY`. When `toResponse()` accesses `item.getMedicine()` for each item, Hibernate fires a separate `SELECT medicine WHERE id = ?` per `OrderItem`. For an order with 10 items, that is 10 extra queries.

**Fix:** Use `@EntityGraph` or `JOIN FETCH` in the repository:
```java
@Query("SELECT o FROM CustomerOrder o JOIN FETCH o.items i JOIN FETCH i.medicine WHERE o.id = :id")
Optional<CustomerOrder> findByIdWithItems(@Param("id") Long id);
```

**`findByNameContainingIgnoreCase` on unindexed column:**
```java
// MedicineRepository
List<Medicine> findByNameContainingIgnoreCase(String name);
// Translates to: SELECT * FROM medicines WHERE LOWER(name) LIKE LOWER('%paracetamol%')
```
`LIKE '%..%'` (leading wildcard) cannot use a B-tree index. As the medicines table grows, this is a full table scan every time a pharmacist searches. With 10,000+ medicines, this will noticeably slow.

**Missing Indexes — all query predicates lack explicit indexes:**

| Column | Query Pattern | Risk |
|---|---|---|
| `medicines.name` | `LIKE %name%` | Full scan |
| `medicines.expiry_date` | `WHERE expiry_date < ?` | Full scan |
| `medicines.stock_quantity` | `WHERE stock_quantity < ?` | Full scan |
| `orders.created_by` | Future history queries | Full scan |

### 5.3 Data Integrity Problems

**Negative stock is possible (pre-fix):**
Without optimistic locking (`@Version`), concurrent transactions can deduct more than available stock, driving `stock_quantity` to negative values. No `CHECK (stock_quantity >= 0)` constraint exists at the DB level.

**Expired medicine can be ordered:**
`BillingService.createOrder` checks stock but never checks `medicine.getExpiryDate()`. A pharmacist can create an order for an expired medicine that was not manually deleted. A pharmacy dispensing expired medication is a patient safety issue.

**No `@UniqueConstraint` on `order_items` for duplicate medicine per order:**
The same `medicine_id` can appear multiple times in the same `order_id` in `order_items`. There is no DB or application-level prevention, leading to inconsistent data when a client sends duplicate medicine entries in one order.

**`Medicine.price` stored as `Double` (floating-point):**
```java
@Column(nullable = false)
private Double price;
```
Using `Double` for monetary values causes floating-point precision errors. `5.50 * 3` in Java double arithmetic = `16.499999999999996`. All billing calculations are inaccurate. **Use `BigDecimal` for all monetary fields.**

---

## 6. Frontend Integration Issues

> No frontend code exists in this repository. This section documents the integration surface that a frontend would need to consume, and the gaps that make reliable integration impossible.

### 6.1 API Not Connected / Wrongly Connected

**Error response format is unpredictable:**
Until CRITICAL-1 is fixed, every error returns `"Something went wrong"`. A frontend cannot display "Wrong password", "Medicine not found", or "Out of stock" — every error looks identical to the user.

**Validation errors use Spring Boot's default format:**
A `@Valid` failure on `POST /api/medicines` returns a completely different JSON structure than other errors:
```json
// Spring default format — different from GlobalExceptionHandler format:
{
  "timestamp": "...",
  "status": 400,
  "errors": [{ "field": "name", "defaultMessage": "must not be blank" }]
}
```

**User API leaks password hash:**
```json
{
  "id": 1,
  "username": "admin",
  "password": "$2a$10$abc...",   // ← BCRYPT HASH EXPOSED
  "role": "ADMIN",
  ...
}
```
Any frontend that renders user data would display (or silently cache) the BCrypt hash.

### 6.2 Static UI Problems

**No order listing endpoint:** A "Order History" screen cannot be built — there is no GET endpoint.  
**No user profile endpoint:** A "My Profile" screen cannot be built — there is no `GET /api/users/me`.  
**No `expiring-soon` endpoint:** Dashboard widget for "medicines expiring in 30 days" cannot be built.  
**No category filter endpoint:** A "Browse by category" feature cannot be built.

### 6.3 State / Async Issues

**No WebSocket / SSE for real-time updates:**
Low-stock alerts, expiry warnings, and new orders require polling the API since no real-time push mechanism exists. Polling `GET /api/medicines/low-stock` on every page load is wasteful.

**No pagination tokens/cursors:** `getAllMedicines()` returns everything. As data grows, initial page load time will degrade and a frontend paginated table has no way to request pages.

**JWT refresh not supported:** After 24 hours the token expires. There is no refresh endpoint, so the user is silently logged out. The frontend would need to catch 401 responses and redirect to login with no ability to silently refresh.

---

## 7. Performance Issues

### 7.1 Bottlenecks

**DB call on every authenticated request (JwtAuthFilter):**
```
Request → JwtAuthFilter → SELECT * FROM users WHERE username = ?  [every time]
```
At 1000 req/s, this is 1000 extra DB queries/second that bypass any connection pool efficiency. This will become the first performance bottleneck under load.

**Fix:** Cache `UserDetails` in an in-memory cache (Caffeine) with short TTL (30–60 seconds), keyed by username. Invalidate on user deactivation.

**`getAllMedicines()` loads the entire table:**
```java
return medicineRepository.findAll().stream()
    .map(this::toResponse)
    .collect(Collectors.toList());
```
100,000 medicines in a mature pharmacy → 100,000 objects in heap on every list request. No pagination, no projection, no limit.

**Fix:** Add `Pageable` parameter:
```java
Page<MedicineResponse> getAllMedicines(Pageable pageable);
```

**N+1 on order item medicine names:**
Covered in DB section. 10 items → 11 queries (1 for order + 10 for medicines). Use `JOIN FETCH`.

**`LIKE '%name%'` full table scan on medicine search:**
No index can help a leading-wildcard LIKE. Consider PostgreSQL `pg_trgm` extension + GIN index for true full-text search on `medicines.name`.

### 7.2 Optimization Suggestions

| Optimization | Expected Gain |
|---|---|
| Cache `UserDetails` in Caffeine (30s TTL) | Eliminates DB call on every request |
| Add `Pageable` to `getAllMedicines` and `getAllUsers` | Prevents OOM on large datasets |
| `JOIN FETCH` on order items | Reduces N+1 to 1 query |
| Index `medicines.expiry_date`, `medicines.stock_quantity` | Range query speedup |
| `pg_trgm` GIN index on `medicines.name` | Full-text search performance |
| Add `@Version` to `Medicine` | Prevents overselling race condition |
| Connection pool already at max=10 | Adequate for moderate load; tune with monitoring |

---

## 8. Security Issues

| # | Vulnerability | Severity | Location | Fix |
|---|---|---|---|---|
| S-1 | Hashed passwords exposed in user API responses | **CRITICAL** | `UserController` returns `User` entity | Use `UserResponse` DTO |
| S-2 | Default credentials hardcoded + logged at INFO | **HIGH** | `DataSeeder.java` logs `admin/admin123` | Log only to WARN, rotate immediately; force password change on first login |
| S-3 | JWT secret hardcoded in `application.properties` | **HIGH** | `application.properties` line: `jwt.secret=zcare_secure_prod_key_...` | Must be env var only: `${JWT_SECRET}` with no default |
| S-4 | No rate limiting on `/api/auth/login` | **HIGH** | `AuthController` / `SecurityConfig` | Add Bucket4j rate limiter or Spring Security's `httpBasic` throttle |
| S-5 | No token revocation / blacklist | **HIGH** | `JwtUtil`, `JwtAuthFilter` | Redis-based token denylist or short-lived access tokens + refresh tokens |
| S-6 | JWT key silently padded with zero bytes | **MEDIUM** | `JwtUtil.getSigningKey()` | Enforce minimum key length at startup; throw `IllegalStateException` if key is too short |
| S-7 | `ddl-auto=update` in production | **HIGH** | `application.properties` | Change to `validate`; use Flyway for migrations |
| S-8 | No HTTPS configuration | **HIGH** | `application.properties` | Configure TLS in Railway; add `server.ssl.*` or enforce HTTPS at load balancer |
| S-9 | Expired medicines can be dispensed | **HIGH** | `BillingService.createOrder()` | Add `if (medicine.getExpiryDate() != null && medicine.getExpiryDate().isBefore(LocalDate.now()))` check |
| S-10 | `BigDecimal` not used for money (floating-point precision) | **MEDIUM** | `Medicine.price`, `OrderItem.price`, `CustomerOrder.totalAmount` | Replace `Double`/`double` with `BigDecimal` for all monetary fields |
| S-11 | CORS `allowCredentials=true` with wildcard-origin approach | **MEDIUM** | `SecurityConfig.corsConfigurationSource()` | Ensure allowed origins list is strictly controlled via env var; validate on startup |
| S-12 | `application-dev.properties` `DB_PASSWORD` has fallback `password` | **MEDIUM** | `application-dev.properties` | Remove fallback default; fail fast if env var not set |
| S-13 | No `Content-Type` validation on POST endpoints | **LOW** | All controllers | Spring MVC handles this by default, but explicitly add `consumes = MediaType.APPLICATION_JSON_VALUE` |

---

## 9. Confusing Areas

### C-1 — `BillingService` vs `MedicineService` Ownership of Stock

Stock deduction (`medicine.setStockQuantity(...)`) happens inside `BillingService`. Any developer reading `MedicineService` would not know that stock can be modified from another service. This creates hidden coupling and makes it impossible to add stock validation logic in one place.

**Suggested fix:** Extract `MedicineService.deductStock(Long medicineId, int quantity)` as a transactional method, called from `BillingService`.

### C-2 — `RegisterRequest.role` Field Is `@NotNull` but Always Ignored

```java
// RegisterRequest.java
@NotNull
private User.Role role;   // ← API clients must send this

// AuthService.java
.role(User.Role.PHARMACIST)  // ← but it's always hardcoded to PHARMACIST
```

A client sees `role` is required in the API spec (Swagger will show it as required), sends `ADMIN`, registration succeeds, but they are silently assigned `PHARMACIST`. This is a contract lie that will frustrate developers integrating with the API.

**Fix:** Either remove `role` from `RegisterRequest` (admin assigns roles separately) or honour the field (and protect the endpoint accordingly).

### C-3 — Package Name vs Project Name vs GroupId Mismatch

```
groupId:    com.zcare          (pom.xml)
artifactId: zcare-backend      (pom.xml)
base pkg:   com.pharmacy       (all Java files)
app name:   pharmacy-backend   (application.properties)
db name:    zcare_db           (application.properties)
```

Four different naming conventions across five config files. A new developer joining the project will be confused about whether this is "ZCare" or "Pharmacy" and which package/namespace to use for new code.

### C-4 — `UserService.getUserById` Throws Wrong Exception

```java
.orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
// Compared to MedicineService which correctly uses:
.orElseThrow(() -> new ResourceNotFoundException("Medicine not found with id: " + id));
```

Same logical operation, different exception, different HTTP status (400 vs 404). A developer debugging would need to check which exception a particular service throws to know what HTTP status to expect.

### C-5 — `CustomerOrder` Entity Called `CustomerOrder` but Table Is `orders`

The class is `CustomerOrder`, mapped to table `orders`. There are no customers as a concept anywhere in the system. The `CUSTOMER` role exists but has no endpoints. The naming is leftover planning artifact creating cognitive noise.

### C-6 — `OrderItemRepository` Is Injected but Never Used

```java
// BillingService
private final OrderItemRepository orderItemRepository;
```
`orderItemRepository` is injected but never called. All `OrderItem` persistence happens via `CascadeType.ALL` on the parent `CustomerOrder`. This is dead code creating confusion about whether items should be saved separately.

### C-7 — README Documents Wrong Endpoints

The README contains a detailed API reference showing endpoints that return 404 in the actual running application. A developer reading the README and trying those endpoints would waste time debugging a "connection issue" that is actually just missing implementation.

---

## 10. Production Readiness Score

### Score: **3.5 / 10**

| Category | Score | Justification |
|---|---|---|
| **Auth & Security** | 4/10 | JWT works, BCrypt used, roles enforced — but no rate limiting, no token revocation, password leaked in responses, secrets in config |
| **API Completeness** | 3/10 | CRUD for medicines works; orders are write-only; 4 documented endpoints missing; pagination absent; validation errors broken |
| **Data Integrity** | 3/10 | Basic constraints present; no FK on orders→users; race condition on stock; Double for money; expired medicines orderable |
| **Error Handling** | 2/10 | GlobalExceptionHandler structured correctly but CRITICAL-1 bug means every error returns "Something went wrong" |
| **Testing** | 4/10 | Integration test structure is good; tests fail to run (missing H2 dep); no unit tests; concurrent order test is non-deterministic |
| **Performance** | 3/10 | DB call on every request; no pagination; N+1 on orders; no caching — acceptable at dev scale, dangerous at production scale |
| **DevOps / CI-CD** | 5/10 | CI pipeline exists, deploys to Railway; no Docker; no health check; no staging env; `ddl-auto=update` in prod |
| **Documentation** | 4/10 | README and Swagger exist; README has stale/wrong endpoints; Swagger description references unimplemented features |
| **Code Quality** | 5/10 | Clean layering, consistent style, good use of Lombok/Spring conventions; naming inconsistencies; dead code; wrong exception types |
| **Frontend Readiness** | 2/10 | Error messages useless; no order read API; no pagination; password leaked; 4 endpoints missing |

**Why not higher than 3.5:**
Three CRITICAL bugs exist that would cause immediate, visible failures in any real usage:
1. All error messages say "Something went wrong" — unusable frontend UX.
2. BCrypt hashes returned in user API responses — data breach on first admin operation.
3. All tests fail to start due to missing H2 dependency.

**What would push to 7+:**
Fix CRITICAL-1 through CRITICAL-5, add H2 dep, use `UserResponse` DTO, add GET order endpoints, fix `ddl-auto=update`, add pagination, and use `BigDecimal` for money.

---

## 11. Action Plan

### 🔴 Phase 1 — Critical Fixes (Do Before Anything Else)

These must be fixed before the application is shown to any real user or connected to any frontend.

1. **Fix `GlobalExceptionHandler.createResponse` bug** — change `body.put("message", "Something went wrong")` to `body.put("message", message)`. *(30 min)*

2. **Fix `UserController` password leak** — change all methods to return `UserResponse` DTO (already defined). *(1 hour)*

3. **Add H2 test dependency to `pom.xml`** — add `<dependency>com.h2database:h2:test</dependency>`. Run all tests to confirm they pass. *(15 min)*

4. **Add `MethodArgumentNotValidException` handler** to `GlobalExceptionHandler` returning field-level errors. *(1 hour)*

5. **Add GET endpoints to `BillingController`** — `GET /api/orders` and `GET /api/orders/{id}`, wired to `BillingService.getAllOrders()` and `getOrderById()`. *(2 hours)*

6. **Fix `InsufficientStockException`** — replace `IllegalArgumentException` in `BillingService` with the existing `InsufficientStockException`; add handler in `GlobalExceptionHandler` mapping to HTTP 409. *(30 min)*

### 🟠 Phase 2 — Security Hardening (Before Any External Access)

7. **Externalise JWT secret** — remove default value from all properties files. Use `${JWT_SECRET}` with validation that it is set and meets minimum length. *(30 min)*

8. **Remove credential logging from `DataSeeder`** — downgrade to WARN level and mask passwords, or remove entirely after first deploy. *(15 min)*

9. **Add expired medicine guard in `BillingService.createOrder()`** — check `expiryDate` before allowing an order item. Return HTTP 409 with specific message. *(30 min)*

10. **Change `ddl-auto` to `validate`** and introduce Flyway for schema management. *(4 hours — includes writing initial migration script)*

11. **Add rate limiting on `/api/auth/login`** — Bucket4j + in-memory or Redis bucket. Limit to 5 attempts per minute per IP. *(2 hours)*

### 🟡 Phase 3 — Data Model Fixes

12. **Replace `Double` with `BigDecimal`** for `Medicine.price`, `OrderItem.price`, `CustomerOrder.totalAmount`. Update all service arithmetic. *(3 hours)*

13. **Add `@Version` to `Medicine`** for optimistic locking. Update `BillingService` to catch `OptimisticLockException` and return HTTP 409. *(1 hour)*

14. **Add FK from `CustomerOrder` to `User`** — replace `String createdBy` with `@ManyToOne User user`. Write Flyway migration. *(2 hours)*

15. **Add DB-level constraint `CHECK (stock_quantity >= 0)`** via Flyway migration. *(15 min)*

16. **Add missing DB indexes** — `medicines.expiry_date`, `medicines.stock_quantity`, `medicines.name` (GIN trgm). *(1 hour)*

17. **Standardise entity auditing** — replace `Medicine`'s manual `@PrePersist`/`@PreUpdate` with `@EntityListeners(AuditingEntityListener.class)`. *(30 min)*

### 🟢 Phase 4 — Feature Completeness

18. **Add pagination** to `getAllMedicines()` and `getAllUsers()` using `Pageable`. *(2 hours)*

19. **Implement missing endpoints:** `GET /api/medicines/expiring-soon?days=30`, `GET /api/medicines/category/{cat}`, `PATCH /api/medicines/{id}/stock`. *(3 hours)*

20. **Add `GET /api/users/me`** — returns `UserResponse` for the authenticated user. *(1 hour)*

21. **Fix `RegisterRequest.role`** — either remove the field (admin assigns roles via `PATCH /{id}/role`) or honour it with access control. *(30 min)*

22. **Fix `UserService.getUserById`** to throw `ResourceNotFoundException` instead of `IllegalArgumentException`. *(10 min)*

23. **Remove `OrderItemRepository` injection** from `BillingService` (unused dead code). *(5 min)*

24. **Extract stock deduction into `MedicineService`** — decouple `BillingService` from direct `Medicine` mutation. *(1 hour)*

### ⚙️ Phase 5 — DevOps & Observability

25. **Add Spring Boot Actuator** to `pom.xml` with secured management endpoints. *(30 min)*

26. **Add `Dockerfile`** (multi-stage Maven build → JRE runtime image) and `docker-compose.yml` for local dev. *(2 hours)*

27. **Add cache for `UserDetails`** (Caffeine, 30s TTL) to eliminate per-request DB lookup in `JwtAuthFilter`. *(2 hours)*

28. **Add structured logging** with MDC correlation IDs (request ID propagation through all log statements). *(2 hours)*

29. **Add JWT refresh token endpoint** and 24-hour access token expiry with 7-day refresh token. *(4 hours)*

30. **Expand CI/CD pipeline** — add Docker build + push, add staging environment, add post-deploy smoke test hitting `/actuator/health`. *(4 hours)*

---

### Summary Table

| Priority | Count | Estimated Effort |
|---|---|---|
| 🔴 Critical (Phase 1) | 6 items | ~6 hours |
| 🟠 Security (Phase 2) | 5 items | ~7 hours |
| 🟡 Data Model (Phase 3) | 6 items | ~8 hours |
| 🟢 Features (Phase 4) | 7 items | ~9 hours |
| ⚙️ DevOps (Phase 5) | 6 items | ~15 hours |
| **Total** | **30 items** | **~45 hours** |

---

*This audit was generated by static code analysis of all 37 source files, 4 property files, 4 test files, the CI/CD pipeline, and `pom.xml`. No dynamic or runtime analysis was performed.*
