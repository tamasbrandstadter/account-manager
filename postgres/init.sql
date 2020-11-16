CREATE TABLE customers (
    customer_id     BIGSERIAL PRIMARY KEY,
    first_name      VARCHAR(25) NOT NULL,
    last_name       VARCHAR(25) NOT NULL
);

CREATE TABLE accounts (
    account_id      BIGSERIAL PRIMARY KEY,
    customer_id     BIGSERIAL,
    CONSTRAINT fk_customer
        FOREIGN KEY (customer_id)
            REFERENCES customers (customer_id),
    currency        VARCHAR(5) NOT NULL,
    balance         DECIMAL NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL
);