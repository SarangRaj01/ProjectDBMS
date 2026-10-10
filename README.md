<div align="center">

<img src="docs/assets/banner.svg" alt="Distributed Inventory & Order Consistency System" width="100%"/>

<br/>

<a href="https://github.com/SarangRaj01/ProjectDBMS"><img src="https://readme-typing-svg.demolab.com?font=JetBrains+Mono&weight=600&size=20&duration=3200&pause=900&color=58A6FF&center=true&vCenter=true&width=820&lines=Reliable+stock+reservation+across+multiple+warehouses;Transactions+that+never+oversell;Data+integrity+enforced+by+design;Java+21+%C2%B7+Spring+Boot+%C2%B7+MySQL+8" alt="Typing animation" /></a>

<br/>

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-Build-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![Hibernate](https://img.shields.io/badge/Hibernate-JPA-59666C?style=for-the-badge&logo=hibernate&logoColor=white)

![Status](https://img.shields.io/badge/status-in_progress-F5A623?style=flat-square&labelColor=0d1117)
![Tables](https://img.shields.io/badge/tables-12-58a6ff?style=flat-square&labelColor=0d1117)
![Endpoints](https://img.shields.io/badge/endpoints-1_live-3fb950?style=flat-square&labelColor=0d1117)
![Last commit](https://img.shields.io/github/last-commit/SarangRaj01/ProjectDBMS?style=flat-square&labelColor=0d1117&color=bc8cff)
![Stars](https://img.shields.io/github/stars/SarangRaj01/ProjectDBMS?style=flat-square&labelColor=0d1117&color=d29922)

<br/>

**[✨ Features](#-current-features)** &nbsp;·&nbsp;
**[🏗️ Architecture](#%EF%B8%8F-architecture)** &nbsp;·&nbsp;
**[🗃️ Database](#%EF%B8%8F-database-design)** &nbsp;·&nbsp;
**[🚀 Quick Start](#-getting-started)** &nbsp;·&nbsp;
**[🗺️ Roadmap](#%EF%B8%8F-roadmap)**

</div>

<img src="docs/assets/divider.svg" width="100%" alt=""/>

## 🌌 The Problem

> *Two customers. One last item. Same millisecond.*

Inventory systems must keep stock accurate while orders, reservations, and warehouse operations all happen at once. Get it wrong and you get **overselling**, **phantom inventory**, and **orders stuck in impossible states**.

This project attacks that problem head-on with a relational database and a REST API, built around three ideas:

<table align="center">
  <tr>
    <td align="center" width="33%">
      <h3>🔒</h3>
      <b>Transaction<br/>Management</b><br/><br/>
      <sub>Atomic order and stock updates that succeed together or not at all</sub>
    </td>
    <td align="center" width="33%">
      <h3>🧱</h3>
      <b>Data<br/>Integrity</b><br/><br/>
      <sub>Referential integrity enforced across every table</sub>
    </td>
    <td align="center" width="33%">
      <h3>⚖️</h3>
      <b>Inventory<br/>Consistency</b><br/><br/>
      <sub>Accurate stock across multiple warehouses</sub>
    </td>
  </tr>
</table>

<img src="docs/assets/divider.svg" width="100%" alt=""/>

## ✨ Current Features

| | Feature |
| :---: | :--- |
| 🗄️ | **Relational schema** covering users, roles, customers, categories, products, warehouses, and inventory |
| 🧾 | **Order tables** for orders, order items, stock reservations, inventory movements, and order status history |
| 🌱 | **SQL migration scripts** and sample seed data |
| 🔌 | **Spring Boot application** connected to MySQL |
| 🚀 | **Working `GET /api/products` endpoint** returning product records as JSON |

<img src="docs/assets/divider.svg" width="100%" alt=""/>

## 🛠️ Technology Stack

<div align="center">

<img src="https://skillicons.dev/icons?i=java,spring,mysql,maven,hibernate,git,github&theme=dark" alt="Tech stack icons" />

</div>

<br/>

| Technology | Purpose |
| :--- | :--- |
| ☕ **Java 21** | Backend programming |
| 🍃 **Spring Boot** | REST API and application framework |
| 🧩 **Spring Data JPA / Hibernate** | Database access and object-relational mapping |
| 🐬 **MySQL 8** | Relational data storage |
| 📦 **Maven** | Dependency and build management |
| 🐙 **Git & GitHub** | Version control and project hosting |

<img src="docs/assets/divider.svg" width="100%" alt=""/>

## 🏗️ Architecture

<img src="docs/assets/architecture.svg" alt="Request lifecycle: Client to REST API to Controller to Repository to JPA/Hibernate to MySQL" width="100%"/>

<details>
<summary><b>📐 View as a diagram (Mermaid)</b></summary>

```mermaid
flowchart LR
    A([🌐 Client]) -->|HTTP| B[🍃 Spring Boot<br/>REST API]
    B --> C[🎮 Product<br/>Controller]
    C --> D[📚 Product<br/>Repository]
    D --> E[🧩 JPA /<br/>Hibernate]
    E -->|SQL| F[(🐬 MySQL)]
    F -.->|rows| E
    E -.-> D
    D -.-> C
    C -.->|JSON| A

    classDef client fill:#1f6feb,stroke:#58a6ff,color:#fff
    classDef api fill:#238636,stroke:#3fb950,color:#fff
    classDef data fill:#8250df,stroke:#bc8cff,color:#fff
    classDef db fill:#0b5a85,stroke:#4479A1,color:#fff
    class A client
    class B,C api
    class D,E data
    class F db
```

</details>

<img src="docs/assets/divider.svg" width="100%" alt=""/>

## 🗃️ Database Design

**12 tables** across five domains:

```mermaid
flowchart TB
    subgraph IAM["🔐 Identity & Access"]
        roles --- users
    end
    subgraph CAT["🛍️ Product Catalog"]
        categories --- products
    end
    subgraph WH["🏭 Warehousing"]
        warehouses --- inventory
    end
    subgraph ORD["👥 Customers & Orders"]
        customers --- orders --- order_items
    end
    subgraph CON["🔄 Inventory Consistency"]
        stock_reservations
        inventory_movements
        order_status_history
    end

    products --- inventory
    products --- order_items
    orders --- stock_reservations
    inventory --- inventory_movements
    orders --- order_status_history
```

| Domain | Tables |
| :--- | :--- |
| 🔐 **Identity & access** | `roles` · `users` |
| 🛍️ **Product catalog** | `categories` · `products` |
| 🏭 **Warehousing** | `warehouses` · `inventory` |
| 👥 **Customers & orders** | `customers` · `orders` · `order_items` |
| 🔄 **Inventory consistency** | `stock_reservations` · `inventory_movements` · `order_status_history` |

> 📁 Schema definitions, seed data, and SQL test scripts live in [`database/`](database/).

<img src="docs/assets/divider.svg" width="100%" alt=""/>

## 🚀 Getting Started

**Prerequisites:** &nbsp; ☕ JDK 21 &nbsp;·&nbsp; 🐬 MySQL Server 8 &nbsp;·&nbsp; 🐙 Git

### 1️⃣ &nbsp;Clone

```bash
git clone https://github.com/SarangRaj01/ProjectDBMS.git
cd ProjectDBMS
```

### 2️⃣ &nbsp;Create the database

```sql
CREATE DATABASE inventory_system;
```

Then run the migration files in `database/migrations/` **in version order**, followed by the seed scripts in `database/seed-data/`.

### 3️⃣ &nbsp;Configure credentials

Create `backend/src/main/resources/application-local.properties` and set your MySQL username and password.

> [!WARNING]
> Never commit this file. Make sure `backend/.gitignore` excludes it.

### 4️⃣ &nbsp;Launch

From the `backend/` directory:

<table>
<tr><th>🪟 Windows</th><th>🍎 macOS / 🐧 Linux</th></tr>
<tr>
<td>

```bash
.\mvnw.cmd spring-boot:run
```

</td>
<td>

```bash
./mvnw spring-boot:run
```

</td>
</tr>
</table>

The backend starts on port **8080** once configuration and database validation succeed.

### 5️⃣ &nbsp;Make your first request

```http
GET http://localhost:8080/api/products
```

> [!TIP]
> Returns product records as JSON. Open it in a browser, Postman, or `curl http://localhost:8080/api/products`.

<img src="docs/assets/divider.svg" width="100%" alt=""/>

## 📂 Project Structure

```text
ProjectDBMS/
├── 🍃 backend/            Spring Boot application
├── 🗄️ database/
│   ├── migrations/        Schema creation scripts
│   ├── seed-data/         Sample data
│   └── queries/           SQL verification and test scripts
├── 📚 docs/               Documentation, ER diagram, README assets
├── 🎨 frontend/           Frontend workspace
├── 🧪 tests/              Project-level tests
└── 📄 README.md
```

<img src="docs/assets/divider.svg" width="100%" alt=""/>

## 🗺️ Roadmap

<div align="center">

![Progress](https://img.shields.io/badge/progress-33%25-58a6ff?style=for-the-badge&labelColor=0d1117) 

`🟦🟦🟦⬛⬛⬛⬛⬛⬛` &nbsp; **3 of 9 milestones complete**

</div>

```mermaid
timeline
    title Journey so far and ahead
    section ✅ Foundation
        Schema + seed data : Core relational design
        MySQL connection : Spring Boot wired to DB
        Product listing : GET /api/products
    section 🔨 Next up
        Validation : Service layer + error handling
        Write endpoints : Product create / update
    section 🔮 Future
        Orders : Placement + stock reservation
        Concurrency : Transaction + concurrent update tests
        Dashboard : Inventory UI
        Polish : API docs + integration tests
```

- [x] Create the core relational schema and seed data
- [x] Connect Spring Boot to MySQL
- [x] Implement the product listing endpoint
- [ ] Add service-layer validation and error handling
- [ ] Implement product creation and update endpoints
- [ ] Build order placement and stock reservation workflows
- [ ] Add transaction and concurrent stock-update tests
- [ ] Build the inventory dashboard
- [ ] Add API documentation and integration tests

<img src="docs/assets/divider.svg" width="100%" alt=""/>

## 🎯 Learning Objectives

| | Objective |
| :---: | :--- |
| 🧬 | Design normalized relational schemas and enforce referential integrity |
| 🌐 | Implement REST APIs using Spring Boot |
| 🧩 | Use JPA and Hibernate to access MySQL |
| ⚔️ | Understand transactions, stock reservations, and concurrent inventory updates |
| 🧪 | Test database-backed application behavior |

## 📌 Project Status

> [!NOTE]
> **🚧 In progress.** The database schema, sample data, MySQL connection, and initial product listing API are implemented. Order processing, automated testing, and the frontend remain future development tasks.

<br/>

<div align="center">

<img src="docs/assets/divider.svg" width="100%" alt=""/>

*Built as a learning project focused on database design, backend engineering, and reliable inventory management.*

<br/>

**Made with ☕ and curiosity by [@SarangRaj01](https://github.com/SarangRaj01)**

⭐ **Star this repo if it taught you something** ⭐

</div>