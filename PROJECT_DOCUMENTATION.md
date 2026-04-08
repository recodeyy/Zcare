# 💊 Pharmacy Management System - Backend Documentation

**Project Repository:** Zcare  
**Owner:** kruturaj-20  
**Current Branch:** master  
**Date Generated:** March 29, 2026

---

## 📖 Table of Contents

1. [Project Overview](#project-overview)
2. [Technology Stack](#technology-stack)
3. [Project Structure](#project-structure)
4. [Architecture & Design](#architecture--design)
5. [Getting Started](#getting-started)
6. [Configuration](#configuration)
7. [Database Models](#database-models)
8. [API Endpoints](#api-endpoints)
9. [Authentication & Security](#authentication--security)
10. [Key Components](#key-components)
11. [Development Guide](#development-guide)

---

## 🎯 Project Overview

**Zcare Pharmacy Management System** is a Spring Boot 3 REST API backend for managing pharmacy inventory, user authentication, and medicine management. The system provides:

- 🔐 JWT-based authentication and authorization
- 📦 Complete medicine inventory management
- 👥 Role-based access control (ADMIN, PHARMACIST)
- 📊 Real-time inventory tracking
- ⏰ Expiry date management
- 🔍 Advanced search and filtering capabilities
- 📚 Swagger/OpenAPI documentation
- 🗄️ PostgreSQL database integration

---

## 🛠️ Technology Stack

### Core Framework
- **Spring Boot 3.2.0** - Framework
- **Spring Security** - Authentication & Authorization
- **Spring Data JPA** - ORM & Database access
- **Java 17** - Programming language

### Database
- **PostgreSQL** - Primary database
- **Hibernate** - ORM framework with automatic schema management

### Authentication & Security
- **JWT (JJWT 0.12.3)** - Token-based authentication
- **BCrypt** - Password encryption
- **Spring Security Filters** - Custom JWT validation

### API Documentation
- **Springdoc OpenAPI 2.3.0** - Swagger/OpenAPI 3 integration
- **Swagger UI** - Interactive API documentation

### Utilities & Tools
- **Lombok** - Reduce boilerplate code
- **Jakarta Validation** - Request validation
- **Maven 3.8+** - Build tool

### Testing
- **Spring Boot Test** - Unit testing
- **Spring Security Test** - Security testing

---

## 📁 Project Structure

```
pharmacy-backend/
├── pom.xml                              # Maven configuration
├── README.md                            # Quick start guide
├── PROJECT_DOCUMENTATION.md             # This file
│
├── src/
│   ├── main/
│   │   ├── java/com/pharmacy/
│   │   │   ├── PharmacyApplication.java     # Spring Boot entry point
│   │   │   │
│   │   │   ├── config/                      # Configuration classes
│   │   │   │   ├── DataSeeder.java          # Initialize default users & data
│   │   │   │   ├── SecurityConfig.java      # Spring Security setup
│   │   │   │   └── SwaggerConfig.java       # OpenAPI/Swagger configuration
│   │   │   │
│   │   │   ├── controller/                  # REST endpoints
│   │   │   │   ├── AuthController.java      # Login & Register endpoints
│   │   │   │   └── MedicineController.java  # Medicine CRUD endpoints
│   │   │   │
│   │   │   ├── service/                     # Business logic
│   │   │   │   ├── AuthService.java         # Authentication logic
│   │   │   │   ├── MedicineService.java     # Medicine business logic
│   │   │   │   └── UserDetailsServiceImpl.java  # Spring Security integration
│   │   │   │
│   │   │   ├── repository/                  # Database access
│   │   │   │   ├── UserRepository.java      # User queries
│   │   │   │   └── MedicineRepository.java  # Medicine queries
│   │   │   │
│   │   │   ├── model/                       # JPA entities
│   │   │   │   ├── User.java                # User entity with UserDetails
│   │   │   │   └── Medicine.java            # Medicine/Drug inventory entity
│   │   │   │
│   │   │   ├── dto/                         # Data Transfer Objects
│   │   │   │   └── AuthDtos.java            # Auth request/response DTOs
│   │   │   │
│   │   │   ├── security/                    # Security components
│   │   │   │   ├── JwtUtil.java             # JWT token generation & validation
│   │   │   │   └── JwtAuthFilter.java       # Custom authentication filter
│   │   │   │
│   │   │   └── exception/                   # Exception handling
│   │   │       └── GlobalExceptionHandler.java  # Centralized error handling
│   │   │
│   │   └── resources/
│   │       └── application.properties       # Application configuration
│   │
│   └── test/
│       └── java/com/pharmacy/              # Unit & integration tests
│
├── target/                               # Compiled output (auto-generated)
│   └── pharmacy-backend-1.0.0.jar.original
│
└── mvn.properties (if exists)            # Maven properties
```

---

## 🏗️ Architecture & Design

### Layered Architecture

```
┌─────────────────────────────────────┐
│   REST Controller Layer             │
│  (AuthController, MedicineController)
├─────────────────────────────────────┤
│   Service Layer                     │
│  (Business Logic & Validation)      │
├─────────────────────────────────────┤
│   Repository Layer                  │
│  (Data Access & Queries)            │
├─────────────────────────────────────┤
│   Database Layer                    │
│  (PostgreSQL with Hibernate/JPA)    │
└─────────────────────────────────────┘
        Security Layer (JWT Filter)
     (Intercepts all requests)
```

### Key Design Patterns

| Pattern | Implementation | Purpose |
|---------|---|---|
| **Repository Pattern** | `UserRepository`, `MedicineRepository` | Abstraction over database access |
| **Service Pattern** | `AuthService`, `MedicineService` | Business logic encapsulation |
| **DTO Pattern** | `AuthDtos` | Clean data transfer between layers |
| **Filter Pattern** | `JwtAuthFilter` | Request interception for auth |
| **Builder Pattern** | Lombok `@Builder` on entities | Fluent object construction |
| **Dependency Injection** | Spring `@RequiredArgsConstructor` | Loose coupling |

---

## 🚀 Getting Started

### Prerequisites

- **Java 17 or higher** - Check: `java -version`
- **Maven 3.8+** - Check: `mvn -version`
- **PostgreSQL 12+** (or Supabase free account)
- **Git** - For cloning the repository

### Installation Steps

#### 1. Clone Repository
```bash
git clone https://github.com/kruturaj-20/Zcare.git
cd pharmacy-backend
```

#### 2. Configure PostgreSQL

**Option A: Local PostgreSQL**
```sql
CREATE DATABASE pharmacy_db;
CREATE USER pharmacy_user WITH PASSWORD 'yourpassword';
GRANT ALL PRIVILEGES ON DATABASE pharmacy_db TO pharmacy_user;
```

**Option B: Supabase Cloud (Recommended)**
1. Visit https://supabase.com and create a free account
2. Create a new project
3. Go to **Settings → Database → Connection String**
4. Copy the PostgreSQL connection string

#### 3. Update Configuration

Edit `src/main/resources/application.properties`:

```properties
# Database Connection
spring.datasource.url=jdbc:postgresql://YOUR_HOST:5432/YOUR_DATABASE
spring.datasource.username=postgres
spring.datasource.password=YOUR_PASSWORD

# JWT Secret (CHANGE IN PRODUCTION!)
jwt.secret=your_long_random_secret_key_minimum_32_characters_required
jwt.expiration=86400000  # 24 hours in milliseconds

# CORS (Update if frontend is on different URL)
cors.allowed-origins=http://localhost:3000,http://localhost:5173
```

#### 4. Build & Run

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run

# OR run the JAR directly
java -jar target/pharmacy-backend-1.0.0.jar
```

#### 5. Verify Installation

```bash
# Check health endpoint
curl http://localhost:8080/swagger-ui.html

# Should see Swagger UI in browser
# Default port: 8080
```

---

## ⚙️ Configuration

### Application Properties

| Property | Default | Description |
|----------|---------|---|
| `spring.application.name` | pharmacy-backend | App name |
| `server.port` | 8080 | Server port |
| `spring.jpa.hibernate.ddl-auto` | update | Schema auto-creation (`update`, `create`, `validate`) |
| `spring.jpa.show-sql` | true | Log SQL queries |
| `jwt.secret` | *[configured]* | Secret key for JWT signing (min 32 chars) |
| `jwt.expiration` | 86400000 | Token expiration in ms (24h default) |
| `cors.allowed-origins` | *[configured]* | Comma-separated CORS origins |

### Default Users (Created on First Run)

| User | Username | Password | Role |
|------|----------|----------|------|
| Admin | `admin` | `admin123` | ADMIN |
| Pharmacist | `pharmacist` | `pharma123` | PHARMACIST |

⚠️ **IMPORTANT:** Change these credentials immediately in production!

---

## 🗄️ Database Models

### User Entity

```java
@Entity
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false)
    private String fullName;
    
    @Column(unique = true)
    private String email;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;  // ADMIN or PHARMACIST
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    private boolean active = true;
}
```

**Roles:**
- **ADMIN** - Full system access, can delete medicines
- **PHARMACIST** - Standard access, manage inventory

### Medicine Entity

```java
@Entity
@Table(name = "medicines")
public class Medicine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    private String genericName;
    
    @Column(nullable = false)
    private String category;
    
    private String manufacturer;
    
    @Column(nullable = false)
    private String batchNumber;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(nullable = false)
    private Integer stockQuantity;
    
    @Column(nullable = false)
    private LocalDate expiryDate;
    
    private String description;
    
    private String unit;  // tablet, ml, capsule, etc.
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
```

---

## 🔌 API Endpoints

### Authentication Endpoints (Public - No token required)

#### Register New User
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "newuser",
  "password": "password123",
  "fullName": "John Doe",
  "email": "john@example.com",
  "role": "PHARMACIST"
}

Response (200 OK):
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "newuser",
  "fullName": "John Doe",
  "role": "PHARMACIST",
  "message": "Registration successful"
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}

Response (200 OK):
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "admin",
  "fullName": "Admin User",
  "role": "ADMIN",
  "message": "Login successful"
}
```

### Medicine Endpoints (Protected - Requires JWT token)

#### Get All Medicines
```http
GET /api/medicines
Authorization: Bearer {token}

Response (200 OK):
[
  {
    "id": 1,
    "name": "Paracetamol",
    "genericName": "Acetaminophen",
    "category": "Analgesic",
    "price": 50.00,
    "stockQuantity": 100,
    "expiryDate": "2026-12-31",
    "batchNumber": "BATCH001",
    "manufacturer": "Pharma Co",
    "unit": "tablet"
  },
  ...
]
```

#### Get Medicine by ID
```http
GET /api/medicines/{id}
Authorization: Bearer {token}

Response (200 OK):
{
  "id": 1,
  "name": "Paracetamol",
  "genericName": "Acetaminophen",
  ...
}
```

#### Create New Medicine
```http
POST /api/medicines
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "Aspirin",
  "genericName": "Acetylsalicylic Acid",
  "category": "Analgesic",
  "manufacturer": "Pharma Co",
  "batchNumber": "BATCH002",
  "price": 75.00,
  "stockQuantity": 200,
  "expiryDate": "2027-06-30",
  "unit": "tablet"
}

Response (201 Created):
{
  "id": 2,
  "name": "Aspirin",
  ...
}
```

#### Update Medicine
```http
PUT /api/medicines/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "Aspirin 500mg",
  "price": 80.00,
  "stockQuantity": 180,
  ...
}

Response (200 OK):
{
  "id": 2,
  ...
}
```

#### Delete Medicine (ADMIN only)
```http
DELETE /api/medicines/{id}
Authorization: Bearer {token}

Response (204 No Content)
```

#### Search Medicine by Name
```http
GET /api/medicines/search?name=paracetamol
Authorization: Bearer {token}

Response (200 OK):
[
  {...}
]
```

#### Filter by Category
```http
GET /api/medicines/category/{category}
Authorization: Bearer {token}

Response (200 OK):
[
  {...}
]
```

#### Get Expiring Soon (within 30 days)
```http
GET /api/medicines/expiring-soon
Authorization: Bearer {token}

Response (200 OK):
[
  {
    "id": 3,
    "name": "Cough Syrup",
    "expiryDate": "2026-04-15",
    ...
  }
]
```

#### Get Low Stock Items
```http
GET /api/medicines/low-stock
Authorization: Bearer {token}

Response (200 OK):
[
  {
    "id": 4,
    "name": "Vitamin C",
    "stockQuantity": 5,
    ...
  }
]
```

#### Adjust Stock Quantity
```http
PATCH /api/medicines/{id}/stock
Authorization: Bearer {token}
Content-Type: application/json

{
  "quantity": 50  // Sets new quantity
}

Response (200 OK):
{
  "id": 1,
  "stockQuantity": 50,
  ...
}
```

---

## 🔐 Authentication & Security

### JWT Flow

```
1. User logs in with username & password
        ↓
2. AuthService validates credentials
        ↓
3. JwtUtil generates JWT token
        ↓
4. Token returned to client
        ↓
5. Client sends token in Authorization header: Bearer {token}
        ↓
6. JwtAuthFilter intercepts request
        ↓
7. Token validated & user extracted
        ↓
8. Request processed with authentication context
```

### How to Use JWT Token

#### 1. Get Token via Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'

# Response:
# {
#   "token": "eyJhbGciOiJIUzI1NiJ9...",
#   ...
# }
```

#### 2. Use Token in Requests
```bash
curl -X GET http://localhost:8080/api/medicines \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

### Security Configuration

- **CSRF Protection:** Disabled (stateless API)
- **Session Management:** Stateless (JWT only)
- **Cors:** Configured for specific origins
- **Password Encoding:** BCrypt with strength 10
- **HTTP Only:** Recommended for production
- **HTTPS:** Required for production

### Protected Endpoints

All `/api/medicines/**` endpoints require:
1. Valid JWT token in `Authorization` header
2. Token must not be expired
3. User must be authenticated

### Admin-Only Operations

- **DELETE /api/medicines/{id}** - Only ADMIN role

---

## 🧩 Key Components

### 1. AuthController
**Location:** `controller/AuthController.java`

Handles user authentication:
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login and get JWT

### 2. MedicineController
**Location:** `controller/MedicineController.java`

Manages medicine inventory operations with full CRUD, search, and filtering.

### 3. AuthService
**Location:** `service/AuthService.java`

Business logic for:
- User registration with duplicate checking
- User login with password validation
- JWT token generation

### 4. MedicineService
**Location:** `service/MedicineService.java`

Business logic for:
- Medicine CRUD operations
- Search and filter operations
- Stock management
- Expiry tracking

### 5. JwtUtil
**Location:** `security/JwtUtil.java`

JWT operations:
- Token generation with claims
- Token validation
- Username extraction
- Expiration checking

### 6. JwtAuthFilter
**Location:** `security/JwtAuthFilter.java`

Intercepts all requests to:
- Extract JWT from Authorization header
- Validate token
- Set Spring Security context
- Pass through if token valid

### 7. SecurityConfig
**Location:** `config/SecurityConfig.java`

Configures:
- Authentication provider (DAO-based)
- Password encoder (BCrypt)
- Security filter chain
- CORS configuration
- Public endpoints
- JWT filter registration

### 8. DataSeeder
**Location:** `config/DataSeeder.java`

Initializes default data on startup:
- Creates default ADMIN user (admin/admin123)
- Creates default PHARMACIST (pharmacist/pharma123)
- Pre-loads sample medicines (optional)

### 9. GlobalExceptionHandler
**Location:** `exception/GlobalExceptionHandler.java`

Centralized error handling:
- Validation errors
- Authentication errors
- Resource not found
- Database exceptions
- Custom business exceptions

### 10. User Repository
**Location:** `repository/UserRepository.java`

Custom queries:
- `findByUsername(String username)`
- `existsByUsername(String username)`
- `existsByEmail(String email)`

### 11. Medicine Repository
**Location:** `repository/MedicineRepository.java`

Custom queries for:
- Search by name
- Filter by category
- Find expiring medicines
- Find low stock items

---

## 👨‍💻 Development Guide

### Code Organization Principles

1. **Separation of Concerns** - Each layer has single responsibility
2. **DRY (Don't Repeat Yourself)** - Reusable components
3. **SOLID Principles** - Clean, maintainable code
4. **Error Handling** - Global exception handler
5. **Validation** - Input validation in DTOs

### Adding a New Feature

#### Example: Add Medicine Category Endpoint

**Step 1:** Create Entity (if needed)
```java
@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;
}
```

**Step 2:** Create Repository
```java
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);
}
```

**Step 3:** Create Service
```java
@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    
    public Category getCategory(Long id) {
        return categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }
}
```

**Step 4:** Create Controller
```java
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;
    
    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategory(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategory(id));
    }
}
```

**Step 5:** Update SecurityConfig (if public)
```java
@Configuration
public class SecurityConfig {
    private static final String[] PUBLIC_URLS = {
        "/api/auth/**",
        "/api/categories/**",  // Add here
        // ...
    };
}
```

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=AuthServiceTest

# Run with coverage
mvn jacoco:report
```

### Building for Production

```bash
# Clean build
mvn clean install

# Build JAR
mvn package

# Run JAR
java -jar target/pharmacy-backend-1.0.0.jar

# With external configuration
java -jar target/pharmacy-backend-1.0.0.jar \
  --spring.datasource.url=jdbc:postgresql://prod-host:5432/pharmacy_db \
  --spring.datasource.username=prod_user \
  --spring.datasource.password=prod_password \
  --jwt.secret=your_production_secret_key
```

### Troubleshooting

| Issue | Cause | Solution |
|-------|-------|----------|
| Port 8080 already in use | Another service running | Change `server.port` in `application.properties` |
| Database connection failed | Wrong credentials/host | Verify `spring.datasource.*` properties |
| JWT token invalid | Secret key mismatch | Ensure same `jwt.secret` on token generation & validation |
| CORS error | Origin not allowed | Update `cors.allowed-origins` in properties |
| Swagger not loading | Missing springdoc dependency | Check `pom.xml` has springdoc-openapi dependency |

---

## 📊 API Documentation

### Access Swagger UI

```
http://localhost:8080/swagger-ui.html
```

Features:
- Interactive API exploration
- Try-out endpoints directly
- View request/response schemas
- Authorization token input

### OpenAPI JSON

```
http://localhost:8080/api-docs
```

Raw OpenAPI 3.0 specification suitable for code generation.

---

## 🔄 Development Workflow

### Local Development

```bash
# 1. Start PostgreSQL
# Ensure PostgreSQL is running

# 2. Clone repository
git clone https://github.com/kruturaj-20/Zcare.git
cd pharmacy-backend

# 3. Create local database
createdb pharmacy_db

# 4. Update application.properties
# Set your local DB credentials

# 5. Run application
mvn spring-boot:run

# 6. Access Swagger
# Navigate to http://localhost:8080/swagger-ui.html
```

### Development Tips

1. **Enable Hot Reload**
   ```bash
   mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dfile.encoding=UTF-8"
   ```

2. **Debug Mode**
   ```bash
   mvn spring-boot:run -Drun.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"
   ```

3. **View SQL Queries**
   - Set `spring.jpa.show-sql=true` in properties
   - Check console for generated SQL

4. **Reload Configuration Changes**
   - Changes in `application.properties` require restart
   - Use Spring DevTools for faster reload (`spring-boot-devtools` dependency)

---

## 📝 Notes & Conventions

### Naming Conventions

| Item | Convention | Example |
|------|-----------|---------|
| Package | Lowercase, domain-based | `com.pharmacy.service` |
| Class | PascalCase | `AuthService`, `User` |
| Method | camelCase | `getUserById()`, `saveUser()` |
| Variable | camelCase | `userName`, `isActive` |
| Constant | UPPER_SNAKE_CASE | `MAX_SIZE`, `DEFAULT_ROLE` |
| Endpoint | lowercase, kebab-case | `/api/medicines`, `/api/auth/login` |

### Code Quality

- Use Lombok annotations to reduce boilerplate
- Follow Spring framework conventions
- Keep methods focused (single responsibility)
- Add Javadoc for complex methods
- Write meaningful commit messages

### Common Issues & Solutions

See "Troubleshooting" section above.

---

## 📚 References & Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security Guide](https://spring.io/projects/spring-security)
- [JWT.io](https://jwt.io) - JWT documentation
- [Jakarta Persistence](https://jakarta.ee/specifications/persistence/)
- [Swagger/OpenAPI](https://swagger.io/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)

---

## 📝 License

Please check the project repository for license information.

---

## 👥 Contributors

- Project Owner: kruturaj-20
- Repository: Zcare
- Branch: master

---

**Last Updated:** March 29, 2026  
**Documentation Version:** 1.0.0  
**Spring Boot Version:** 3.2.0  
**Java Version:** 17+
