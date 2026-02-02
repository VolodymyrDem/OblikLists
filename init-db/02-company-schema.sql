-- Використання основної бази даних
USE company_db;

-- Створення таблиць з основної схеми
DROP TABLE IF EXISTS `cars`;
CREATE TABLE `cars` (
  `id-car` int NOT NULL AUTO_INCREMENT,
  `number` varchar(45) NOT NULL,
  `model` varchar(45) NOT NULL,
  `fuel-type` varchar(45) NOT NULL,
  `fuel-usage` double NOT NULL,
  `engine-volume` double NOT NULL,
  `start-date` date NOT NULL,
  `start-order-number` varchar(45) NOT NULL,
  `end-date` date DEFAULT NULL,
  `end-order-number` varchar(45) DEFAULT NULL,
  `valid` tinyint NOT NULL,
  `start-fuel` double NOT NULL,
  `start-mileage` double NOT NULL,
  PRIMARY KEY (`id-car`),
  UNIQUE KEY `idCar_UNIQUE` (`id-car`)
) ENGINE=InnoDB AUTO_INCREMENT=64 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


DROP TABLE IF EXISTS `lists`;
CREATE TABLE `lists` (
  `id` int NOT NULL AUTO_INCREMENT,
  `number` int NOT NULL,
  `id-order` int DEFAULT NULL,
  `id-car` int NOT NULL,
  `start-mileage` double NOT NULL,
  `start-fuel` double NOT NULL,
  `end-mileage` double DEFAULT NULL,
  `end-fuel` double DEFAULT NULL,
  `refuel` double DEFAULT NULL,
  `done` tinyint NOT NULL,
  `start-date` date NOT NULL,
  `end-date` date NOT NULL,
  `route` varchar(45) NOT NULL,
  `goal` varchar(45) NOT NULL,
  `id-worker` int NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
  `id-order` int NOT NULL AUTO_INCREMENT,
  `order-date` date NOT NULL,
  `order-number` varchar(45) NOT NULL,
  `id-worker` int NOT NULL,
  `start-date` date NOT NULL,
  `end-date` date NOT NULL,
  `route` varchar(45) NOT NULL,
  `money` double NOT NULL,
  `goal` varchar(45) NOT NULL,
  `head` varchar(45) NOT NULL,
  PRIMARY KEY (`id-order`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `parameters`;
CREATE TABLE `parameters` (
  `idparameters` int NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `address` mediumtext NOT NULL,
  `code` int NOT NULL,
  `ceo` varchar(45) NOT NULL,
  `accountant` varchar(45) NOT NULL,
  `typeFull` varchar(45) NOT NULL,
  `typeShort` varchar(45) NOT NULL,
  PRIMARY KEY (`idparameters`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `positions`;
CREATE TABLE `positions` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nameN` varchar(45) NOT NULL,
  `nameR` varchar(45) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


DROP TABLE IF EXISTS `workers`;
CREATE TABLE `workers` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nameN` varchar(45) NOT NULL,
  `nameR` varchar(45) NOT NULL,
  `positionId` varchar(45) NOT NULL,
  `drivingLicence` varchar(45) DEFAULT NULL,
  `start-date` date NOT NULL,
  `start-order-number` varchar(45) NOT NULL,
  `end-date` date DEFAULT NULL,
  `end-order-number` varchar(45) DEFAULT NULL,
  `valid` tinyint NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
