CREATE SEQUENCE IF NOT EXISTS user_id_seq START WITH 1;

CREATE TABLE IF NOT EXISTS users (
                            id BIGINT PRIMARY KEY,
                            name VARCHAR(500),
                            date_of_birth DATE,
                            password VARCHAR(500)
);