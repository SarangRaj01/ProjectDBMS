-- V3: Add performance indexes for high-throughput concurrency and audit queries
-- Database: inventory_system
-- Engine: InnoDB

-- 1. Accelerate order queries by customer, status, and creation date
CREATE INDEX idx_orders_customer ON orders(customer_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at);

-- 2. Accelerate order line item joins
CREATE INDEX idx_order_items_order ON order_items(order_id);
CREATE INDEX idx_order_items_product ON order_items(product_id);

-- 3. Accelerate inventory audit and movement tracking
CREATE INDEX idx_movements_prod_wh ON inventory_movements(product_id, warehouse_id);
CREATE INDEX idx_movements_created_at ON inventory_movements(created_at);
CREATE INDEX idx_movements_reference ON inventory_movements(reference_type, reference_id);

-- 4. Accelerate stock reservation lookups by order item
CREATE INDEX idx_reservations_order_item ON stock_reservations(order_item_id);
CREATE INDEX idx_reservations_status ON stock_reservations(status);
