
-- V2: Insert inventory sample data
-- Database: inventory_system

USE inventory_system;

INSERT INTO inventory
    (product_id, warehouse_id, on_hand_quantity, reserved_quantity)
VALUES
    -- Wireless Mouse
    (1, 1, 20, 0),
    (1, 2, 15, 0),

    -- Mechanical Keyboard
    (2, 1, 10, 0),
    (2, 2, 8, 0),

    -- USB-C Cable
    (3, 1, 50, 0),
    (3, 2, 30, 0);
