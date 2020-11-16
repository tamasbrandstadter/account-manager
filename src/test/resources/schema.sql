DROP TABLE IF EXISTS customers cascade;
DROP TABLE IF EXISTS accounts cascade;

CREATE TABLE customers
(
    customer_id BIGINT auto_increment primary key,
    first_name  VARCHAR(25) NOT NULL,
    last_name   VARCHAR(25) NOT NULL
);

CREATE TABLE accounts
(
    account_id  BIGINT auto_increment PRIMARY KEY,
    customer_id BIGINT,
    foreign key (customer_id) references customers (customer_id),
    currency    VARCHAR(5)               NOT NULL,
    balance     DECIMAL(19, 4)           NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL
);