# Problems Report

This report groups the current Problems list by root cause. The Problems view shows 284 items, many of which are repeats of the same missing symbols.

## Blocking compile issues

### [src/main/java/com/pharmacy/security/JwtUtil.java](src/main/java/com/pharmacy/security/JwtUtil.java)
- Missing type: `JwtProperties` is not found at compile time.
- Fix: ensure a class exists at package `com.pharmacy.config` named `JwtProperties`, under `src/main/java`, and that its `package` line matches the folder structure. If it exists, re-check for typos in the package name and file name.

### [src/test/java/com/pharmacy/BillingIntegrationTest.java](src/test/java/com/pharmacy/BillingIntegrationTest.java)
- Missing DTO types: `OrderItemRequest`, `MedicineRequest`, `MedicineResponse`, `RegisterRequest`, `AuthRequest`.
- Missing fields in the test class: `mockMvc`, `objectMapper`.
- Missing model method: `Medicine.getStockQuantity()`.
- Fixes:
  - Ensure DTO classes are in `com.pharmacy.dto` and the imports match their exact names.
  - Add or inherit `MockMvc` and `ObjectMapper` (for example by extending a base test class that defines them or autowiring them directly).
  - Update tests to use the correct stock accessor in `Medicine` (or add `getStockQuantity()` if the model uses another field name).

## Test warnings

### [src/test/java/com/pharmacy/BillingIntegrationTest.java](src/test/java/com/pharmacy/BillingIntegrationTest.java)
- Methods like `shouldFetchAllOrdersAndById`, `shouldFailWithoutJwtToken`, `shouldFailWithNonExistentMedicineId`, `shouldHandleMultipleItemsInSingleOrder` are reported as unused.
- Fix: add `@Test` annotations (or remove unused methods).

## JPA entity warnings (safe to ignore)

### [src/main/java/com/pharmacy/model/User.java](src/main/java/com/pharmacy/model/User.java)
- Fields reported as unused: `id`, `fullName`, `email`, `createdAt`, `updatedAt`.
- `active` flagged as "can be final".
- These are typical for JPA entities; you can ignore or suppress if desired.

## Build configuration warnings

### [pom.xml](pom.xml)
- Project configuration out of date and Spring Boot patch update available.
- Fix: refresh the Maven project in the IDE; update Spring Boot version only if desired.
