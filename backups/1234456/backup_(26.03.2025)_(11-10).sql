-- MySQL dump 10.13  Distrib 8.0.40, for Win64 (x86_64)
--
-- Host: localhost    Database: 1234456
-- ------------------------------------------------------
-- Server version	8.0.40

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
  `number` mediumtext NOT NULL,
  `model` mediumtext NOT NULL,
  `fuel-type` mediumtext NOT NULL,
  `fuel-usage` double NOT NULL,
  `engine-volume` double NOT NULL,
  `start-date` date NOT NULL,
  `start-order-number` mediumtext NOT NULL,
  `end-date` date DEFAULT NULL,
  `end-order-number` mediumtext,
  `valid` tinyint NOT NULL,
  `start-fuel` double NOT NULL,
  `start-mileage` double NOT NULL,
  PRIMARY KEY (`id-car`),
  UNIQUE KEY `idCar_UNIQUE` (`id-car`)
) ENGINE=InnoDB AUTO_INCREMENT=69 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cars`
--

LOCK TABLES `cars` WRITE;
/*!40000 ALTER TABLE `cars` DISABLE KEYS */;
INSERT INTO `cars` VALUES (66,'КА2387ІС','Volkswagen CADDY','дизельне паливо',8,1968,'2024-11-07','-',NULL,NULL,1,50,134775),(68,'КА8546ВТ ','Volkswagen CADDY','дизельне паливо',8,1968,'2020-01-16','-',NULL,NULL,1,35,213975);
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
  `route` longtext NOT NULL,
  `goal` longtext NOT NULL,
  `id-worker` int NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lists`
--

