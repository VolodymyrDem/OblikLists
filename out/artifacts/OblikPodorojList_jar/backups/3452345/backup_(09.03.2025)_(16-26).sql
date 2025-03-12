-- MySQL dump 10.13  Distrib 8.0.41, for Win64 (x86_64)
--
-- Host: localhost    Database: 3452345
-- ------------------------------------------------------
-- Server version	8.0.41

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `cars`
--

DROP TABLE IF EXISTS `cars`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=66 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cars`
--

LOCK TABLES `cars` WRITE;
/*!40000 ALTER TABLE `cars` DISABLE KEYS */;
INSERT INTO `cars` VALUES (64,'АІ1772СЕ','Kia sorento','дизель',8.9,2.6,'2025-02-24','40-н',NULL,NULL,1,12,145990),(65,'СК5671УР','Dodge ram','А95',22,4.6,'2025-03-03','412-з',NULL,NULL,1,34,1735980);
/*!40000 ALTER TABLE `cars` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lists`
--

DROP TABLE IF EXISTS `lists`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lists`
--

LOCK TABLES `lists` WRITE;
/*!40000 ALTER TABLE `lists` DISABLE KEYS */;
INSERT INTO `lists` VALUES (25,1,8,64,145990,12,146200,60,90,1,'2025-03-09','2025-03-09','м.Київ, м.Одеса','перевезення товару',9),(26,2,-1,64,146200,60,0,0,0,0,'2025-03-29','2025-03-30','Київ','перевезення сина',9),(27,3,9,65,1735980,34,1735990,40,12,1,'2025-03-09','2025-03-09','м.Київ, м.Одеса','перевезення товару',10);
/*!40000 ALTER TABLE `lists` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
INSERT INTO `orders` VALUES (8,'2025-03-08','4-к',9,'2025-03-09','2025-03-09','м.Київ, м.Одеса',1200,'перевезення товару','Гіга Степан Олегович'),(9,'2025-03-08','56-з',10,'2025-03-09','2025-03-09','м.Київ, м.Одеса',1200,'перевезення товару','Гіга Степан Олегович');
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `parameters`
--

DROP TABLE IF EXISTS `parameters`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `parameters`
--

LOCK TABLES `parameters` WRITE;
/*!40000 ALTER TABLE `parameters` DISABLE KEYS */;
INSERT INTO `parameters` VALUES (3,'Діаліз Медик','Київ Амосова',3452345,'Гіга Степан Олегович','Доместос Віктор Фараонович','ТОВАРИСТВО З ОБМЕЖЕНОЮ ВІДПОВІДАЛЬНІСТЮ','ТОВ');
/*!40000 ALTER TABLE `parameters` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `positions`
--

DROP TABLE IF EXISTS `positions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `positions` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nameN` varchar(45) NOT NULL,
  `nameR` varchar(45) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `positions`
--

LOCK TABLES `positions` WRITE;
/*!40000 ALTER TABLE `positions` DISABLE KEYS */;
INSERT INTO `positions` VALUES (15,'водій','водія'),(16,'експедитор','експедитора');
/*!40000 ALTER TABLE `positions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reports`
--

DROP TABLE IF EXISTS `reports`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reports` (
  `id` int NOT NULL AUTO_INCREMENT,
  `id-order` int NOT NULL,
  `comments` longtext,
  `date` date NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=65 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reports`
--

LOCK TABLES `reports` WRITE;
/*!40000 ALTER TABLE `reports` DISABLE KEYS */;
INSERT INTO `reports` VALUES (64,8,'','2025-03-09');
/*!40000 ALTER TABLE `reports` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `workers`
--

DROP TABLE IF EXISTS `workers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `workers`
--

LOCK TABLES `workers` WRITE;
/*!40000 ALTER TABLE `workers` DISABLE KEYS */;
INSERT INTO `workers` VALUES (9,'Демянчук Олег Миколайович','Демянчука Олега Миколайовича','15','РО-453756','2025-03-01','456-а',NULL,NULL,1),(10,'Кривий Віктор Степанович','Кривого Віктора Степановича','16','АК-4544','2024-03-08','459-г',NULL,NULL,1);
/*!40000 ALTER TABLE `workers` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-03-09 16:26:49
