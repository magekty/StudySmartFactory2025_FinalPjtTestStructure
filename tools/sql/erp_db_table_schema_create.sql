-- 데이터베이스 생성
CREATE DATABASE IF NOT EXISTS `erp_fd_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- erp_db 데이터베이스 사용
USE `erp_fd_db`;

-- tb_product (제품 마스터) 테이블 생성
CREATE TABLE `tb_product` (
  `product_id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `type` VARCHAR(20) NOT NULL,
  `unit` VARCHAR(20) NOT NULL,
  `description` VARCHAR(255) DEFAULT NULL,
  `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
  `deleted_at` DATETIME(6) DEFAULT NULL,
  `created_by` VARCHAR(50) DEFAULT NULL,
  `created_at` DATETIME(6) NOT NULL,
  `modified_by` VARCHAR(50) DEFAULT NULL,
  `modified_at` DATETIME(6) DEFAULT NULL,
  PRIMARY KEY (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- tb_material (자재 마스터) 테이블 생성
CREATE TABLE `tb_material` (
  `material_id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `unit` VARCHAR(20) NOT NULL,
  `cost` DECIMAL(10,2) NOT NULL,
  `supplier` VARCHAR(100) DEFAULT NULL,
  `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
  `deleted_at` DATETIME(6) DEFAULT NULL,
  `created_by` VARCHAR(50) DEFAULT NULL,
  `created_at` DATETIME(6) NOT NULL,
  `modified_by` VARCHAR(50) DEFAULT NULL,
  `modified_at` DATETIME(6) DEFAULT NULL,
  PRIMARY KEY (`material_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- tb_bom_header (BOM 최상위 정보) 테이블 생성
CREATE TABLE `tb_bom_header` (
  `bom_id` INT NOT NULL AUTO_INCREMENT,
  `product_id` INT NOT NULL,
  `version` INT DEFAULT NULL,
  `description` VARCHAR(255) DEFAULT NULL,
  `valid_from` DATETIME(6) DEFAULT NULL,
  `valid_to` DATETIME(6) DEFAULT NULL,
  `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
  `deleted_at` DATETIME(6) DEFAULT NULL,
  `created_by` VARCHAR(50) DEFAULT NULL,
  `created_at` DATETIME(6) NOT NULL,
  `modified_by` VARCHAR(50) DEFAULT NULL,
  `modified_at` DATETIME(6) DEFAULT NULL,
  PRIMARY KEY (`bom_id`),
  FOREIGN KEY (`product_id`) REFERENCES `tb_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- tb_bom_line (BOM 상세/계층 정보) 테이블 생성
CREATE TABLE `tb_bom_line` (
  `line_id` INT NOT NULL AUTO_INCREMENT,
  `bom_id` INT NOT NULL,
  `item_id` INT NOT NULL,
  `item_type` VARCHAR(20) NOT NULL,
  `quantity` DECIMAL(10,2) NOT NULL,
  `unit` VARCHAR(20) NOT NULL,
  `parent_line_id` INT DEFAULT NULL,
  `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
  `deleted_at` DATETIME(6) DEFAULT NULL,
  `created_by` VARCHAR(50) DEFAULT NULL,
  `created_at` DATETIME(6) NOT NULL,
  `modified_by` VARCHAR(50) DEFAULT NULL,
  `modified_at` DATETIME(6) DEFAULT NULL,
  PRIMARY KEY (`line_id`),
  FOREIGN KEY (`bom_id`) REFERENCES `tb_bom_header` (`bom_id`),
  FOREIGN KEY (`parent_line_id`) REFERENCES `tb_bom_line` (`line_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- tb_production_plan (생산 계획) 테이블 생성
CREATE TABLE `tb_production_plan` (
  `plan_id` INT NOT NULL AUTO_INCREMENT,
  `product_id` INT NOT NULL,
  `quantity` DECIMAL(10,2) NOT NULL,
  `planned_start` DATETIME(6) NOT NULL,
  `planned_end` DATETIME(6) NOT NULL,
  `status` VARCHAR(20) NOT NULL,
  `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
  `deleted_at` DATETIME(6) DEFAULT NULL,
  `created_by` VARCHAR(50) DEFAULT NULL,
  `created_at` DATETIME(6) NOT NULL,
  `modified_by` VARCHAR(50) DEFAULT NULL,
  `modified_at` DATETIME(6) DEFAULT NULL,
  PRIMARY KEY (`plan_id`),
  FOREIGN KEY (`product_id`) REFERENCES `tb_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- tb_cost (원가) 테이블 생성
CREATE TABLE `tb_cost` (
  `cost_id` INT NOT NULL AUTO_INCREMENT,
  `material_id` INT DEFAULT NULL,
  `product_id` INT NOT NULL,
  `cost_type` VARCHAR(50) NOT NULL,
  `amount` DECIMAL(15,2) NOT NULL,
  `currency` VARCHAR(10) NOT NULL,
  `effective_from` DATETIME(6) DEFAULT NULL,
  `effective_to` DATETIME(6) DEFAULT NULL,
  `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
  `deleted_at` DATETIME(6) DEFAULT NULL,
  `created_by` VARCHAR(50) DEFAULT NULL,
  `created_at` DATETIME(6) NOT NULL,
  `modified_by` VARCHAR(50) DEFAULT NULL,
  `modified_at` DATETIME(6) DEFAULT NULL,
  PRIMARY KEY (`cost_id`),
  FOREIGN KEY (`material_id`) REFERENCES `tb_material` (`material_id`),
  FOREIGN KEY (`product_id`) REFERENCES `tb_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;