-- Додаткові таблиці для системи користувачів та звітів
USE company_db;

-- Таблиця користувачів
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL,
  `password` VARCHAR(255) NOT NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username_UNIQUE` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Таблиця звітів про відрядження
DROP TABLE IF EXISTS `reports`;
CREATE TABLE `reports` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `order_id` INT NOT NULL,
  `comments` TEXT NOT NULL,
  `date` DATE NOT NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_reports_orders_idx` (`order_id`),
  CONSTRAINT `fk_reports_orders` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id-order`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Індекси для покращення продуктивності
CREATE INDEX `idx_reports_date` ON `reports` (`date`);
CREATE INDEX `idx_users_username` ON `users` (`username`);

-- Вставка тестових даних (опціонально)
-- INSERT INTO users (username, password) VALUES ('admin', 'admin123');
-- INSERT INTO users (username, password) VALUES ('user', 'user123');

