-- Створення гостьового користувача з обмеженими правами
-- Цей користувач може тільки переглядати інформацію про БД та користувачів

-- Створення користувача 'guest' з паролем 'guest_password'
CREATE USER IF NOT EXISTS 'GUEST'@'%' IDENTIFIED BY 'GUEST';

-- Надання прав для перегляду баз даних
GRANT SHOW DATABASES ON *.* TO 'GUEST'@'%';

-- Надання прав для перегляду користувачів та їх привілеїв
GRANT SELECT ON mysql.user TO 'GUEST'@'%';
GRANT SELECT ON mysql.db TO 'GUEST'@'%';
GRANT SELECT ON mysql.tables_priv TO 'GUEST'@'%';
GRANT SELECT ON mysql.columns_priv TO 'GUEST'@'%';

-- Надання базових прав для підключення
GRANT USAGE ON *.* TO 'GUEST'@'%';

-- Застосування змін
FLUSH PRIVILEGES;

-- Виведення інформації про створеного користувача
SELECT 
    'Guest user created successfully!' AS message,
    'Username: GUEST' AS username,
    'Password: GUEST_PASSWORD' AS password,
    'Host: %' AS host;

-- Запити які може виконувати guest користувач:
-- SHOW DATABASES;
-- SELECT User, Host FROM mysql.user;
-- SELECT * FROM mysql.db;
-- SHOW GRANTS FOR 'guest'@'%';
-- SHOW GRANTS FOR 'app_user'@'%';
-- SHOW GRANTS FOR 'root'@'%';
