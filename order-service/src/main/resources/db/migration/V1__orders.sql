CREATE TABLE dining_table (
    id uuid PRIMARY KEY,
    branch_id uuid NOT NULL,
    table_number int NOT NULL,
    seats int NOT NULL,
    status varchar(32) NOT NULL,
    UNIQUE (branch_id, table_number)
);

CREATE TABLE orders (
    id uuid PRIMARY KEY,
    branch_id uuid NOT NULL,
    table_id uuid NOT NULL REFERENCES dining_table (id),
    waiter_id uuid NOT NULL,
    status varchar(32) NOT NULL,
    note varchar(500),
    version bigint NOT NULL
);

CREATE INDEX orders_branch_status ON orders (branch_id, status);

CREATE TABLE order_item (
    id uuid PRIMARY KEY,
    order_id uuid NOT NULL REFERENCES orders (id),
    dish_id uuid NOT NULL,
    name_snapshot varchar(160),
    unit_price numeric(12, 2),
    quantity int NOT NULL,
    modifiers_json varchar(2000),
    status varchar(32)
);
