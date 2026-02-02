-- Використання основної бази даних
USE company_db;

DELIMITER ;;
CREATE PROCEDURE `AddCar`(
    IN p_number VARCHAR(45),
    IN p_model VARCHAR(45),
    IN p_fuel_type VARCHAR(45),
    IN p_fuel_usage DOUBLE,
    IN p_engine_volume DOUBLE,
    IN p_start_date DATE,
    IN p_start_order_number VARCHAR(45),
    IN p_start_fuel DOUBLE,
    IN p_start_mileage DOUBLE
)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM cars
        WHERE number = p_number AND valid = TRUE
    ) THEN
        INSERT INTO cars (
            number,
            model,
            `fuel-type`,
            `fuel-usage`,
            `engine-volume`,
            `start-date`,
            `start-order-number`,
            valid,
            `start-fuel`,
            `start-mileage`
        ) VALUES (
            p_number,
            p_model,
            p_fuel_type,
            p_fuel_usage,
            p_engine_volume,
            p_start_date,
            p_start_order_number,
            TRUE,
            p_start_fuel,
            p_start_mileage
        );
    ELSE
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'A valid car with this number already exists.';
    END IF;
END ;;
DELIMITER ;

DELIMITER ;;
CREATE PROCEDURE `AddList`(
    IN p_id_car INT,
    IN p_id_order INT,
    IN p_end_mileage DOUBLE,
    IN p_end_fuel DOUBLE,
    IN p_refuel DOUBLE,
    IN p_start_date DATE,
    IN p_end_date DATE,
    IN p_route VARCHAR(45),
    IN p_goal VARCHAR(45),
    IN p_id_worker INT
)
BEGIN
    DECLARE max_number INT;
    DECLARE new_number INT;
    DECLARE prev_mileage DOUBLE;
    DECLARE prev_fuel DOUBLE;
    DECLARE car_start_mileage DOUBLE;
    DECLARE car_start_fuel DOUBLE;
    
    SELECT `start-mileage`, `start-fuel` INTO car_start_mileage, car_start_fuel
    FROM cars
    WHERE `id-car` = p_id_car;

    IF car_start_mileage IS NULL THEN
        SET car_start_mileage = 0;
    END IF;
    
    IF car_start_fuel IS NULL THEN
        SET car_start_fuel = 0;
    END IF;

    SELECT MAX(number) INTO max_number 
    FROM lists 
    WHERE YEAR(`start-date`) = YEAR(p_start_date);

    IF max_number IS NULL THEN
        SET new_number = 1;
    ELSE
        SET new_number = max_number + 1;
    END IF;

    SELECT `end-mileage`, `end-fuel` INTO prev_mileage, prev_fuel
    FROM lists
    WHERE `id-car` = p_id_car
    ORDER BY `start-date` DESC, id DESC
    LIMIT 1;

    IF prev_mileage IS NULL THEN
        SET prev_mileage = car_start_mileage;
        SET prev_fuel = car_start_fuel;
    END IF;

    INSERT INTO lists (number, `id-car`, `id-order`, `start-mileage`, `start-fuel`, `done`, `end-mileage`, `end-fuel`, `refuel`, `start-date`, `end-date`, `route`, `goal`, `id-worker`)
    VALUES (new_number, p_id_car, p_id_order, prev_mileage, prev_fuel, false, p_end_mileage, p_end_fuel, p_refuel, p_start_date, p_end_date, p_route, p_goal, p_id_worker);
    
END ;;
DELIMITER ;

DELIMITER ;;
CREATE PROCEDURE `AddOrder`(
    IN p_order_date DATE,
    IN p_order_number VARCHAR(45),
    IN p_id_worker INT,
    IN p_start_date DATE,
    IN p_end_date DATE,
    IN p_route VARCHAR(45),
    IN p_money DOUBLE,
    IN p_goal VARCHAR(45),
    IN p_head VARCHAR(45)
)
BEGIN
    INSERT INTO `orders` (
        `order-date`,
        `order-number`,
        `id-worker`,
        `start-date`,
        `end-date`,
        `route`,
        `money`,
        `goal`,
        `head`
    ) VALUES (
        p_order_date,
        p_order_number,
        p_id_worker,
        p_start_date,
        p_end_date,
        p_route,
        p_money,
        p_goal,
        p_head
    );
END ;;
DELIMITER ;

