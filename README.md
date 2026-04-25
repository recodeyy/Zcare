# 💊 Pharmacy Management System — Backend

Spring Boot 3 REST API with JWT Authentication, PostgreSQL, and Swagger UI.

---

## 🚀 Quick Start (Run Locally in 5 Minutes)

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL (or use Supabase free tier)

### Step 1 — Clone the repo
```bash
git clone https://github.com/YOUR_USERNAME/pharmacy-backend.git
cd pharmacy-backend
```

### Step 2 — Set up PostgreSQL
**Option A: Local PostgreSQL**
```sql
CREATE DATABASE pharmacy_db;
CREATE USER pharmacy_user WITH PASSWORD 'yourpassword';
GRANT ALL PRIVILEGES ON DATABASE pharmacy_db TO pharmacy_user;
```

**Option B: Supabase (Recommended — Free & Cloud)**
1. Go to https://supabase.com and create a free account
2. Create a new project
3. Go to Settings → Database → Connection String
4. Copy the connection string

### Step 3 — Configure application.properties
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://YOUR_HOST:5432/YOUR_DB
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD
jwt.secret=your_long_random_secret_key_here_min_32_chars
```

### Step 4 — Run the app
```bash
mvn spring-boot:run
```

### Step 5 — Open Swagger UI
```
http://localhost:8080/swagger-ui.html
```

---

## 🔐 Default Login Credentials (Created on First Run)

| User        | Username      | Password     | Role       |
|-------------|---------------|--------------|------------|
| Admin       | `admin`       | `admin123`   | ADMIN      |
| Pharmacist  | `pharmacist`  | `pharma123`  | PHARMACIST |

> ⚠️ **Change these passwords immediately in production!**

---

## 📋 API Endpoints

### Authentication (Public — No token needed)
| Method | Endpoint              | Description          |
|--------|-----------------------|----------------------|
| POST   | `/api/auth/register`  | Register new user    |
| POST   | `/api/auth/login`     | Login & get JWT token|

### Medicine Inventory (Requires JWT token)
| Method | Endpoint                        | Description              |
|--------|---------------------------------|--------------------------|
| GET    | `/api/medicines`                | Get all medicines        |
| GET    | `/api/medicines/page`           | Get paginated medicines  |
| GET    | `/api/medicines/{id}`           | Get medicine by ID       |
| GET    | `/api/medicines/barcode/{barcode}` | Get medicine by barcode |
| POST   | `/api/medicines`                | Add new medicine         |
| PUT    | `/api/medicines/{id}`           | Update medicine          |
| DELETE | `/api/medicines/{id}`           | Deactivate medicine (soft delete) |
| PATCH  | `/api/medicines/{id}/restore`   | Restore deactivated medicine |
| GET    | `/api/medicines/inactive`       | List deactivated medicines |
| GET    | `/api/medicines/search?name=X`  | Search by name           |
| GET    | `/api/medicines/category/{cat}` | Filter by category       |
| GET    | `/api/medicines/batch/{batchNumber}` | Lookup by batch number |
| GET    | `/api/medicines/expiring-soon`  | Expiring within 30 days  |
| GET    | `/api/medicines/low-stock`      | Stock below threshold    |
| PATCH  | `/api/medicines/{id}/stock`     | Adjust stock quantity    |

Medicine records use `imageUrl` for image references. Binary upload endpoints are not part of the current API, and `price`/`sellingPrice` are mirrored during the transition.

### Billing & Stock Audit (Requires JWT token)
| Method | Endpoint                               | Description                      |
|--------|----------------------------------------|----------------------------------|
| POST   | `/api/orders`                          | Create a customer order          |
| GET    | `/api/orders`                          | List all orders                  |
| GET    | `/api/orders/{id}`                     | Get an order by ID               |
| GET    | `/api/stock-adjustments`               | List stock adjustment history    |
| GET    | `/api/stock-adjustments/medicine/{id}` | Get audit history for a medicine |
| POST   | `/api/stock-adjustments`               | Record a manual stock change     |

---

## 🔑 How to Use JWT Authentication

### 1. Login to get token
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "admin",
  "fullName": "System Administrator",
  "role": "ADMIN"
}
```

