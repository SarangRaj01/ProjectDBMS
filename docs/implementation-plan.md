# Implementation Plan — Distributed Inventory & Order Consistency System

> **Generated:** 2026-10-13 · **Author:** Repository Audit (Phase 1)

---

## 1. Current Implementation Status

### ✅ What Works

| Component | Status | Details |
|:--|:--|:--|
| **Database schema** | ✅ Complete | 12 tables across 2 migration scripts |
| **Seed data** | ✅ Loaded | 4 roles, 3 users, 3 customers, 2 categories, 3 products, 2 warehouses, 6 inventory records |
| **Spring Boot app** | ✅ Starts | Java 21, Spring Boot 4.1.1, connects to MySQL |
| **`GET /api/products`** | ✅ Live | Returns 3 product JSON records |
| **Git & GitHub** | ✅ Set up | Remote: `SarangRaj01/ProjectDBMS`, 4 commits on `main` |
| **`.gitignore`** | ✅ Secure | `application-local.properties` excluded in `backend/.gitignore` |
| **SQL test script** | ✅ Present | `test_inventory_transaction.sql` demonstrates atomic reservation with ROLLBACK |

### ⚠️ Minor Issues Found

| Issue | Severity | Details |
|:--|:--|:--|
| **V1 migration comment removed** | Trivial | `--stroage engine` comment was removed from line 10 of `V1__create_core_tables.sql`. This is a harmless whitespace change. |
| **`startup.log` tracked** | Low | `backend/startup.log` is a generated file. It should be in `.gitignore`, not committed. |
| **Empty `frontend/` and `tests/`** | Expected | Both directories are empty placeholders — no work lost. |
| **Possible stale reservation** | Medium | You mentioned a manual test reservation of 7 keyboards in Chennai. The SQL test script uses `ROLLBACK`, so if you ran it as written the data was not committed. We'll verify live DB state before any data changes. |
| **`Product.java` missing setters** | Medium | Entity has only getters — cannot create/update products via API yet. |
| **No service layer** | High | Controller calls repository directly — no place for business logic, validation, or transactions. |

---

## 2. Architecture — Current vs Target

### Current (Minimal)

```
Client → ProductController → ProductRepository → MySQL
```

Only one entity (`Product`), one controller, one repository. No DTOs, no service layer, no error handling.

### Target (Layered)

```
Client → Controller → Service → Repository → MySQL
           ↓              ↓          ↓
         DTOs        Transactions   Entities
           ↓
    Exception Handler
```

Each layer has a clear job:

| Layer | Responsibility |
|:--|:--|
| **Controller** | Accept HTTP requests, validate input, delegate to service, return responses |
| **Service** | Business rules, transaction boundaries, orchestration |
| **Repository** | Database queries (JPA + custom SQL where needed) |
| **Entity** | JPA table mappings |
| **DTO** | Request/response objects (never expose raw entities) |
| **Exception** | Centralized error handler returning consistent error JSON |

---

## 3. Existing Database Schema

### 12 Tables (all InnoDB, all with constraints)

```
┌─────────────────────────────────────────────────────────┐
│  IDENTITY & ACCESS                                      │
│  roles ──< users                                        │
├─────────────────────────────────────────────────────────┤
│  PRODUCT CATALOG                                        │
│  categories (self-referencing) ──< products              │
├─────────────────────────────────────────────────────────┤
│  WAREHOUSING                                            │
│  warehouses ──< inventory >── products                   │
│  (unique constraint on product_id + warehouse_id)        │
│  (version column for optimistic locking)                 │
├─────────────────────────────────────────────────────────┤
│  ORDERS                                                 │
│  customers ──< orders ──< order_items >── products       │
│  orders has idempotency_key (UNIQUE)                     │
│  order_items.line_total = GENERATED ALWAYS AS            │
│      (quantity * unit_price) STORED                      │
├─────────────────────────────────────────────────────────┤
│  CONSISTENCY & AUDIT                                    │
│  stock_reservations >── order_items, warehouses           │
│  inventory_movements >── products, warehouses, users      │
│  order_status_history >── orders, users                   │
└─────────────────────────────────────────────────────────┘
```

### Key Design Strengths Already Present
- `CHECK` constraints enforce non-negative quantities, valid statuses
- `reserved_quantity <= on_hand_quantity` enforced at DB level
- `version` column on `inventory` table — ready for optimistic locking
- `idempotency_key` on `orders` — prevents duplicate order creation
- `line_total` is a generated column — no client-side math errors possible
- `inventory_movements` has `reference_type` + `reference_id` — polymorphic audit trail

### Schema Observations
- No index on `inventory_movements.created_at` — will be needed for audit queries
- No index on `orders.customer_id` or `orders.status` — needed for filtering
- `order_status_history` has `changed_by BIGINT` referencing `users` — good for audit

---

## 4. Identified Gaps

