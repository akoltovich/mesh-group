CREATE SEQUENCE IF NOT EXISTS phone_id_seq START WITH 1;

CREATE TABLE IF NOT EXISTS phone_data (
                               id BIGINT PRIMARY KEY,
                               user_id BIGINT,
                               phone VARCHAR(13) UNIQUE,
                               CONSTRAINT fk_phone_users FOREIGN KEY (user_id) REFERENCES users (id)
);