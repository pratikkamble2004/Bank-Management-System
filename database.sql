CREATE DATABASE IF NOT EXISTS bank_management;
USE bank_management;

DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS accounts;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL DEFAULT 'CUSTOMER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE accounts (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL UNIQUE,
    account_number VARCHAR(20) NOT NULL UNIQUE,
    account_type VARCHAR(50) NOT NULL DEFAULT 'SAVINGS',
    balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_account_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE TABLE transactions (
    id INT PRIMARY KEY AUTO_INCREMENT,
    account_id INT NOT NULL,
    transaction_type VARCHAR(30) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    description VARCHAR(255),
    reference VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_transaction_account
        FOREIGN KEY (account_id) REFERENCES accounts(id)
        ON DELETE CASCADE
);

-- Demo users (passwords are BCrypt-hashed: '12345' for pratik & demo, 'admin123' for admin)
INSERT INTO users(id, username, password, role)
VALUES (1, 'pratik', '$2a$10$CY/OzCU25QOFQPjAWs4PfelNSUNcqPvkONC9T3U00UxK.gRgpaXUK', 'CUSTOMER');

INSERT INTO accounts(user_id, account_number, account_type, balance)
VALUES (1, '1000000001', 'SAVINGS', 10000.00);

INSERT INTO users(id, username, password, role)
VALUES (2, 'demo', '$2a$10$CY/OzCU25QOFQPjAWs4PfelNSUNcqPvkONC9T3U00UxK.gRgpaXUK', 'CUSTOMER');

INSERT INTO accounts(user_id, account_number, account_type, balance)
VALUES (2, '1000000002', 'SAVINGS', 5000.00);

-- Admin user
INSERT INTO users(id, username, password, role)
VALUES (3, 'admin', '$2a$10$Dtc1YSxX4q6PhK8Ln.LROeGjGWu2toNQP.QeyNdXc21FqwtafVFZS', 'ADMIN');