### 4.1 Missing Java Entities (10 of 12 tables unmapped)

| Table | JPA Entity | Status |
|:--|:--|:--|
| `products` | `Product.java` | ✅ Exists |
| `roles` | — | ❌ Missing |
| `users` | — | ❌ Missing |
| `customers` | — | ❌ Missing |
| `categories` | — | ❌ Missing |
| `warehouses` | — | ❌ Missing |
| `inventory` | — | ❌ Missing |
| `orders` | — | ❌ Missing |
| `order_items` | — | ❌ Missing |
| `stock_reservations` | — | ❌ Missing |
| `inventory_movements` | — | ❌ Missing |
| `order_status_history` | — | ❌ Missing |

### 4.2 Missing API Endpoints

| Category | Endpoint | Status |
|:--|:--|:--|
| Products | `GET /api/products` | ✅ Works |
| Products | `GET /api/products/{id}` | ❌ |
| Products | `POST /api/products` | ❌ |
| Products | `PUT /api/products/{id}` | ❌ |
| Products | `GET /api/products?search=...` | ❌ |
| Warehouses | `GET /api/warehouses` | ❌ |
| Inventory | `GET /api/inventory` | ❌ |
| Orders | `POST /api/orders` | ❌ |
| Orders | `GET /api/orders/{id}` | ❌ |
| Orders | `GET /api/orders` | ❌ |
| Orders | `POST /api/orders/{id}/cancel` | ❌ |

### 4.3 Missing Backend Components

- **Service layer** — no business logic separation
- **DTOs** — entity exposed directly in API responses
- **Exception handler** — no `@ControllerAdvice`, no error JSON format
- **Input validation** — `spring-boot-starter-validation` is in `pom.xml` but unused
- **Transaction management** — no `@Transactional` anywhere
- **Concurrency control** — `version` column exists but `@Version` annotation not used

### 4.4 Missing Tests

- `BackendApplicationTests.java` has only `contextLoads()` — requires running MySQL
- No unit tests, no integration tests, no concurrency tests
- `tests/` directory is empty

### 4.5 Files That Should Not Be Committed

| File | Problem | Fix |
|:--|:--|:--|
| `backend/startup.log` | Generated output | Add `*.log` to `backend/.gitignore`, delete file |
| `backend/target/` | Maven build output | Already in `.gitignore` ✅ |

---

## 5. Implementation Phases

### Phase 2 — Foundation Layer (Entity + Service + DTO + Error Handling)

**Goal:** Establish the layered architecture. Make all 12 tables accessible from Java.

1. Restructure Java packages:
   ```
   com.inventory.backend/
   ├── controller/
   ├── service/
   ├── repository/
   ├── entity/
   ├── dto/
   ├── exception/
   └── config/
   ```
2. Create JPA entities for all 12 tables (with proper relationships, `@Version` on `inventory`)
3. Move `Product.java` to `entity/` package, add setters
4. Create repositories for all entities
5. Create `GlobalExceptionHandler` (`@ControllerAdvice`) with consistent error response format
6. Create request/response DTOs for products
7. Add `ProductService` between controller and repository
8. Expand product endpoints: GET by ID, POST, PUT, search/filter
9. Add Jakarta Bean Validation annotations (`@NotNull`, `@Positive`, etc.)
10. Add `startup.log` to `.gitignore`

**Acceptance Criteria:**
- All 12 entities validate against the live database (`ddl-auto=validate` passes)
- `GET /api/products` still works
- `POST /api/products` creates a product with validation
- Invalid requests return structured error JSON with appropriate HTTP status codes
- No existing data is modified or deleted

### Phase 3 — Warehouse & Inventory APIs

**Goal:** Expose inventory data with proper available-quantity calculation.

1. `WarehouseController` + `WarehouseService` — list warehouses
2. `InventoryController` + `InventoryService` — view inventory by product/warehouse
3. Available quantity = `on_hand_quantity - reserved_quantity` (calculated, not stored)
4. Inventory response DTO includes product name, warehouse name, all quantities

**Acceptance Criteria:**
- `GET /api/warehouses` returns 2 warehouses
- `GET /api/inventory?productId=2` shows Keyboard stock across both warehouses
- Available quantity is computed correctly

### Phase 4 — Order Management (The Core)

**Goal:** Implement the full order lifecycle with atomic stock reservation.

1. `OrderController` + `OrderService`
2. Create order endpoint (`POST /api/orders`):
   - Accept customer ID, idempotency key, list of items (product ID + quantity)
   - Validate all product IDs exist and are ACTIVE
   - For each item, find inventory with sufficient available quantity
   - Reserve stock atomically using `UPDATE ... WHERE available >= requested`
   - Create order, order_items, stock_reservations, inventory_movements, order_status_history — all in one `@Transactional`
   - If any item fails → entire transaction rolls back (no partial reservations)
   - Idempotency: if `idempotency_key` already exists, return the existing order
