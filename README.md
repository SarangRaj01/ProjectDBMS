# Distributed Inventory & Order Consistency System

A database-driven inventory and order management backend built with Java, Spring Boot, and MySQL, designed to explore reliable stock reservation and order processing across multiple warehouses.

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1-green)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)
![Status](https://img.shields.io/badge/Project-In%20Progress-yellow)

## Overview

Inventory systems must maintain accurate stock levels while handling orders, reservations, and warehouse operations. Incorrect updates can lead to overselling, inconsistent inventory, and unreliable order states.

This project is being developed to model these problems through a relational database and a REST API, with a focus on transaction management, data integrity, and inventory consistency.

## Current Features

- Relational database schema covering users, roles, customers, categories, products, warehouses, and inventory.
- Order-related tables for orders, order items, stock reservations, inventory movements, and order status history.
- SQL migration scripts and sample seed data.
- Spring Boot application connected to MySQL.
- Working `GET /api/products` endpoint returning product records as JSON.

## Technology Stack

| Technology | Purpose |
|---|---|
| Java 21 | Backend programming |
| Spring Boot | REST API and application framework |
| Spring Data JPA / Hibernate | Database access and object-relational mapping |
| MySQL 8 | Relational data storage |
| Maven | Dependency and build management |
| Git and GitHub | Version control and project hosting |

## Architecture

```text
Client / Browser
       |
       v
Spring Boot REST API
       |
       v
Product Controller
       |
       v
Product Repository
       |
       v
Spring Data JPA / Hibernate
       |
       v
MySQL Database
```

## Database Design

The database currently includes 12 tables grouped around:

- **Identity and access:** `roles`, `users`
- **Product catalog:** `categories`, `products`
- **Warehousing:** `warehouses`, `inventory`
- **Customers and orders:** `customers`, `orders`, `order_items`
- **Inventory consistency:** `stock_reservations`, `inventory_movements`, `order_status_history`

Schema definitions, seed data, and SQL test scripts are maintained in the `database/` directory.

## Getting Started

### Prerequisites

- JDK 21
- MySQL Server 8
- Git

### 1. Clone the repository

```bash
git clone https://github.com/YOUR_USERNAME/ProjectDBMS.git
cd ProjectDBMS
```

Replace `YOUR_USERNAME` with the GitHub account that owns the repository.

### 2. Create the database

Create a MySQL database named `inventory_system`, then execute the SQL migration files in `database/migrations/` in version order. Run the seed-data scripts in `database/seed-data/` to insert sample records.

### 3. Configure database credentials

Create `backend/src/main/resources/application-local.properties` for local credentials. Set your MySQL username and password there, and do not commit this file.

Ensure that `backend/.gitignore` excludes the local configuration file.

### 4. Run the backend

From the `backend/` directory, run:

```powershell
.\mvnw.cmd spring-boot:run
```

The backend starts on port `8080` when configuration and database validation succeed.

### 5. Try the API

```http
GET http://localhost:8080/api/products
```

The endpoint returns product records as JSON.

## Project Structure

```text
ProjectDBMS/
├── backend/          # Spring Boot application
├── database/
│   ├── migrations/   # Schema creation scripts
│   ├── seed-data/    # Sample data
│   └── queries/      # SQL verification and test scripts
├── docs/             # Project documentation and ER diagram
├── frontend/         # Frontend workspace
├── tests/            # Project-level tests
└── README.md
```

## Roadmap

- [x] Create the core relational schema and seed data
- [x] Connect Spring Boot to MySQL
- [x] Implement the product listing endpoint
- [ ] Add service-layer validation and error handling
- [ ] Implement product creation and update endpoints
- [ ] Build order placement and stock reservation workflows
- [ ] Add transaction and concurrent stock-update tests
- [ ] Build the inventory dashboard
- [ ] Add API documentation and integration tests

## Learning Objectives

- Design normalized relational schemas and enforce referential integrity.
- Implement REST APIs using Spring Boot.
- Use JPA and Hibernate to access MySQL.
- Understand transactions, stock reservations, and concurrent inventory updates.
- Test database-backed application behavior.

## Project Status

**In progress.** The database schema, sample data, MySQL connection, and initial product listing API are implemented. Order processing, automated testing, and the frontend remain future development tasks.

---

*Built as a learning project focused on database design, backend engineering, and reliable inventory management.*