LOCK TABLES `lists` WRITE;
/*!40000 ALTER TABLE `lists` DISABLE KEYS */;
INSERT INTO `lists` VALUES (29,1,8,66,134775,50,0,0,0,0,'2025-01-08','2025-01-09','Харків','поставки товару',16);
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
  `order-number` mediumtext NOT NULL,
  `id-worker` int NOT NULL,
  `start-date` date NOT NULL,
  `end-date` date NOT NULL,
  `route` longtext NOT NULL,
  `money` double NOT NULL,
  `goal` longtext NOT NULL,
  `head` mediumtext NOT NULL,
  PRIMARY KEY (`id-order`)
) ENGINE=InnoDB AUTO_INCREMENT=63 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
INSERT INTO `orders` VALUES (8,'2025-01-07','1-вд',16,'2025-01-08','2025-01-09','Харків',1200,'поставки товару','Геннадій ІГНАТЕНКО'),(9,'2025-01-07','2-вд',13,'2025-01-08','2025-01-09','Суми, Шостка, Конотоп, Згурівка, Глухів, Ромни, Буринь, Недригайлів ',1200,'поставки товару','Геннадій ІГНАТЕНКО'),(10,'2025-01-07','3-вд',16,'2025-01-13','2025-01-15','Дніпро, Кам’янське, Нікополь, Кривий Ріг, Запоріжжя',1200,'поставки товару','Геннадій ІГНАТЕНКО'),(11,'2025-01-10','4-вд',10,'2025-01-13','2025-01-14','Хмельницький, Бердичів ',1200,'поставки товару','Геннадій ІГНАТЕНКО'),(12,'2025-01-10','5-вд',13,'2025-01-13','2025-01-15','Харків, Балаклія, Полтава, Лубни, Бориспіль, Гадяч, Карлівка ',1200,'поставки товару','Геннадій ІГНАТЕНКО'),(13,'2025-01-15','6-вд',13,'2025-01-16','2025-01-16','Чернігів ',1200,'поставки товару','Геннадій ІГНАТЕНКО'),(14,'2025-01-17','7-вд',10,'2025-01-20','2025-01-22','Великополовецьке, Ставище, Плюти, Херсон, Біла Церква, Одеса, Миколаїв, Південноукраїнськ, Біла Церква, Ізмаїл ',1200,'поставки товару','Геннадій ІГНАТЕНКО'),(15,'2025-01-17','8-вд',15,'2025-01-20','2025-01-22','Великополовецьке, Ставище, Плюти, Херсон, Біла Церква, Одеса, Миколаїв, Південноукраїнськ, Біла Церква, Ізмаїл ',1200,'поставки товару','Геннадій ІГНАТЕНКО'),(16,'2025-01-17','9-вд',13,'2025-01-20','2025-01-21','Коростень, Житомир, Коростишів, Ірпінь, Станишівка, Бердичів, Андрушівка, Ворзель, Буча, Олевськ, Малин ',1200,'поставки товару','Геннадій ІГНАТЕНКО'),(17,'2025-01-17','10-вд',14,'2025-01-20','2025-01-21','Харків',1200,'поставки товару','Геннадій ІГНАТЕНКО'),(18,'2025-01-23','11-вд',13,'2025-01-16','2025-01-16','Чернігів ',1200,'поставки товару','Геннадій ІГНАТЕНКО'),(19,'2025-01-24','12-вд',14,'2025-01-27','2025-01-29','Кривий Ріг, Дніпро, Запоріжжя, Переяслав, Кам’янське ',1200,'поставки товару','Геннадій ІГНАТЕНКО'),(20,'2025-01-24','13-вд',10,'2025-01-27','2025-01-28','Хмельницький',1200,'поставки товару','Геннадій ІГНАТЕНКО'),(21,'2025-01-24','14-вд',13,'2025-01-27','2025-01-29','Харків, Зіньків, Полтава, Лубни ',1200,'поставки товару','Геннадій ІГНАТЕНКО'),(22,'2025-01-31','15-вд',14,'2025-02-03','2025-02-05','Суми, Охтирка, Ромни, Прилуки, Глухів, Згурівка, Буринь, Шостка, Тростянець, Недригайлів, Конотоп',1200,'поставки товару','Геннадій ІГНАТЕНКО'),(23,'2025-01-31','16-вд',13,'2025-02-03','2025-02-04','Біла Церква, Південноукраїнськ, Миколаїв, Умань, Тальне, Одеса   ',1200,'поставки товару','Геннадій ІГНАТЕНКО'),(24,'2025-01-31','17-вд',10,'2025-02-03','2025-02-04','Іванків, Житомир, Буча, Ворзель, Бердичів, Овруч ',1200,'поставки товару','Геннадій ІГНАТЕНКО'),(25,'2025-01-31','18-вд',15,'2025-02-03','2025-02-04','Іванків, Житомир, Буча, Ворзель, Бердичів, Овруч ',1200,'поставки товару','Геннадій ІГНАТЕНКО'),(26,'2025-02-04','19-вд',16,'2025-02-05','2025-02-06','Охтирка',1200,'поставки товару','Геннадій ІГНАТЕНКО'),(27,'2025-02-05','20-вд',10,'2025-02-06','2025-02-07','Хмельницький',1200,'поставки товару','Геннадій ІГНАТЕНКО'),(28,'2025-02-05','21-вд',13,'2025-02-06','2025-02-06','Чернігів, Халявинська сільська рада, 4-й кілометр Гомельського шосе, б.6',1200,'поставки товару','Геннадій ІГНАТЕНКО'),(29,'2025-02-07','23-вд',10,'2025-02-10','2025-02-12','Харків, Полтава, Валки, Яготин, Бориспіль, Лубни, Карлівка',1200,'поставки товару','Геннадій ІГНАТЕНКО'),(30,'2025-02-07','24-вд',15,'2025-02-10','2025-02-12','Харків, Полтава, Валки, Яготин, Бориспіль, Лубни, Карлівка ',1200,'поставки товару','Геннадій ІГНАТЕНКО'),(31,'2025-02-12','25-вд',14,'2025-02-13','2025-02-13','Чернігів',1200,'поставки товару','Геннадій ІГНАТЕНКО'),(32,'2025-02-14','26-вд',13,'2025-02-17','2025-02-18','Зарічани, Звягель, Житомир, Попільня, Андрушівка, Станишівка, Олевськ, Малин, Баранівка, Бердичів, Ірпінь, Буча, Лугини ',1200,'поставки товару','Геннадій ІГНАТЕНКО'),(33,'2025-02-14','27-вд',14,'2025-02-17','2025-02-19','Ставище, Обухів, Умань, Ізмаїл, Одеса, Миколаїв, Південноукраїнськ, Фастів, Біла Церква, Бориспіль, Тараща, Володарка ',1200,'поставки товару','Геннадій ІГНАТЕНКО'),(34,'2025-02-17','28-вд',10,'2025-02-18','2025-02-19','Харків',1200,'поставки товару','Геннадій ІГНАТЕНКО'),(35,'2025-02-17','29-вд',15,'2025-02-18','2025-02-19','Харків',1200,'поставки товару','Геннадій ІГНАТЕНКО'),(36,'2025-02-20','30-вд',14,'2025-02-21','2025-02-21','Суми, Ромни',1200,'поставки товару','Геннадій ІГНАТЕНКО'),(37,'2025-02-21','31-вд',14,'2025-02-24','2025-02-26','Дніпро, Кривий Ріг, Софіївка, Запоріжжя, Кропивницький, Лозова, Кам’янське, Шпола, Нікополь ',1200,'поставки товару','Геннадій ІГНАТЕНКО'),(38,'2025-02-21','32-вд',13,'2025-02-24','2025-02-26','Полтава, Харків, Мерефа, Балаклія, Лубни, Гадяч, Гребінка ',1200,'поставки товару','Геннадій ІГНАТЕНКО'),(39,'2025-02-21','33-вд',10,'2025-02-24','2025-02-25','Харків',1200,'поставки товару','Геннадій ІГНАТЕНКО'),(40,'2025-02-21','34-вд',15,'2025-02-24','2025-02-25','Харків',1200,'поставки товару','Геннадій ІГНАТЕНКО'),(41,'2025-02-25','35-вд',10,'2025-02-26','2025-02-27','Хмельницький',1200,'поставки товару','Геннадій ІГНАТЕНКО'),(42,'2025-02-28','36-вд',10,'2025-03-03','2025-03-05','Житомир, Романів, Коростишів, Народичі, Радомишль, Капітанівка, Іванків, Ірпінь, Ворзель, Олевськ, Буча, Бердичів, Баранівка, Малин, Андрушівка ',1200,'поставки товару','Геннадій ІГНАТЕНКО'),(43,'2025-02-28','37-вд',15,'2025-03-03','2025-03-05','Житомир, Романів, Коростишів, Народичі, Радомишль, Капітанівка, Іванків, Ірпінь, Ворзель, Олевськ, Буча, Бердичів, Баранівка, Малин, Андрушівка ',1200,'поставки товару','Геннадій ІГНАТЕНКО'),(44,'2025-02-28','38-вд',16,'2025-02-03','2025-02-03','Чернігів ',1200,'поставки товару','Геннадій ІГНАТЕНКО'),(45,'2025-02-28','39-вд',11,'2025-02-03','2025-02-03','Чернігів ',1200,'поставки товару','Геннадій ІГНАТЕНКО'),(46,'2025-02-28','40-вд',13,'2025-03-03','2025-03-05','Біла Церква, Ставище, Тараща, Тальне, Південноукраїнськ, Одеса, Фастів, Бориспіль, Обухів ',1200,'поставки товару','Геннадій ІГНАТЕНКО'),(47,'2025-02-28','41-вд',14,'2025-03-03','2025-03-05','Біла Церква, Ставище, Тараща, Тальне, Південноукраїнськ, Одеса, Фастів, Бориспіль, Обухів ',1200,'поставки товару','Геннадій ІГНАТЕНКО'),(48,'2025-03-05','42-вд',16,'2025-03-06','2025-03-06','Ромни',1200,'поставки товару','Геннадій ІГНАТЕНКО'),(49,'2025-03-05','43-вд',10,'2025-03-06','2025-03-07','Вінниця, Хмельницький ',1200,'поставки товару','Геннадій ІГНАТЕНКО'),(50,'2025-03-07','44-вд',16,'2025-03-10','2025-03-12','Кривий Ріг, Дніпро, Бориспіль, Запоріжжя, Кам’янське ',1200,'поставки товару','Геннадій ІГНАТЕНКО'),(51,'2025-03-07','45-вд',12,'2025-03-10','2025-03-11','Кам’янка-Бузька ',1200,'поставки товару','Геннадій ІГНАТЕНКО'),(52,'2025-03-07','46-вд',13,'2025-03-10','2025-03-12','Харків, Зіньків, Яготин, Бориспіль, Берестин, Полтава, Лубни, Карлівка',1200,'поставки товару','Геннадій ІГНАТЕНКО'),(53,'2025-03-12','47-вд',14,'2025-03-13','2025-03-13','Ніжин, Ріпки, Козелець, Чернігів, Суми  ',1200,'поставки товару','Геннадій ІГНАТЕНКО'),(54,'2025-03-12','48-вд',13,'2025-03-13','2025-03-14','Харків',1200,'поставки товару','Геннадій ІГНАТЕНКО'),(55,'2025-03-14','49-вд',13,'2025-03-17','2025-03-19','Зарічани, Житомир, Коростень, Андрушівка, Овруч, Станишівка, Звягель, Капітанівка, Ірпінь, Баранівка, Малин, Ємільчине, Черняхів, Бердичів, Вишгород ',1200,'поставки товару','Геннадій ІГНАТЕНКО'),(56,'2025-03-14','50-вд',14,'2025-03-17','2025-03-19','Боярка, Умань, Великополовецьке, Одеса, Фастів, Миколаїв, Бориспіль, Біла Церква',1200,'поставки товару','Геннадій ІГНАТЕНКО'),(57,'2025-03-14','51-вд',16,'2025-03-17','2025-03-17','Вінниця',1200,'поставки товару','Геннадій ІГНАТЕНКО'),(58,'2025-03-14','52-вд',11,'2025-03-17','2025-03-17','Вінниця',1200,'поставки товару','Геннадій ІГНАТЕНКО'),(59,'2025-03-18','53-вд',16,'2025-03-19','2025-03-19','Чернігів',1200,'поставки товару','Геннадій ІГНАТЕНКО'),(60,'2025-03-18','54-вд',11,'2025-03-19','2025-03-19','Чернігів',1200,'поставки товару','Геннадій ІГНАТЕНКО'),(61,'2025-03-20','55',9,'2025-03-21','2025-03-22','56789iuytr',777,'7 hr6','Геннадій ІГНАТЕНКО'),(62,'2025-03-20','666',9,'2025-03-28','2025-04-05','456e',46,'34567','Геннадій ІГНАТЕНКО');
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
  `name` mediumtext NOT NULL,
  `address` mediumtext NOT NULL,
  `code` int NOT NULL,
  `ceo` mediumtext NOT NULL,
  `accountant` mediumtext NOT NULL,
  `typeFull` longtext NOT NULL,
  `typeShort` mediumtext NOT NULL,
  PRIMARY KEY (`idparameters`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `parameters`
--

LOCK TABLES `parameters` WRITE;
/*!40000 ALTER TABLE `parameters` DISABLE KEYS */;
INSERT INTO `parameters` VALUES (3,'ДІАЛІЗ МЕДИК','01015 м.Київ, вул. Лаврська, 16',40477029,'Геннадій ІГНАТЕНКО','Світлана КОНДРАЧУК','Товариство з обмеженою відповідальністю','ТОВ');
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
  `nameN` longtext NOT NULL,
  `nameR` longtext NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `positions`
--

LOCK TABLES `positions` WRITE;
/*!40000 ALTER TABLE `positions` DISABLE KEYS */;
INSERT INTO `positions` VALUES (15,'експедитор','експедитора'),(16,'водій','водія');
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
) ENGINE=InnoDB AUTO_INCREMENT=64 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reports`
--

LOCK TABLES `reports` WRITE;
/*!40000 ALTER TABLE `reports` DISABLE KEYS */;
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
  `nameN` mediumtext NOT NULL,
  `nameR` mediumtext NOT NULL,
  `positionId` int NOT NULL,
  `drivingLicence` mediumtext,
  `start-date` date NOT NULL,
  `start-order-number` mediumtext NOT NULL,
  `end-date` date DEFAULT NULL,
  `end-order-number` mediumtext,
  `valid` tinyint NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `workers`
--

LOCK TABLES `workers` WRITE;
/*!40000 ALTER TABLE `workers` DISABLE KEYS */;
INSERT INTO `workers` VALUES (9,'Подлесний Ігор Олесандрович','Подлесного Ігора Олександровича',16,'КВЕ387891','2020-01-14','-',NULL,NULL,1),(10,'Дуброва Олександр Миколайович','Дуброви Олександра Миколайовича',16,'КІА471771','2017-10-01','-',NULL,NULL,1),(11,'Драч Вадим Петрович','Драча Вадима Петровича',16,'ВВТ213354','2018-10-01','-',NULL,NULL,1),(12,'Щербина Сергій Володимирович','Щербини Сергія Володимировича',16,'ААВ419828','2024-08-13','36-к ',NULL,NULL,1),(13,'Дегтянніков Володимир Миколайович','Дегтяннікова Володимира Миколайовича',15,'-','2020-07-01','-',NULL,NULL,1),(14,'Кірін Вадим Костянтинович','Кіріна Вадима Костянтиновича',15,'-','2023-04-06','12-к ',NULL,NULL,1),(15,'Пікалюк Олексій Олександрович','Пікалюка Олексія Олександровича',15,'-','2018-06-27','-',NULL,NULL,1),(16,'Биховський Михайло Володимирович','Биховського Михайла Володимировича',15,'-','2024-01-12','06-к ',NULL,NULL,1);
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

-- Dump completed on 2025-03-26 11:10:06
