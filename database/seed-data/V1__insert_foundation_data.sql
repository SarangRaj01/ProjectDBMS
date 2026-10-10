
-- V1: Insert foundational sample data
-- Database: inventory_system

USE inventory_system;

-- 1. Create application roles
INSERT INTO roles (role_name)
VALUES
    ('ADMIN'),
    ('INVENTORY_MANAGER'),
    ('ORDER_MANAGER'),
    ('VIEWER');

-- 2. Create sample users
-- Password hashes are placeholders, not real login credentials.
INSERT INTO users (role_id, full_name, email, password_hash)
VALUES
    (1, 'System Admin', 'admin@example.com', 'placeholder_hash_admin'),
    (2, 'Inventory Manager', 'inventory@example.com', 'placeholder_hash_inventory'),
    (3, 'Order Manager', 'orders@example.com', 'placeholder_hash_orders');

-- 3. Create sample customers
INSERT INTO customers (full_name, email, phone)
VALUES
    ('Aarav Nair', 'aarav@example.com', '9876500001'),
    ('Diya Menon', 'diya@example.com', '9876500002'),
    ('Rahul Kumar', 'rahul@example.com', '9876500003');

-- 4. Create product categories
INSERT INTO categories (category_name)
VALUES
    ('Electronics'),
    ('Accessories');

-- 5. Create products
INSERT INTO products
    (category_id, sku, product_name, description, unit_price)
VALUES
    (1, 'ELEC-001', 'Wireless Mouse', 'Wireless USB mouse', 799.00),
    (1, 'ELEC-002', 'Mechanical Keyboard', 'Mechanical USB keyboard', 2499.00),
    (2, 'ACC-001', 'USB-C Cable', 'One metre USB-C cable', 299.00);

-- 6. Create warehouses
INSERT INTO warehouses (warehouse_name, location)
VALUES
    ('Chennai Warehouse', 'Chennai, Tamil Nadu'),
    ('Vellore Warehouse', 'Vellore, Tamil Nadu');
