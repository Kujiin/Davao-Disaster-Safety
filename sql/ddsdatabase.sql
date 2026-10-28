-- DDS: import this entire file in MySQL Workbench before running Java.
-- Creates ddsdatabase, all nine InnoDB tables, and fictional demo data.
-- No server software is bundled or started by Java.
-- Intended for MySQL 8.0.16+ / 8.4. Import as a user allowed to CREATE DATABASE.
-- No DROP/TRUNCATE statements. Existing primary-key rows are not replaced.
CREATE DATABASE IF NOT EXISTS ddsdatabase CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ddsdatabase;
CREATE TABLE IF NOT EXISTS schema_info (setting_key VARCHAR(80) PRIMARY KEY, setting_value VARCHAR(255) NOT NULL) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS families (
 family_id INT AUTO_INCREMENT PRIMARY KEY,
 family_code VARCHAR(24) NOT NULL UNIQUE,
 representative VARCHAR(120) NOT NULL,
 member_count INT NOT NULL CHECK(member_count BETWEEN 1 AND 100),
 barangay VARCHAR(100) NOT NULL,
 street VARCHAR(180) NOT NULL,
 contact VARCHAR(30) NOT NULL DEFAULT '',
 latitude DECIMAL(10,7) NULL,
 longitude DECIMAL(10,7) NULL,
 location_note VARCHAR(180) NOT NULL DEFAULT 'Approximate location',
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS users (
 user_id INT AUTO_INCREMENT PRIMARY KEY,
 username VARCHAR(60) NOT NULL UNIQUE,
 display_name VARCHAR(120) NOT NULL,
 password_hash VARCHAR(600) NOT NULL,
 role ENUM('ADMIN','MEMBER') NOT NULL,
 family_id INT NULL,
 active BOOLEAN NOT NULL DEFAULT TRUE,
 FOREIGN KEY(family_id) REFERENCES families(family_id)
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS disasters (
 disaster_id INT AUTO_INCREMENT PRIMARY KEY,
 name VARCHAR(150) NOT NULL,
 disaster_type VARCHAR(40) NOT NULL,
 location VARCHAR(150) NOT NULL,
 event_date DATE NOT NULL,
 notes VARCHAR(500) NOT NULL DEFAULT ''
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS response_rounds (
 round_id INT AUTO_INCREMENT PRIMARY KEY,
 disaster_id INT NOT NULL,
 round_name VARCHAR(80) NOT NULL,
 FOREIGN KEY(disaster_id) REFERENCES disasters(disaster_id),
 UNIQUE(disaster_id,round_name)
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS relief_items (
 item_id INT AUTO_INCREMENT PRIMARY KEY,
 item_name VARCHAR(100) NOT NULL UNIQUE,
 category VARCHAR(60) NOT NULL,
 unit VARCHAR(30) NOT NULL,
 quantity INT NOT NULL DEFAULT 0 CHECK(quantity >= 0),
 low_stock_threshold INT NOT NULL DEFAULT 10 CHECK(low_stock_threshold >= 0)
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS distributions (
 distribution_id BIGINT AUTO_INCREMENT PRIMARY KEY,
 family_id INT NOT NULL,
 item_id INT NOT NULL,
 round_id INT NOT NULL,
 quantity INT NOT NULL CHECK(quantity > 0),
 staff_id INT NOT NULL,
 notes VARCHAR(500) NOT NULL DEFAULT '',
 distributed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 FOREIGN KEY(family_id) REFERENCES families(family_id),
 FOREIGN KEY(item_id) REFERENCES relief_items(item_id),
 FOREIGN KEY(round_id) REFERENCES response_rounds(round_id),
 FOREIGN KEY(staff_id) REFERENCES users(user_id),
 UNIQUE KEY no_duplicate_assistance(family_id,item_id,round_id),
 INDEX by_round(round_id), INDEX by_date(distributed_at)
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS stock_movements (
 movement_id BIGINT AUTO_INCREMENT PRIMARY KEY,
 item_id INT NOT NULL,
 quantity_change INT NOT NULL,
 reason VARCHAR(500) NOT NULL,
 staff_id INT NOT NULL,
 distribution_id BIGINT NULL,
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 FOREIGN KEY(item_id) REFERENCES relief_items(item_id),
 FOREIGN KEY(staff_id) REFERENCES users(user_id),
 FOREIGN KEY(distribution_id) REFERENCES distributions(distribution_id)
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS audit_logs (
 audit_id BIGINT AUTO_INCREMENT PRIMARY KEY,
 user_id INT NOT NULL,
 action VARCHAR(80) NOT NULL,
 details VARCHAR(600) NOT NULL,
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 FOREIGN KEY(user_id) REFERENCES users(user_id)
) ENGINE=InnoDB;

-- Seed records use fixed IDs and are for a fresh classroom database.
-- Do not import over an unrelated existing database with this name.
SET @DDS_OLD_FK_CHECKS=@@FOREIGN_KEY_CHECKS;
SET FOREIGN_KEY_CHECKS=0;
START TRANSACTION;
INSERT IGNORE INTO `audit_logs` (`audit_id`, `user_id`, `action`, `details`, `created_at`) VALUES (1,1,'SETUP','Created clearly fictional demonstration data','2026-10-22 17:29:17');
INSERT IGNORE INTO `disasters` (`disaster_id`, `name`, `disaster_type`, `location`, `event_date`, `notes`) VALUES (1,'Demo flood response','Flood','Matina Crossing, Davao City','2026-10-09','Classroom demonstration only. This is not a report of an actual disaster.');
INSERT IGNORE INTO `distributions` (`distribution_id`, `family_id`, `item_id`, `round_id`, `quantity`, `staff_id`, `notes`, `distributed_at`) VALUES (1,1,1,1,1,1,'Fictional demonstration record','2026-10-22 17:29:17');
INSERT IGNORE INTO `distributions` (`distribution_id`, `family_id`, `item_id`, `round_id`, `quantity`, `staff_id`, `notes`, `distributed_at`) VALUES (2,2,1,1,1,1,'Fictional demonstration record','2026-10-22 17:29:17');
INSERT IGNORE INTO `distributions` (`distribution_id`, `family_id`, `item_id`, `round_id`, `quantity`, `staff_id`, `notes`, `distributed_at`) VALUES (3,3,1,1,1,1,'Fictional demonstration record','2026-10-22 17:29:17');
INSERT IGNORE INTO `distributions` (`distribution_id`, `family_id`, `item_id`, `round_id`, `quantity`, `staff_id`, `notes`, `distributed_at`) VALUES (4,4,1,1,1,1,'Fictional demonstration record','2026-10-22 17:29:17');
INSERT IGNORE INTO `distributions` (`distribution_id`, `family_id`, `item_id`, `round_id`, `quantity`, `staff_id`, `notes`, `distributed_at`) VALUES (5,5,1,1,1,1,'Fictional demonstration record','2026-10-22 17:29:17');
INSERT IGNORE INTO `distributions` (`distribution_id`, `family_id`, `item_id`, `round_id`, `quantity`, `staff_id`, `notes`, `distributed_at`) VALUES (6,6,1,1,1,1,'Fictional demonstration record','2026-10-22 17:29:17');
INSERT IGNORE INTO `distributions` (`distribution_id`, `family_id`, `item_id`, `round_id`, `quantity`, `staff_id`, `notes`, `distributed_at`) VALUES (7,7,1,1,1,1,'Fictional demonstration record','2026-10-22 17:29:17');
INSERT IGNORE INTO `distributions` (`distribution_id`, `family_id`, `item_id`, `round_id`, `quantity`, `staff_id`, `notes`, `distributed_at`) VALUES (8,8,1,1,1,1,'Fictional demonstration record','2026-10-22 17:29:17');
INSERT IGNORE INTO `distributions` (`distribution_id`, `family_id`, `item_id`, `round_id`, `quantity`, `staff_id`, `notes`, `distributed_at`) VALUES (9,9,1,1,1,1,'Fictional demonstration record','2026-10-22 17:29:17');
INSERT IGNORE INTO `distributions` (`distribution_id`, `family_id`, `item_id`, `round_id`, `quantity`, `staff_id`, `notes`, `distributed_at`) VALUES (10,10,1,1,1,1,'Fictional demonstration record','2026-10-22 17:29:17');
INSERT IGNORE INTO `distributions` (`distribution_id`, `family_id`, `item_id`, `round_id`, `quantity`, `staff_id`, `notes`, `distributed_at`) VALUES (11,11,1,1,1,1,'Fictional demonstration record','2026-10-22 17:29:17');
INSERT IGNORE INTO `distributions` (`distribution_id`, `family_id`, `item_id`, `round_id`, `quantity`, `staff_id`, `notes`, `distributed_at`) VALUES (12,12,1,1,1,1,'Fictional demonstration record','2026-10-22 17:29:17');
INSERT IGNORE INTO `distributions` (`distribution_id`, `family_id`, `item_id`, `round_id`, `quantity`, `staff_id`, `notes`, `distributed_at`) VALUES (13,13,1,1,1,1,'Fictional demonstration record','2026-10-22 17:29:17');
INSERT IGNORE INTO `distributions` (`distribution_id`, `family_id`, `item_id`, `round_id`, `quantity`, `staff_id`, `notes`, `distributed_at`) VALUES (14,14,1,1,1,1,'Fictional demonstration record','2026-10-22 17:29:17');
INSERT IGNORE INTO `distributions` (`distribution_id`, `family_id`, `item_id`, `round_id`, `quantity`, `staff_id`, `notes`, `distributed_at`) VALUES (15,15,1,1,1,1,'Fictional demonstration record','2026-10-22 17:29:17');
INSERT IGNORE INTO `distributions` (`distribution_id`, `family_id`, `item_id`, `round_id`, `quantity`, `staff_id`, `notes`, `distributed_at`) VALUES (16,16,1,1,1,1,'Fictional demonstration record','2026-10-22 17:29:17');
INSERT IGNORE INTO `distributions` (`distribution_id`, `family_id`, `item_id`, `round_id`, `quantity`, `staff_id`, `notes`, `distributed_at`) VALUES (17,17,1,1,1,1,'Fictional demonstration record','2026-10-22 17:29:17');
INSERT IGNORE INTO `distributions` (`distribution_id`, `family_id`, `item_id`, `round_id`, `quantity`, `staff_id`, `notes`, `distributed_at`) VALUES (18,18,1,1,1,1,'Fictional demonstration record','2026-10-22 17:29:17');
INSERT IGNORE INTO `families` (`family_id`, `family_code`, `representative`, `member_count`, `barangay`, `street`, `contact`, `latitude`, `longitude`, `location_note`, `created_at`, `updated_at`) VALUES (1,'F-001','Ana Santos',3,'Matina Crossing','MacArthur Highway','',7.0490000,125.5740000,'Fictional demo household; approximate point, not a verified home address','2026-10-22 17:29:17','2026-10-22 17:29:17');
INSERT IGNORE INTO `families` (`family_id`, `family_code`, `representative`, `member_count`, `barangay`, `street`, `contact`, `latitude`, `longitude`, `location_note`, `created_at`, `updated_at`) VALUES (2,'F-002','Luis Reyes',4,'Matina Crossing','MacArthur Highway','',7.0500000,125.5740000,'Fictional demo household; approximate point, not a verified home address','2026-10-22 17:29:17','2026-10-22 17:29:17');
INSERT IGNORE INTO `families` (`family_id`, `family_code`, `representative`, `member_count`, `barangay`, `street`, `contact`, `latitude`, `longitude`, `location_note`, `created_at`, `updated_at`) VALUES (3,'F-003','Mia Cruz',5,'Matina Crossing','MacArthur Highway','',7.0510000,125.5740000,'Fictional demo household; approximate point, not a verified home address','2026-10-22 17:29:17','2026-10-22 17:29:17');
INSERT IGNORE INTO `families` (`family_id`, `family_code`, `representative`, `member_count`, `barangay`, `street`, `contact`, `latitude`, `longitude`, `location_note`, `created_at`, `updated_at`) VALUES (4,'F-004','Paolo Lim',6,'Matina Crossing','MacArthur Highway','',7.0520000,125.5740000,'Fictional demo household; approximate point, not a verified home address','2026-10-22 17:29:17','2026-10-22 17:29:17');
INSERT IGNORE INTO `families` (`family_id`, `family_code`, `representative`, `member_count`, `barangay`, `street`, `contact`, `latitude`, `longitude`, `location_note`, `created_at`, `updated_at`) VALUES (5,'F-005','Maria Dela Cruz',3,'Matina Crossing','MacArthur Highway','',7.0530000,125.5740000,'Fictional demo household; approximate point, not a verified home address','2026-10-22 17:29:17','2026-10-22 17:29:17');
INSERT IGNORE INTO `families` (`family_id`, `family_code`, `representative`, `member_count`, `barangay`, `street`, `contact`, `latitude`, `longitude`, `location_note`, `created_at`, `updated_at`) VALUES (6,'F-006','Jose Garcia',4,'Matina Crossing','MacArthur Highway','',7.0540000,125.5740000,'Fictional demo household; approximate point, not a verified home address','2026-10-22 17:29:17','2026-10-22 17:29:17');
INSERT IGNORE INTO `families` (`family_id`, `family_code`, `representative`, `member_count`, `barangay`, `street`, `contact`, `latitude`, `longitude`, `location_note`, `created_at`, `updated_at`) VALUES (7,'F-007','Elena Ramos',5,'Matina Crossing','MacArthur Highway','',7.0490000,125.5755000,'Fictional demo household; approximate point, not a verified home address','2026-10-22 17:29:17','2026-10-22 17:29:17');
INSERT IGNORE INTO `families` (`family_id`, `family_code`, `representative`, `member_count`, `barangay`, `street`, `contact`, `latitude`, `longitude`, `location_note`, `created_at`, `updated_at`) VALUES (8,'F-008','Carlo Mendoza',6,'Matina Crossing','MacArthur Highway','',7.0500000,125.5755000,'Fictional demo household; approximate point, not a verified home address','2026-10-22 17:29:17','2026-10-22 17:29:17');
INSERT IGNORE INTO `families` (`family_id`, `family_code`, `representative`, `member_count`, `barangay`, `street`, `contact`, `latitude`, `longitude`, `location_note`, `created_at`, `updated_at`) VALUES (9,'F-009','Rosa Aquino',3,'Matina Crossing','MacArthur Highway','',7.0510000,125.5755000,'Fictional demo household; approximate point, not a verified home address','2026-10-22 17:29:17','2026-10-22 17:29:17');
INSERT IGNORE INTO `families` (`family_id`, `family_code`, `representative`, `member_count`, `barangay`, `street`, `contact`, `latitude`, `longitude`, `location_note`, `created_at`, `updated_at`) VALUES (10,'F-010','Ben Torres',4,'Matina Crossing','MacArthur Highway','',7.0520000,125.5755000,'Fictional demo household; approximate point, not a verified home address','2026-10-22 17:29:17','2026-10-22 17:29:17');
INSERT IGNORE INTO `families` (`family_id`, `family_code`, `representative`, `member_count`, `barangay`, `street`, `contact`, `latitude`, `longitude`, `location_note`, `created_at`, `updated_at`) VALUES (11,'F-011','Liza Flores',5,'Matina Crossing','MacArthur Highway','',7.0530000,125.5755000,'Fictional demo household; approximate point, not a verified home address','2026-10-22 17:29:17','2026-10-22 17:29:17');
INSERT IGNORE INTO `families` (`family_id`, `family_code`, `representative`, `member_count`, `barangay`, `street`, `contact`, `latitude`, `longitude`, `location_note`, `created_at`, `updated_at`) VALUES (12,'F-012','Marco Villanueva',6,'Matina Crossing','MacArthur Highway','',7.0540000,125.5755000,'Fictional demo household; approximate point, not a verified home address','2026-10-22 17:29:17','2026-10-22 17:29:17');
INSERT IGNORE INTO `families` (`family_id`, `family_code`, `representative`, `member_count`, `barangay`, `street`, `contact`, `latitude`, `longitude`, `location_note`, `created_at`, `updated_at`) VALUES (13,'F-013','Celia Castillo',3,'Matina Crossing','MacArthur Highway','',7.0490000,125.5770000,'Fictional demo household; approximate point, not a verified home address','2026-10-22 17:29:17','2026-10-22 17:29:17');
INSERT IGNORE INTO `families` (`family_id`, `family_code`, `representative`, `member_count`, `barangay`, `street`, `contact`, `latitude`, `longitude`, `location_note`, `created_at`, `updated_at`) VALUES (14,'F-014','Ramon Bautista',4,'Matina Crossing','MacArthur Highway','',7.0500000,125.5770000,'Fictional demo household; approximate point, not a verified home address','2026-10-22 17:29:17','2026-10-22 17:29:17');
INSERT IGNORE INTO `families` (`family_id`, `family_code`, `representative`, `member_count`, `barangay`, `street`, `contact`, `latitude`, `longitude`, `location_note`, `created_at`, `updated_at`) VALUES (15,'F-015','Nina Navarro',5,'Matina Crossing','MacArthur Highway','',7.0510000,125.5770000,'Fictional demo household; approximate point, not a verified home address','2026-10-22 17:29:17','2026-10-22 17:29:17');
INSERT IGNORE INTO `families` (`family_id`, `family_code`, `representative`, `member_count`, `barangay`, `street`, `contact`, `latitude`, `longitude`, `location_note`, `created_at`, `updated_at`) VALUES (16,'F-016','Leo Fernandez',6,'Matina Crossing','MacArthur Highway','',7.0520000,125.5770000,'Fictional demo household; approximate point, not a verified home address','2026-10-22 17:29:17','2026-10-22 17:29:17');
INSERT IGNORE INTO `families` (`family_id`, `family_code`, `representative`, `member_count`, `barangay`, `street`, `contact`, `latitude`, `longitude`, `location_note`, `created_at`, `updated_at`) VALUES (17,'F-017','Sara Diaz',3,'Matina Crossing','MacArthur Highway','',7.0530000,125.5770000,'Fictional demo household; approximate point, not a verified home address','2026-10-22 17:29:17','2026-10-22 17:29:17');
INSERT IGNORE INTO `families` (`family_id`, `family_code`, `representative`, `member_count`, `barangay`, `street`, `contact`, `latitude`, `longitude`, `location_note`, `created_at`, `updated_at`) VALUES (18,'F-018','Dan Morales',4,'Matina Crossing','MacArthur Highway','',7.0540000,125.5770000,'Fictional demo household; approximate point, not a verified home address','2026-10-22 17:29:17','2026-10-22 17:29:17');
INSERT IGNORE INTO `families` (`family_id`, `family_code`, `representative`, `member_count`, `barangay`, `street`, `contact`, `latitude`, `longitude`, `location_note`, `created_at`, `updated_at`) VALUES (19,'F-019','Grace Mercado',5,'Matina Crossing','MacArthur Highway','',7.0490000,125.5785000,'Fictional demo household; approximate point, not a verified home address','2026-10-22 17:29:17','2026-10-22 17:29:17');
INSERT IGNORE INTO `families` (`family_id`, `family_code`, `representative`, `member_count`, `barangay`, `street`, `contact`, `latitude`, `longitude`, `location_note`, `created_at`, `updated_at`) VALUES (20,'F-020','Noel Perez',6,'Matina Crossing','MacArthur Highway','',7.0500000,125.5785000,'Fictional demo household; approximate point, not a verified home address','2026-10-22 17:29:17','2026-10-22 17:29:17');
INSERT IGNORE INTO `families` (`family_id`, `family_code`, `representative`, `member_count`, `barangay`, `street`, `contact`, `latitude`, `longitude`, `location_note`, `created_at`, `updated_at`) VALUES (21,'F-021','Irene Rivera',3,'Matina Crossing','MacArthur Highway','',7.0510000,125.5785000,'Fictional demo household; approximate point, not a verified home address','2026-10-22 17:29:17','2026-10-22 17:29:17');
INSERT IGNORE INTO `families` (`family_id`, `family_code`, `representative`, `member_count`, `barangay`, `street`, `contact`, `latitude`, `longitude`, `location_note`, `created_at`, `updated_at`) VALUES (22,'F-022','Alex Gonzales',4,'Matina Crossing','MacArthur Highway','',7.0520000,125.5785000,'Fictional demo household; approximate point, not a verified home address','2026-10-22 17:29:17','2026-10-22 17:29:17');
INSERT IGNORE INTO `families` (`family_id`, `family_code`, `representative`, `member_count`, `barangay`, `street`, `contact`, `latitude`, `longitude`, `location_note`, `created_at`, `updated_at`) VALUES (23,'F-023','Mila Tan',5,'Matina Crossing','MacArthur Highway','',7.0530000,125.5785000,'Fictional demo household; approximate point, not a verified home address','2026-10-22 17:29:17','2026-10-22 17:29:17');
INSERT IGNORE INTO `families` (`family_id`, `family_code`, `representative`, `member_count`, `barangay`, `street`, `contact`, `latitude`, `longitude`, `location_note`, `created_at`, `updated_at`) VALUES (24,'F-024','Joel Salvador',6,'Matina Crossing','MacArthur Highway','',7.0540000,125.5785000,'Fictional demo household; approximate point, not a verified home address','2026-10-22 17:29:17','2026-10-22 17:29:17');
INSERT IGNORE INTO `relief_items` (`item_id`, `item_name`, `category`, `unit`, `quantity`, `low_stock_threshold`) VALUES (1,'Family food pack','Food','packs',120,20);
INSERT IGNORE INTO `relief_items` (`item_id`, `item_name`, `category`, `unit`, `quantity`, `low_stock_threshold`) VALUES (2,'Drinking water (6 L)','Water','bottles',80,15);
INSERT IGNORE INTO `relief_items` (`item_id`, `item_name`, `category`, `unit`, `quantity`, `low_stock_threshold`) VALUES (3,'Hygiene kit','Hygiene','kits',8,10);
INSERT IGNORE INTO `relief_items` (`item_id`, `item_name`, `category`, `unit`, `quantity`, `low_stock_threshold`) VALUES (4,'Sleeping mat','Shelter','mats',35,10);
INSERT IGNORE INTO `response_rounds` (`round_id`, `disaster_id`, `round_name`) VALUES (1,1,'Round 1');
INSERT IGNORE INTO `response_rounds` (`round_id`, `disaster_id`, `round_name`) VALUES (2,1,'Round 2');
INSERT IGNORE INTO `schema_info` (`setting_key`, `setting_value`) VALUES ('seeded','1');
INSERT IGNORE INTO `stock_movements` (`movement_id`, `item_id`, `quantity_change`, `reason`, `staff_id`, `distribution_id`, `created_at`) VALUES (1,1,138,'Opening demo balance',1,NULL,'2026-10-22 17:29:17');
INSERT IGNORE INTO `stock_movements` (`movement_id`, `item_id`, `quantity_change`, `reason`, `staff_id`, `distribution_id`, `created_at`) VALUES (2,2,80,'Opening demo balance',1,NULL,'2026-10-22 17:29:17');
INSERT IGNORE INTO `stock_movements` (`movement_id`, `item_id`, `quantity_change`, `reason`, `staff_id`, `distribution_id`, `created_at`) VALUES (3,3,8,'Opening demo balance',1,NULL,'2026-10-22 17:29:17');
INSERT IGNORE INTO `stock_movements` (`movement_id`, `item_id`, `quantity_change`, `reason`, `staff_id`, `distribution_id`, `created_at`) VALUES (4,4,35,'Opening demo balance',1,NULL,'2026-10-22 17:29:17');
INSERT IGNORE INTO `stock_movements` (`movement_id`, `item_id`, `quantity_change`, `reason`, `staff_id`, `distribution_id`, `created_at`) VALUES (5,1,-1,'Demo distribution',1,1,'2026-10-22 17:29:17');
INSERT IGNORE INTO `stock_movements` (`movement_id`, `item_id`, `quantity_change`, `reason`, `staff_id`, `distribution_id`, `created_at`) VALUES (6,1,-1,'Demo distribution',1,2,'2026-10-22 17:29:17');
INSERT IGNORE INTO `stock_movements` (`movement_id`, `item_id`, `quantity_change`, `reason`, `staff_id`, `distribution_id`, `created_at`) VALUES (7,1,-1,'Demo distribution',1,3,'2026-10-22 17:29:17');
INSERT IGNORE INTO `stock_movements` (`movement_id`, `item_id`, `quantity_change`, `reason`, `staff_id`, `distribution_id`, `created_at`) VALUES (8,1,-1,'Demo distribution',1,4,'2026-10-22 17:29:17');
INSERT IGNORE INTO `stock_movements` (`movement_id`, `item_id`, `quantity_change`, `reason`, `staff_id`, `distribution_id`, `created_at`) VALUES (9,1,-1,'Demo distribution',1,5,'2026-10-22 17:29:17');
INSERT IGNORE INTO `stock_movements` (`movement_id`, `item_id`, `quantity_change`, `reason`, `staff_id`, `distribution_id`, `created_at`) VALUES (10,1,-1,'Demo distribution',1,6,'2026-10-22 17:29:17');
INSERT IGNORE INTO `stock_movements` (`movement_id`, `item_id`, `quantity_change`, `reason`, `staff_id`, `distribution_id`, `created_at`) VALUES (11,1,-1,'Demo distribution',1,7,'2026-10-22 17:29:17');
INSERT IGNORE INTO `stock_movements` (`movement_id`, `item_id`, `quantity_change`, `reason`, `staff_id`, `distribution_id`, `created_at`) VALUES (12,1,-1,'Demo distribution',1,8,'2026-10-22 17:29:17');
INSERT IGNORE INTO `stock_movements` (`movement_id`, `item_id`, `quantity_change`, `reason`, `staff_id`, `distribution_id`, `created_at`) VALUES (13,1,-1,'Demo distribution',1,9,'2026-10-22 17:29:17');
INSERT IGNORE INTO `stock_movements` (`movement_id`, `item_id`, `quantity_change`, `reason`, `staff_id`, `distribution_id`, `created_at`) VALUES (14,1,-1,'Demo distribution',1,10,'2026-10-22 17:29:17');
INSERT IGNORE INTO `stock_movements` (`movement_id`, `item_id`, `quantity_change`, `reason`, `staff_id`, `distribution_id`, `created_at`) VALUES (15,1,-1,'Demo distribution',1,11,'2026-10-22 17:29:17');
INSERT IGNORE INTO `stock_movements` (`movement_id`, `item_id`, `quantity_change`, `reason`, `staff_id`, `distribution_id`, `created_at`) VALUES (16,1,-1,'Demo distribution',1,12,'2026-10-22 17:29:17');
INSERT IGNORE INTO `stock_movements` (`movement_id`, `item_id`, `quantity_change`, `reason`, `staff_id`, `distribution_id`, `created_at`) VALUES (17,1,-1,'Demo distribution',1,13,'2026-10-22 17:29:17');
INSERT IGNORE INTO `stock_movements` (`movement_id`, `item_id`, `quantity_change`, `reason`, `staff_id`, `distribution_id`, `created_at`) VALUES (18,1,-1,'Demo distribution',1,14,'2026-10-22 17:29:17');
INSERT IGNORE INTO `stock_movements` (`movement_id`, `item_id`, `quantity_change`, `reason`, `staff_id`, `distribution_id`, `created_at`) VALUES (19,1,-1,'Demo distribution',1,15,'2026-10-22 17:29:17');
INSERT IGNORE INTO `stock_movements` (`movement_id`, `item_id`, `quantity_change`, `reason`, `staff_id`, `distribution_id`, `created_at`) VALUES (20,1,-1,'Demo distribution',1,16,'2026-10-22 17:29:17');
INSERT IGNORE INTO `stock_movements` (`movement_id`, `item_id`, `quantity_change`, `reason`, `staff_id`, `distribution_id`, `created_at`) VALUES (21,1,-1,'Demo distribution',1,17,'2026-10-22 17:29:17');
INSERT IGNORE INTO `stock_movements` (`movement_id`, `item_id`, `quantity_change`, `reason`, `staff_id`, `distribution_id`, `created_at`) VALUES (22,1,-1,'Demo distribution',1,18,'2026-10-22 17:29:17');
INSERT IGNORE INTO `users` (`user_id`, `username`, `display_name`, `password_hash`, `role`, `family_id`, `active`) VALUES (1,'admin','Barangay staff','180000:9BnarEkKOPbEiqeDxICImA==:dv9E62dPucrsBI9Ora0QI7WzTf+GTMgD9JOlXBL9D/c=','ADMIN',NULL,1);
INSERT IGNORE INTO `users` (`user_id`, `username`, `display_name`, `password_hash`, `role`, `family_id`, `active`) VALUES (2,'member','Ana Santos','180000:88/t4WK1Ha3esZPQ9hKSbQ==:/30XoQZMRHAWi2CYsCW/bsxn2obicwrGedOD5nniF3c=','MEMBER',1,1);
COMMIT;
SET FOREIGN_KEY_CHECKS=@DDS_OLD_FK_CHECKS;
-- Demo accounts: admin / Admin@DDS2026; member / Member@DDS2026.
-- Passwords above are login credentials; stored values are salted hashes.