3. Cancel order endpoint (`POST /api/orders/{id}/cancel`):
   - Only PENDING or CONFIRMED orders can be cancelled
   - Release all reservations (set `status=RELEASED`, decrement `reserved_quantity`)
   - Record status history and inventory movements
   - Prevent double-cancellation
4. Get order endpoint (`GET /api/orders/{id}`)
5. List orders with filtering (`GET /api/orders?status=...&customerId=...`)

**Concurrency Strategy (documented in `docs/architecture.md`):**
- **Pessimistic locking** via `SELECT ... FOR UPDATE` on `inventory` rows during reservation
- This prevents two concurrent orders from reading the same available quantity
- Combined with the DB-level `CHECK (reserved_quantity <= on_hand_quantity)` as a safety net
- Deadlock handling: Spring `@Retryable` with bounded retries (max 3)

**Order State Machine:**
```
PENDING → CONFIRMED → SHIPPED → DELIVERED
   ↓          ↓
CANCELLED  CANCELLED
```

**Acceptance Criteria:**
- Creating an order reserves stock and all records are consistent
- Attempting to order more than available stock returns 409 Conflict
- Two simultaneous orders for the last item — only one succeeds
- Cancelling an order releases exactly the reserved quantity
- Re-cancelling the same order returns 409, does not double-release
- Duplicate idempotency key returns the existing order (no duplicate)

### Phase 5 — Concurrency Tests

**Goal:** Prove the system handles concurrent requests correctly.

1. JUnit integration tests using `ExecutorService` with multiple threads
2. Test scenarios from `docs/summary.md`:
   - Two simultaneous orders exceed stock → one fails
   - Transaction fails halfway → no partial state
   - Duplicate idempotency key → no duplicate effect
   - Cancel order → reservation released exactly once
3. Place in `backend/src/test/java/` with proper test profiles

**Acceptance Criteria:**
- All concurrency tests pass consistently
- No overselling detected after 100 concurrent order attempts
- Test report shows pass/fail for each scenario

### Phase 6 — Database Performance & Indexes

**Goal:** Add missing indexes and demonstrate query performance.

1. New migration `V3__add_performance_indexes.sql`:
   - `orders(customer_id)`, `orders(status)`, `orders(created_at)`
   - `inventory_movements(created_at)`, `inventory_movements(product_id, warehouse_id)`
   - `order_items(order_id)`, `order_items(product_id)`
2. EXPLAIN ANALYZE for key queries
3. Document findings in `docs/performance.md`

### Phase 7 — Frontend Dashboard (Future)

Simple React dashboard showing:
- Product list with stock levels
- Order placement form
- Order status tracking

---

## 6. Risks & Compatibility

| Risk | Mitigation |
|:--|:--|
| Spring Boot 4.1.1 uses Jakarta namespace | Already using `jakarta.persistence` ✅ |
| `ddl-auto=validate` will fail if entities don't match DB | Map entities precisely to existing columns |
| Moving `Product.java` to new package breaks import | Update all references in same commit |
| MySQL reserved word conflicts | Use backtick escaping in `@Column(name=...)` if needed |
| Stale reservation data in DB | Inspect live DB state before any corrective migration |
| `order_items.line_total` is GENERATED | Mark as `insertable=false, updatable=false` in JPA |
| No `application-local.properties` in repo | App uses `${DB_PASSWORD}` env var — developers must set it |

---

## 7. File Organization After Phase 2

```
backend/src/main/java/com/inventory/backend/
├── BackendApplication.java
├── config/
├── controller/
│   └── ProductController.java
├── dto/
│   ├── request/
│   │   └── ProductRequest.java
│   └── response/
│       ├── ProductResponse.java
│       └── ErrorResponse.java
├── entity/
│   ├── Product.java
│   ├── Category.java
│   ├── Role.java
│   ├── User.java
│   ├── Customer.java
│   ├── Warehouse.java
│   ├── Inventory.java
│   ├── Order.java
│   ├── OrderItem.java
│   ├── StockReservation.java
│   ├── InventoryMovement.java
│   └── OrderStatusHistory.java
├── exception/
│   ├── GlobalExceptionHandler.java
│   └── ResourceNotFoundException.java
├── repository/
│   ├── ProductRepository.java
│   ├── CategoryRepository.java
│   └── ... (one per entity)
└── service/
    └── ProductService.java
```

---

## 8. Next Step

**Phase 2 starts now.** I will:
1. Create all 12 JPA entities mapped exactly to the existing database
2. Restructure packages (controller/, service/, entity/, dto/, exception/, repository/)
3. Add ProductService with validation
4. Add global exception handler
5. Expand product endpoints (GET by ID, POST, PUT, search)
6. Add `.gitignore` entries for `startup.log`

No tables will be dropped. No data will be deleted. The existing `GET /api/products` endpoint will continue to work throughout.
