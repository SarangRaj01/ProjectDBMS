CREATE TABLE orders (
    order_id BIGINT PRIMARY KEY AUTO_INCREMENT,

    customer_id BIGINT NOT NULL,

    idempotency_key VARCHAR(100) NOT NULL UNIQUE,

    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

    total_amount DECIMAL(12, 2) NOT NULL DEFAULT 0.00,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_orders_customer
        FOREIGN KEY (customer_id)
        REFERENCES customers(customer_id),

    CONSTRAINT chk_orders_status
        CHECK (status IN (
            'PENDING',
            'CONFIRMED',
            'CANCELLED',
            'SHIPPED',
            'DELIVERED'
        )),

    CONSTRAINT chk_orders_total
        CHECK (total_amount >= 0)
) ENGINE = InnoDB;
CREATE TABLE order_items (
    order_item_id BIGINT PRIMARY KEY AUTO_INCREMENT,

    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,

    quantity INT NOT NULL,

    unit_price DECIMAL(12, 2) NOT NULL,

    line_total DECIMAL(14, 2)
        GENERATED ALWAYS AS (quantity * unit_price) STORED,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id)
        REFERENCES orders(order_id),

    CONSTRAINT fk_order_items_product
        FOREIGN KEY (product_id)
        REFERENCES products(product_id),

    CONSTRAINT chk_order_items_quantity
        CHECK (quantity > 0),

    CONSTRAINT chk_order_items_unit_price
        CHECK (unit_price >= 0)
) ENGINE = InnoDB;
CREATE TABLE stock_reservations (
    reservation_id BIGINT PRIMARY KEY AUTO_INCREMENT,

    order_item_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,

    quantity INT NOT NULL,

    status VARCHAR(20) NOT NULL DEFAULT 'RESERVED',

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_reservations_order_item
        FOREIGN KEY (order_item_id)
        REFERENCES order_items(order_item_id),

    CONSTRAINT fk_reservations_warehouse
        FOREIGN KEY (warehouse_id)
        REFERENCES warehouses(warehouse_id),

    CONSTRAINT chk_reservations_quantity
        CHECK (quantity > 0),

    CONSTRAINT chk_reservations_status
        CHECK (status IN (
            'RESERVED',
            'RELEASED',
            'CONSUMED'
        ))
) ENGINE = InnoDB;
CREATE TABLE inventory_movements (
    movement_id BIGINT PRIMARY KEY AUTO_INCREMENT,

    product_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    user_id BIGINT,

    movement_type VARCHAR(30) NOT NULL,
    quantity_change INT NOT NULL,

    reference_type VARCHAR(30),
    reference_id BIGINT,

    notes VARCHAR(500),

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_movements_product
        FOREIGN KEY (product_id)
        REFERENCES products(product_id),

    CONSTRAINT fk_movements_warehouse
        FOREIGN KEY (warehouse_id)
        REFERENCES warehouses(warehouse_id),

    CONSTRAINT fk_movements_user
        FOREIGN KEY (user_id)
        REFERENCES users(user_id),

    CONSTRAINT chk_movements_type
        CHECK (movement_type IN (
            'RECEIPT',
            'SHIPMENT',
            'TRANSFER_IN',
            'TRANSFER_OUT',
            'ADJUSTMENT',
            'RETURN'
        )),

    CONSTRAINT chk_movements_quantity
        CHECK (quantity_change <> 0)
) ENGINE = InnoDB;
CREATE TABLE order_status_history (
    history_id BIGINT PRIMARY KEY AUTO_INCREMENT,

    order_id BIGINT NOT NULL,
    changed_by BIGINT,

    old_status VARCHAR(30),
    new_status VARCHAR(30) NOT NULL,

    change_reason VARCHAR(500),

    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_status_history_order
        FOREIGN KEY (order_id)
        REFERENCES orders(order_id),

    CONSTRAINT fk_status_history_user
        FOREIGN KEY (changed_by)
        REFERENCES users(user_id),

    CONSTRAINT chk_status_history_old_status
        CHECK (
            old_status IS NULL OR
            old_status IN (
                'PENDING',
                'CONFIRMED',
                'CANCELLED',
                'SHIPPED',
                'DELIVERED'
            )
        ),

    CONSTRAINT chk_status_history_new_status
        CHECK (new_status IN (
            'PENDING',
            'CONFIRMED',
            'CANCELLED',
            'SHIPPED',
            'DELIVERED'
        ))
) ENGINE = InnoDB;