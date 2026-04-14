# Detailed Scan of plan.txt

Date: 2026-04-14
Project: Zcare Pharmacy Backend
Scope: Deep review of the forwarded plan in plan.txt, validated against the current codebase.

---

## 1) Executive Summary

The plan in plan.txt is strong as a long-term product vision, but it mixes three different things:
1. Current-state analysis
2. Future design proposals
3. Delivery timeline estimates

After scanning the repository, the plan is directionally correct about missing production pharmacy features (barcode, prescriptions, batch tracking, suppliers, reporting), but it has several factual mismatches against the current implementation.

Bottom line:
- Architecture foundation is good.
- Core pharmacy domain is currently minimal (inventory + orders + auth).
- Integration tests already exist (not zero testing).
- Documentation is partially out-of-sync with actual endpoints.
- The 12-week roadmap is possible only with strict scope control and migration discipline.

---

## 2) What Is Actually Implemented Today

### 2.1 Domain Models (current)
Verified entities:
- User
- Medicine
- CustomerOrder
- OrderItem

Validation source:
- src/main/java/com/pharmacy/model

### 2.2 API Surface (current)
Implemented and visible in controllers:
- Auth register/login
- Medicine CRUD + search + low-stock + expired
- Billing order creation

Validation source:
- src/main/java/com/pharmacy/controller/AuthController.java
- src/main/java/com/pharmacy/controller/MedicineController.java
- src/main/java/com/pharmacy/controller/BillingController.java

### 2.3 Security (current)
Implemented:
- JWT filter
- Role-based method security
- Stateless session policy
- CORS from config property

Validation source:
- src/main/java/com/pharmacy/config/SecurityConfig.java

### 2.4 Testing (current)
Implemented integration tests:
- AuthIntegrationTest
- MedicineIntegrationTest
- BillingIntegrationTest

Validation source:
- src/test/java/com/pharmacy

---

## 3) Accuracy Check Against plan.txt

### 3.1 Correct observations in plan.txt
The following are accurate gaps for production-grade pharmacy operations:
- No barcode field/workflow
- No prescription management
- No customer/patient domain
- No batch/lot recall-ready structure
- No supplier/purchase-order workflow
- No reporting and analytics module
- No stock audit trail entity for adjustment history

### 3.2 Inaccurate or outdated statements in plan.txt
1. "No tests"
- Not correct. Integration tests are present and non-trivial.

2. Some endpoint assumptions in docs/plan differ from current controllers
- README and plan mention endpoints that are not currently implemented exactly as described.

3. SQL injection note is phrased as a direct gap
- Repository usage is Spring Data JPA (parameterized by design for typical operations). Risk still exists for future custom native queries, but current baseline is not raw SQL-heavy.

4. CORS example includes wildcard with credentials
- Pattern like allowing "*" while credentials are enabled is not valid for strict browser security behavior.

### 3.3 Data model expansion risk not called out strongly enough
The plan proposes a large Medicine expansion in one step. This can create:
- migration churn,
- DTO breakage,
- test fragility,
- frontend contract instability.

Safer approach: stage fields by business value.

---

## 4) Gap Matrix (Current vs Proposed)

### Phase-1-priority gaps (high value, low-to-medium complexity)
1. Barcode support
- Status: Missing
- Suggested first deliverable: add barcode + unique index + lookup endpoint

2. Expiring-soon endpoint
- Status: Missing
- Suggested first deliverable: repository query + controller filter by days

3. Pagination for medicine listing
- Status: Missing
- Suggested first deliverable: Pageable endpoint preserving backward compatibility

4. Stock adjustment audit
- Status: Missing
- Suggested first deliverable: StockAdjustment entity + add/retrieve history APIs

### Phase-2 core domain gaps (medium/high complexity)
1. Customer/Patient management
2. Prescription + PrescriptionItem lifecycle
3. Batch/Lot tracking
4. Supplier basics

### Phase-3/4 platform gaps (higher complexity)
1. Reports (sales/inventory/profitability)
2. Notification workflows (expiry/low-stock)
3. Purchase orders and receiving
4. Mobile-optimized endpoints and operational dashboards

---

## 5) Technical Risks in the Proposed Plan

1. Overloading Medicine too early
- The plan adds 20+ fields in one migration.
- Risk: poor data quality and slow release cycles.

2. API contract break risk
- Renaming price to sellingPrice directly can break existing consumers/tests.
- Better: additive migration (keep price temporarily, deprecate later).

3. Migration strategy not explicit
- Need Flyway/Liquibase baseline to avoid schema drift and unclear rollback paths.

4. Security hardening sequence
- Rate limiting, audit logs, and operational controls should be introduced before broadening endpoints.

5. Test strategy should be pyramid-based
- Current integration tests are good, but service-level unit tests and repository tests should be layered in.

---

## 6) Corrected Implementation Order (Practical)

### Sprint A (1-2 weeks)
1. Introduce DB migration framework (Flyway).
2. Add barcode (entity, DTO, repository, endpoint, validation).
3. Add expiring-soon endpoint.
4. Add pagination to GET medicines.
5. Keep existing response shape stable; introduce additive fields only.

### Sprint B (1-2 weeks)
1. Add StockAdjustment entity and APIs.
2. Add reorder-point fields (minStockLevel) minimally.
3. Add integration tests for new endpoints.

### Sprint C (2-3 weeks)
1. Add Customer and Prescription domains.
2. Implement dispense flow with stock linkage and conflict-safe transaction logic.

### Sprint D (2-3 weeks)
1. Add Batch and Supplier.
2. Add recall and expiry-by-batch queries.

### Sprint E (2+ weeks)
1. Reporting APIs.
2. Notification scheduler and templates.
3. Operational hardening: rate-limit, audit logs, metrics.

---

## 7) Documentation Quality Findings

1. plan.txt contains forwarded email header noise at top.
- This should be removed for a clean technical artifact.

2. README and code diverge on some endpoints.
- Action: regenerate endpoint list from controllers and sync docs.

3. Existing docs imply broader coverage than current model set.
- Action: include explicit "Implemented vs Planned" section in docs.

---

## 8) Immediate Actionable Tasks

1. Clean plan.txt into a pure technical markdown document.
2. Add a concise RFC for Phase 1 schema/API changes.
3. Implement barcode + expiring-soon + pagination in one controlled PR.
4. Update integration tests and Swagger after that PR.
5. Then start stock adjustment audit trail as a second PR.

---

## 9) Final Assessment

The plan is ambitious and largely correct in direction, but it should be converted from a broad vision document into an incremental delivery plan tied to current code reality.

Current readiness:
- Learning/demo: good
- Small controlled production pilot: possible after Phase-1 hardening
- Full pharmacy operations: not yet, requires domain expansion and compliance-grade traceability

If needed, this scan can be converted next into:
- a strict milestone checklist,
- a PR-by-PR implementation plan,
- and a Flyway migration map (V2-V10) with rollback notes.
