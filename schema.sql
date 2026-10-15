-- ============================================================================
-- SQL Setup Script: Personal Finance Management Portal
-- Target: MySQL / MariaDB / phpMyAdmin / MySQL Workbench
-- ============================================================================

CREATE DATABASE IF NOT EXISTS finance_db;
USE finance_db;

CREATE TABLE IF NOT EXISTS transactions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(10) NOT NULL,
    entry_date VARCHAR(15) NOT NULL,
    category VARCHAR(50) NOT NULL,
    amount DOUBLE NOT NULL,
    description VARCHAR(100) NOT NULL
);
