CREATE TABLE organization (
    id uuid PRIMARY KEY,
    name varchar(200) NOT NULL,
    code varchar(64) NOT NULL UNIQUE,
    status varchar(32) NOT NULL
);

CREATE TABLE branch (
    id uuid PRIMARY KEY,
    organization_id uuid NOT NULL REFERENCES organization (id),
    name varchar(200) NOT NULL,
    code varchar(64) NOT NULL UNIQUE,
    currency varchar(3) NOT NULL,
    timezone varchar(64) NOT NULL,
    status varchar(32) NOT NULL
);

CREATE TABLE branch_settings (
    branch_id uuid PRIMARY KEY REFERENCES branch (id),
    tax_rate numeric(8, 4) NOT NULL,
    service_charge_pct numeric(8, 4) NOT NULL,
    receipt_footer_text varchar(500) NOT NULL
);

CREATE TABLE app_user (
    id uuid PRIMARY KEY,
    organization_id uuid NOT NULL REFERENCES organization (id),
    branch_id uuid REFERENCES branch (id),
    username varchar(64) NOT NULL UNIQUE,
    password_hash varchar(100) NOT NULL,
    role varchar(32) NOT NULL,
    status varchar(32) NOT NULL
);

CREATE TABLE shift (
    id uuid PRIMARY KEY,
    branch_id uuid NOT NULL REFERENCES branch (id),
    user_id uuid NOT NULL REFERENCES app_user (id),
    opened_at timestamptz NOT NULL,
    closed_at timestamptz
);

CREATE UNIQUE INDEX shift_one_open ON shift (user_id) WHERE closed_at IS NULL;
