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
