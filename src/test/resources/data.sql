INSERT INTO customers (customer_id, first_name, last_name)
VALUES (1, 'first', 'last');
INSERT INTO customers (customer_id, first_name, last_name)
VALUES (2, 'first2', 'last2');

INSERT INTO accounts (account_id, customer_id, currency, balance, created_at)
VALUES (11, 1, 'EUR', 15246.25, PARSEDATETIME('15-11-2020 13:00:00 GMT', 'dd-MM-yyyy hh:mm:ss z'));
INSERT INTO accounts (account_id, customer_id, currency, balance, created_at)
VALUES (22, 2, 'EUR', 0, PARSEDATETIME('15-11-2020 13:05:00 GMT', 'dd-MM-yyyy hh:mm:ss z'));