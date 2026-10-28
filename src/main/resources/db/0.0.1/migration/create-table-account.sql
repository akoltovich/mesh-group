CREATE SEQUENCE IF NOT EXISTS account_id_seq START WITH 1;

CREATE TABLE IF NOT EXISTS account (
                            id BIGINT PRIMARY KEY,
                            user_id BIGINT UNIQUE,
                            balance DECIMAL(19, 2),
                            initial_deposit DECIMAL(19, 2),
                            CONSTRAINT fk_account_users FOREIGN KEY (user_id) REFERENCES users (id),
                            CONSTRAINT check_balance_positive CHECK (balance >= 0)
);