### 2. Use token in requests
```bash
curl -X GET http://localhost:8080/api/medicines \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

### 3. In Swagger UI
- Click the **Authorize 🔓** button at the top
- Paste your token (without "Bearer " prefix)
- Click **Authorize** — all endpoints now work!

---

## 🏗️ Project Structure

```
src/main/java/com/pharmacy/
├── PharmacyApplication.java     ← Main entry point
├── config/
│   ├── SecurityConfig.java      ← JWT + CORS + Security rules
│   ├── SwaggerConfig.java       ← OpenAPI documentation setup
│   └── DataSeeder.java          ← Creates default users & sample data
├── controller/
│   ├── AuthController.java      ← /api/auth/** endpoints
│   ├── BillingController.java    ← /api/orders/** endpoints
│   ├── MedicineController.java   ← /api/medicines/** endpoints
│   ├── StockAdjustmentController.java ← /api/stock-adjustments/** endpoints
│   └── UserController.java       ← /api/users/** endpoints
├── service/
│   ├── AuthService.java         ← Register/login business logic
│   ├── BillingService.java       ← Order creation & history
│   ├── MedicineService.java     ← Inventory business logic
│   ├── StockAdjustmentService.java ← Stock audit trail logic
│   └── UserDetailsServiceImpl.java ← Spring Security integration
├── repository/
│   ├── UserRepository.java      ← User DB queries
│   ├── CustomerOrderRepository.java ← Order DB queries
│   ├── MedicineRepository.java  ← Medicine DB queries
│   └── StockAdjustmentRepository.java ← Stock audit DB queries
├── model/
│   ├── User.java                ← User entity + roles
│   ├── Medicine.java            ← Medicine entity
│   ├── CustomerOrder.java       ← Order entity
│   └── StockAdjustment.java     ← Inventory audit entity
├── security/
│   ├── JwtUtil.java             ← Generate & validate JWT tokens
│   └── JwtAuthFilter.java       ← Intercept & authenticate requests
└── exception/
    └── GlobalExceptionHandler.java ← Clean error responses
```

---

## ☁️ Hosting on Railway (Free Tier)

### Step 1 — Push to GitHub
```bash
git init
git add .
git commit -m "Initial pharmacy backend"
git remote add origin https://github.com/YOUR_USERNAME/pharmacy-backend.git
git push -u origin main
```

### Step 2 — Deploy to Railway
1. Go to https://railway.app and sign in with GitHub
2. Click **New Project → Deploy from GitHub repo**
3. Select your `pharmacy-backend` repo
4. Add environment variables:
   ```
   SPRING_DATASOURCE_URL=jdbc:postgresql://...
   SPRING_DATASOURCE_USERNAME=postgres
   SPRING_DATASOURCE_PASSWORD=your_password
   JWT_SECRET=your_secret_key
   ```
5. Railway auto-detects Maven and deploys!
6. Your API will be live at: `https://pharmacy-backend-xxxx.railway.app`

### Step 3 — Add Railway token to GitHub Secrets
For auto-deploy via GitHub Actions:
1. In Railway: Account Settings → Tokens → Create Token
2. In GitHub repo: Settings → Secrets → New secret
   - Name: `RAILWAY_TOKEN`
   - Value: paste your Railway token

---

## 🧪 Testing with Swagger UI

Once running, open: `http://localhost:8080/swagger-ui.html`

You will see all endpoints organized by tag:
- **Authentication** — Register & Login
- **Medicine Inventory** — Full CRUD + search + alerts
- **Billing & Stock Audit** — Orders and inventory adjustment history

Steps:
1. Use `POST /api/auth/login` with admin credentials
2. Copy the token from the response
3. Click **Authorize** and paste the token
4. Try any endpoint!

---

## 📅 Coming Next (Week 3–4)
- `Prescription` entity and endpoints
- `Bill` & `BillItem` entities
- Sales reports endpoint
- PDF invoice generation

---

## 🛠️ Tech Stack
| Technology | Version | Purpose |
|---|---|---|
| Spring Boot | 3.5.12 | Framework |
| Spring Security | 6.x | Authentication |
| PostgreSQL | 15 | Database |
| jjwt | 0.12.3 | JWT tokens |
| springdoc-openapi | 2.3.0 | Swagger UI |
| Lombok | Latest | Reduce boilerplate |
| Java | 17 | Language |
