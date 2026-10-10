
-- Test: Safe inventory reservation
-- Database: inventory_system

USE inventory_system;

-- Check the starting stock.
SELECT
    product_id,
    warehouse_id,
    on_hand_quantity,
    reserved_quantity,
    on_hand_quantity - reserved_quantity AS available_quantity
FROM inventory
WHERE product_id = 2
  AND warehouse_id = 1;

-- Begin a transaction.
START TRANSACTION;

-- Reserve 7 keyboards only if at least 7 are available.
UPDATE inventory
SET reserved_quantity = reserved_quantity + 7,
    version = version + 1
WHERE product_id = 2
  AND warehouse_id = 1
  AND on_hand_quantity - reserved_quantity >= 7;

-- Check how many rows were updated.
SELECT ROW_COUNT() AS rows_updated;

-- Inspect the result inside the transaction.
SELECT
    on_hand_quantity,
    reserved_quantity,
    on_hand_quantity - reserved_quantity AS available_quantity
FROM inventory
WHERE product_id = 2
  AND warehouse_id = 1;

-- Undo the test so the original inventory is preserved.
ROLLBACK;

-- Verify the stock after rollback.
SELECT
    on_hand_quantity,
    reserved_quantity,
    version
FROM inventory
WHERE product_id = 2
  AND warehouse_id = 1;
