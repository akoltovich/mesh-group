INSERT INTO users (id, name, date_of_birth, password) VALUES
                                                          (1, 'Иван Иванов', '1993-05-01', 'password123'),
                                                          (2, 'Петр Петров', '1995-10-15', 'qwerty888');

INSERT INTO account (id, user_id, balance, initial_deposit) VALUES
                                                                (1, 1, 1000.00, 1000.00),
                                                                (2, 2, 500.00, 500.00);

INSERT INTO email_data (id, user_id, email) VALUES
                                                (1, 1, 'ivan@example.com'),
                                                (2, 1, 'ivan_work@example.com'),
                                                (3, 2, 'petr@example.com');

INSERT INTO phone_data (id, user_id, phone) VALUES
                                                (1, 1, '79207865432'),
                                                (2, 2, '79991112233');

SELECT setval('user_id_seq', (SELECT MAX(id) FROM users));
SELECT setval('account_id_seq', (SELECT MAX(id) FROM account));
SELECT setval('email_id_seq', (SELECT MAX(id) FROM email_data));
SELECT setval('phone_id_seq', (SELECT MAX(id) FROM phone_data));
