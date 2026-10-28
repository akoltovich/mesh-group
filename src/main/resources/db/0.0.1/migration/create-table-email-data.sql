CREATE SEQUENCE IF NOT EXISTS email_id_seq START WITH 1;

CREATE TABLE IF NOT EXISTS email_data (
                               id BIGINT PRIMARY KEY,
                               user_id BIGINT,
                               email VARCHAR(200) UNIQUE,
                               CONSTRAINT fk_email_users FOREIGN KEY (user_id) REFERENCES users (id)
);