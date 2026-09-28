CREATE TABLE category (
    id uuid PRIMARY KEY,
    organization_id uuid NOT NULL,
    name varchar(120) NOT NULL,
    sort_order int NOT NULL,
    active boolean NOT NULL
);

CREATE TABLE dish (
    id uuid PRIMARY KEY,
    organization_id uuid NOT NULL,
    category_id uuid NOT NULL REFERENCES category (id),
    name varchar(160) NOT NULL,
    description varchar(500),
    base_price numeric(12, 2) NOT NULL,
    active boolean NOT NULL
);

CREATE TABLE modifier (
    id uuid PRIMARY KEY,
    dish_id uuid NOT NULL REFERENCES dish (id),
    name varchar(120) NOT NULL,
    price_delta numeric(12, 2) NOT NULL
);

CREATE TABLE branch_menu_override (
    branch_id uuid NOT NULL,
    dish_id uuid NOT NULL REFERENCES dish (id),
    custom_price numeric(12, 2),
    available boolean NOT NULL,
    PRIMARY KEY (branch_id, dish_id)
);