DELIMITER ;;
CREATE PROCEDURE `AddPosition`(IN p_nameN VARCHAR(45), IN p_nameR VARCHAR(45))
BEGIN
    IF EXISTS (SELECT 1 FROM positions WHERE nameN = p_nameN OR nameR = p_nameR) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Position with the same nameN or nameR already exists';
    ELSE
        INSERT INTO positions (nameN, nameR) VALUES (p_nameN, p_nameR);
    END IF;
END ;;
DELIMITER ;

DELIMITER ;;
CREATE PROCEDURE `AddWorker`(
	IN p_nameN VARCHAR(45), 
    IN p_nameR VARCHAR(45), 
    IN p_positionId VARCHAR(45), 
    IN p_drivingLicence VARCHAR(45), 
    IN p_start_date DATE, 
    IN p_start_order_number VARCHAR(45)
)
BEGIN
	IF EXISTS (SELECT 1 FROM workers WHERE nameN = p_nameN AND valid = 1) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'A valid worker with the same name already exists';
	ELSE
        INSERT INTO workers (
            nameN, nameR, positionId, drivingLicence, 
            `start-date`, `start-order-number`, valid
        ) VALUES (
            p_nameN, p_nameR, p_positionId, p_drivingLicence, 
            p_start_date, p_start_order_number, 1
        );
    END IF;
END ;;
DELIMITER ;

DELIMITER ;;
CREATE PROCEDURE `getFreeCars`()
BEGIN
select *
from cars
where cars.valid = true
and cars.`id-car` not in (
	select lists.`id-car`
    from lists
    where lists.done = false
);
END ;;
DELIMITER ;

DELIMITER ;;
CREATE PROCEDURE `getFreeOrders`()
BEGIN
SELECT * 
FROM orders 
WHERE `id-order` NOT IN (SELECT DISTINCT `id-order` FROM lists);
END ;;
DELIMITER ;

DELIMITER ;;
CREATE PROCEDURE `getFreeWorkers`()
BEGIN
select *
from workers
JOIN positions ON workers.positionId = positions.id
where workers.valid = true
and workers.id not in (
	select lists.`id-worker`
    from lists
    where lists.done = false
);
END ;;
DELIMITER ;

DELIMITER ;;
CREATE PROCEDURE `getListsFromCarNumber`(
    IN car_number VARCHAR(45),
    IN start_date DATE,
    IN end_date DATE
)
BEGIN
    SELECT l.*
    FROM lists l
    JOIN cars c ON l.`id-car` = c.`id-car`
    WHERE c.number = car_number 
      AND l.done = TRUE
      AND l.`end-date` BETWEEN start_date AND end_date;
END ;;
DELIMITER ;

DELIMITER ;;
CREATE PROCEDURE `getUniqueCarsNumbers`()
BEGIN
    SELECT number, MAX(model) AS model
    FROM cars
    GROUP BY number;
END ;;
DELIMITER ;

DELIMITER ;;
CREATE PROCEDURE `getUniqueWorkerNames`()
BEGIN
SELECT DISTINCT NameN FROM workers;
END ;;
DELIMITER ;

DELIMITER ;;
CREATE PROCEDURE `getWorkerPositionNameN`(IN workerId INT)
BEGIN
SELECT positions.nameN AS PositionName
FROM workers 
JOIN positions ON workers.positionId = positions.id
where workers.id = workerId;
END ;;
DELIMITER ;

DELIMITER ;;
CREATE PROCEDURE `getWorkerPositionNameR`(IN workerId INT)
BEGIN
SELECT positions.nameR AS PositionName
FROM workers 
JOIN positions ON workers.positionId = positions.id
where workers.id = workerId;
END ;;
DELIMITER ;

DELIMITER ;;
CREATE PROCEDURE `RemoveCar`(
    IN p_id INT,
    IN p_end_date DATE,
    IN p_end_order_number VARCHAR(45)
)
BEGIN
    UPDATE cars
    SET `end-date` = p_end_date,
        `end-order-number` = p_end_order_number,
        valid = FALSE
    WHERE `id-car` = p_id;
END ;;
DELIMITER ;

DELIMITER ;;
CREATE PROCEDURE `RemovePosition`(IN p_id INT)
BEGIN
    DELETE FROM positions WHERE id = p_id;
END ;;
DELIMITER ;

DELIMITER ;;
CREATE PROCEDURE `RemoveWorker`(
	IN p_id INT,
    IN p_end_date DATE,
    IN p_end_order_number VARCHAR(45)
)
BEGIN
UPDATE workers
    SET `end-date` = p_end_date,
        `end-order-number` = p_end_order_number,
        valid = FALSE
    WHERE `id` = p_id;
END ;;
DELIMITER ;
