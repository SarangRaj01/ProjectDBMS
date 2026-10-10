# Distributed Inventory and Order Consistency System

## 1. Problem Statement

Businesses operating multiple warehouses need a reliable way to manage products, inventory, orders and stock transfers. Concurrent customer requests, duplicate submissions and failed transactions can lead to overselling and inconsistent inventory records.

This project aims to build a database-driven inventory and order management system that preserves stock consistency, supports concurrent operations and provides reliable order tracking.

## 2. Project Objectives

- Design a normalized relational database.
- Manage products and inventory across multiple warehouses.
- Process orders using database transactions.
- Prevent overselling and negative inventory.
- Handle concurrent requests and duplicate submissions safely.
- Maintain a complete inventory movement and order history.
- Optimize SQL queries using indexes and execution-plan analysis.
- Build a secure REST API and web dashboard.
- Demonstrate correctness through automated tests and measurable benchmarks.

## 3. User Roles

- **Administrator:** Manages users, roles, products and system configuration.
- **Inventory Manager:** Receives stock, adjusts inventory and monitors stock levels.
- **Warehouse Manager:** Manages warehouse stock and transfers.
- **Order Manager:** Monitors orders, cancellations and fulfilment.
- **Viewer:** Reads authorized inventory, order and reporting data.

## 4. Functional Requirements

### Product Management
- Create, update, search and deactivate products.
- Maintain unique product SKUs.
- Organize products into categories.

### Warehouse and Inventory Management
- Maintain multiple warehouses.
- Track on-hand and reserved quantities per product and warehouse.
- Receive stock and record stock adjustments.
- Transfer stock between warehouses.
- Generate low-stock alerts.
- Maintain inventory movement history.

### Order Management
- Create orders containing multiple products.
- Reserve stock when an order is accepted.
- Track order status and status history.
- Cancel eligible orders and release reservations.
- Fulfil orders and update inventory correctly.
- Prevent duplicate order processing.

### Reporting
- View current inventory by warehouse.
- Identify low-stock and out-of-stock products.
- Report orders by status and date.
- Analyze inventory valuation and sales.
- Export authorized reports.

### Security
- Authenticate users.
- Enforce role-based permissions.
- Protect credentials and sensitive configuration.
- Record important administrative actions.

## 5. Business Rules

1. Every product must have a unique SKU.
2. Each product-warehouse combination must have at most one inventory record.
3. Inventory quantities must never become negative.
4. A reservation must not exceed available stock.
5. An order must not be confirmed unless its required stock reservation succeeds.
6. Cancellation must not release the same reservation twice.
7. Every stock change must have a traceable business reason.
8. Order status transitions must follow defined rules.
9. Retried requests must not duplicate successful business operations.
10. Stock transfers must preserve total stock across the source and destination, except for explicitly recorded losses or adjustments.
11. Monetary values must use appropriate decimal precision.
12. Related database changes must be committed or rolled back together where they belong to one database transaction.

## 6. Consistency Invariants

A consistency invariant is a condition that must remain true throughout the system's operation.

- Available quantity = on-hand quantity - reserved quantity.
- On-hand and reserved quantities must be non-negative.
- Reserved quantity must not exceed on-hand quantity.
- Each successful reservation must correspond to a valid order item.
- Every completed stock movement must have an auditable record.
- Repeating an idempotent operation must not duplicate its inventory effect.

## 7. Non-Functional Requirements

- **Correctness:** Prevent overselling and inconsistent transactions.
- **Performance:** Measure API latency, query execution time and concurrent throughput.
- **Security:** Apply authentication, authorization and input validation.
- **Reliability:** Handle failures and support recovery.
- **Maintainability:** Separate controllers, services, repositories and database migrations.
- **Testability:** Provide automated unit, integration and concurrency tests.
- **Portability:** Support reproducible setup and deployment using Docker.

## 8. Technology Stack

- Java 21
- Spring Boot
- Maven
- MySQL 8 with InnoDB
- React
- Node.js and npm
- Git and GitHub
- Postman
- Docker and Docker Compose

## 9. Initial Scope and Future Extensions

The initial implementation focuses on relational database design, product and warehouse management, order processing, stock reservations, transaction safety and performance testing.

Future extensions may include caching, asynchronous events, multiple backend instances, payment integration and distributed consistency workflows.