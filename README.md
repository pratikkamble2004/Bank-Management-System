# 🏦 Apex Bank Management System

[![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Tomcat](https://img.shields.io/badge/Apache%20Tomcat-11.0-F8DC75?logo=apachetomcat&logoColor=black)](https://tomcat.apache.org/)
[![Jakarta EE](https://img.shields.io/badge/Jakarta%20Servlet-6.1-2C2255?logo=jakartaee&logoColor=white)](https://jakarta.ee/)
[![Angular](https://img.shields.io/badge/Angular-21.2-DD0031?logo=angular&logoColor=white)](https://angular.dev/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Bootstrap](https://img.shields.io/badge/Bootstrap-5.3-7952B3?logo=bootstrap&logoColor=white)](https://getbootstrap.com/)
[![Maven](https://img.shields.io/badge/Maven-3.9-C71A36?logo=apachemaven&logoColor=white)](https://maven.apache.org/)

A full-stack, enterprise-grade digital banking web application built with **Java 21**, **Jakarta Servlets 6.1**, **Apache Tomcat 11**, **MySQL 8.0**, and **Angular 21**. 

Designed using a **3-Tier Layered Architecture**, the system features both a self-service customer banking portal and a bank employee/teller administrative terminal, backed by **ACID-compliant manual transaction management** and **pessimistic row-level locking** via native JDBC.

---


## 📑 Table of Contents

- [Core Features](#-core-features)
- [System Architecture](#-system-architecture)
- [Tech Stack & Dependencies](#-tech-stack--dependencies)
- [Project Directory Structure](#-project-directory-structure)
- [Prerequisites](#-prerequisites)
- [Database Setup](#-database-setup)
- [Environment Configuration](#-environment-configuration)
- [How to Run](#-how-to-run)
  - [Option 1: Quick 1-Click Launch (Windows)](#option-1-quick-1-click-launch-windows)
  - [Option 2: Manual Terminal Execution](#option-2-manual-terminal-execution)
- [API Endpoints Reference](#-api-endpoints-reference)
- [Security & Concurrency Highlights](#-security--concurrency-highlights)
- [GitHub & Contribution](#-github--contribution)

---

## ✨ Core Features

### 👤 Customer Banking Portal
- **Account Dashboard**: Real-time display of ledger balance, account type, masked account number, and recent transaction history.
- **Cash Deposit**: Instant self-service account credit with preset quick-amount chips and auto-generated receipt IDs.
- **ATM / UPI Cash Withdrawal**: Balance-validated withdrawals simulating ATM pickup terminals and QR-based debit.
- **Direct Wire Transfers**: Inter-account fund transfers with recipient account verification, balance checks, and atomic debit/credit operations.
- **Passbook Statement**: Comprehensive transaction history ledger with live filtering by transaction type (Credit/Debit) and reference search.
- **UPI ID & QR Terminal**: Displays personalized virtual payment address (`username@apexbank`), branch IFSC, and quick copy actions.

### 👔 Bank Employee / Admin Terminal
- **Branch Dashboard**: Live branch liquidity tracking (Total Vault Deposits), registered user count, active accounts, and total ledger operations.
- **Counter Cash Deposits**: Teller-assisted counter deposit crediting funds to any verified 10-digit customer account.
- **Teller Wire Transfers**: Teller-assisted fund transfers between any two accounts in the bank network.
- **Client Accounts Directory**: Complete roster of customer accounts with live keyword search by user ID or account number.
- **Global Audit Ledger**: Bank-wide immutable audit trail of all transactions with timestamp, reference IDs, and debit/credit labels.

---

## 🏛 System Architecture

The application strictly follows the **3-Tier Layered Architecture**:

```text
 ┌────────────────────────────────────────────────────────┐
 │            1. PRESENTATION TIER (Frontend)             │
 │         Angular 21 SPA (Port 4200) + Bootstrap 5       │
 └───────────────────────────┬────────────────────────────┘
                             │ HTTP / JSON (via Proxy)
 ┌───────────────────────────▼────────────────────────────┐
 │         2. BUSINESS & LOGIC TIER (Tomcat 11 / Port 8080)│
 │                                                        │
 │  ┌──────────────────────────────────────────────────┐  │
 │  │ Controller Layer (Jakarta Servlets & Filters)    │  │
 │  └────────────────────────┬─────────────────────────┘  │
 │                           │ Java Method Calls          │
 │  ┌────────────────────────▼─────────────────────────┐  │
 │  │ Service Layer (Business Logic & Transactions)    │  │
 │  └────────────────────────┬─────────────────────────┘  │
 │                           │ Java Method Calls          │
 │  ┌────────────────────────▼─────────────────────────┐  │
 │  │ Data Access Layer (DAOs)                         │  │
 │  └────────────────────────┬─────────────────────────┘  │
 └───────────────────────────┼────────────────────────────┘
                             │ JDBC (SQL over TCP)
 ┌───────────────────────────▼────────────────────────────┐
 │             3. DATA TIER (MySQL Database)              │
 │          MySQL 8.0 Server (Port 3306)                  │
 └────────────────────────────────────────────────────────┘
```

---

## 🛠 Tech Stack & Dependencies

### Backend
- **Runtime**: Java 21 / 22 (Source target: Java 21)
- **Servlet Container**: Apache Tomcat 11.0.15
- **Web Specification**: Jakarta Servlet API 6.1.0 (`jakarta.servlet-api`)
- **Data Persistence**: Pure Native Java JDBC API (`java.sql.*`)
- **Database Driver**: MySQL Connector/J 9.3.0 (`com.mysql:mysql-connector-j`)
- **JSON Processing**: Google Gson 2.11.0 (`com.google.code.gson:gson`)
- **Security & Password Hashing**: jBCrypt 0.4 (`org.mindrot:jbcrypt`)
- **Build System**: Apache Maven 3.9

### Frontend
- **Framework**: Angular 21.2 (Standalone Component Architecture)
- **Change Detection**: Angular Zoneless Change Detection (`provideZonelessChangeDetection`)
- **Styling**: Bootstrap 5.3 + Bootstrap Icons 1.13 + Custom CSS
- **HTTP Client**: Reactive RxJS 7.8 (`HttpClient`, `forkJoin`, `BehaviorSubject`)
- **Proxy**: Angular Dev Server Reverse Proxy (`proxy.conf.json`)

### Database
- **Engine**: MySQL 8.0 Server (InnoDB engine for foreign keys and row locking)

---

## 📂 Project Directory Structure

```text
temporary/
├── .env.example              # Template environment variables (safe for git)
├── .gitignore                # Root gitignore (ignores .env, target, node_modules, logs)
├── database.sql              # Database schema DDL & seed records
├── pom.xml                   # Maven project descriptor & dependencies
├── README.md                 # Project documentation
├── start-backend.bat         # 1-click launcher for Tomcat Backend
├── start-frontend.bat        # 1-click launcher for Angular Frontend
├── src/
│   └── main/
│       ├── java/com/bank/
│       │   ├── controller/   # Jakarta Servlets (Login, Account, Transfer, Admin, etc.)
│       │   ├── dao/          # Data Access Objects (UserDAO, AccountDAO, TransactionDAO)
│       │   ├── model/        # Domain POJOs (User, Account, Transaction)
│       │   ├── service/      # Business Services & ACID Transactions (AccountService, AuthService)
│       │   └── util/         # Database connection & JSON helper utilities
│       └── resources/
│           └── db.properties # JDBC connection settings (read at runtime)
└── frontend/
    ├── angular.json          # Angular CLI workspace config
    ├── package.json          # Frontend npm scripts & dependencies
    ├── proxy.conf.json       # Proxy routing /bank-management -> http://localhost:8080
    └── src/
        └── app/
            ├── components/   # Standalone UI components (dashboard, admin, transfer, upi, etc.)
            ├── core/         # Angular HTTP services & models
            └── guards/       # Route guards for role authorization
```

---

## 📋 Prerequisites

Before running the project locally, ensure you have the following installed:

1. **Java JDK 21 or higher** (`java -version`)
2. **Apache Tomcat 11.0.x** ([Tomcat Downloads](https://tomcat.apache.org/download-11.cgi))
3. **MySQL Server 8.0+** ([MySQL Downloads](https://dev.mysql.com/downloads/mysql/))
4. **Node.js 20+ & npm** (`node -v`, `npm -v`)
5. **Apache Maven 3.9+** (`mvn -v`)

---

## 🗄 Database Setup

1. Start your local MySQL service:
   ```powershell
   net start MySQL80
   ```
2. Open MySQL CLI or MySQL Workbench:
   ```bash
   mysql -u root -p
   ```
3. Execute the provided `database.sql` script:
   ```sql
   SOURCE c:/path/to/project/database.sql;
   ```
   *(Or copy-paste the contents of `database.sql` into your SQL client and execute)*.

---

## 🔐 Environment Configuration

To keep sensitive passwords and credentials safe, this project uses an environment file (`.env`) that is ignored by Git.

1. In the project root, copy `.env.example` to `.env`:
   ```powershell
   Copy-Item .env.example .env
   ```
2. Open `.env` and adjust your local MySQL password:
   ```env
   DB_URL=jdbc:mysql://localhost:3306/bank_management?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
   DB_USERNAME=root
   DB_PASSWORD=your_actual_password
   ```
3. Update `src/main/resources/db.properties` with the matching credentials.

> ⚠️ **Security Warning**: Never commit your `.env` or production credentials to GitHub. The root `.gitignore` is pre-configured to prevent accidental commits.

---

## 🚀 How to Run

### Option 1: Quick 1-Click Launch (Windows)

Two batch launcher scripts are provided in the root folder:

1. **Double-click `start-backend.bat`**:
   - Automatically builds the WAR with Maven, deploys to Tomcat `webapps/`, and starts Apache Tomcat on `http://localhost:8080`.
   - *Keep this terminal window open.*
2. **Double-click `start-frontend.bat`**:
   - Starts the Angular Dev Server on `http://localhost:4200`.
   - *Keep this terminal window open.*
3. Open your browser and navigate to:
   👉 **`http://localhost:4200`**

---

### Option 2: Manual Terminal Execution

#### 1. Compile & Deploy the Backend
Open a terminal in the project root:
```powershell
# 1. Package WAR archive
mvn package -DskipTests

# 2. Deploy to Tomcat webapps directory
Copy-Item -Path "target\bank-management\*" -Destination "C:\path\to\apache-tomcat-11.x\webapps\bank-management\" -Recurse -Force

# 3. Start Apache Tomcat
$env:JAVA_HOME = "C:\Program Files\Java\jdk-22"
$env:CATALINA_HOME = "C:\path\to\apache-tomcat-11.x"
& "$env:CATALINA_HOME\bin\catalina.bat" run
```

#### 2. Start the Frontend
Open a second terminal in the `frontend` folder:
```powershell
cd frontend
npm install
npm start
```
Access the application at `http://localhost:4200`.

---

## 🔑 Demo Login Credentials

The database script initializes the following test accounts (passwords are stored as BCrypt hashes in `database.sql`):

| Role | Username | Plain-Text Password | Account Number | Purpose |
| :--- | :--- | :--- | :--- | :--- |
| **Bank Admin / Teller** | `admin` | `admin123` | — | Accesses Bank Employee Portal, Global Audit Ledger, Counter Operations |
| **Customer** | `pratik` | `12345` | `1000000001` | Primary customer account (initial balance: ₹10,000.00) |
| **Customer** | `demo` | `12345` | `1000000002` | Secondary transfer destination account (initial balance: ₹5,000.00) |

> ℹ️ **Demo Passwords Note**: Passwords in `database.sql` are securely stored as BCrypt hashes. To log in through the application or test the endpoints, use the plain-text passwords listed above.

---

## 📡 API Endpoints Reference

Base Context URL: `http://localhost:8080/bank-management`

| Method | Endpoint | Description | Protected? |
| :--- | :--- | :--- | :---: |
| `POST` | `/api/auth/login` | Authenticates username & password; sets `HttpSession`. | No |
| `GET` | `/api/auth/me` | Retrieves profile of currently authenticated session. | Yes |
| `POST` | `/api/auth/register` | Registers customer and auto-generates 10-digit account number. | No |
| `POST` | `/api/auth/logout` | Invalidates active user session. | Yes |
| `GET` | `/api/account` | Retrieves current account details and balance. | Yes |
| `GET` | `/api/account/balance` | Returns live numeric balance. | Yes |
| `GET` | `/api/account/lookup` | Validates target account number (`?accountNumber=...`). | Yes |
| `POST` | `/api/account/deposit` | Deposits money into customer's account. | Yes |
| `POST` | `/api/account/withdraw` | Withdraws money with balance verification. | Yes |
| `POST` | `/api/account/transfer` | Executes atomic inter-account transfer. | Yes |
| `GET` | `/api/account/transactions` | Returns customer passbook statement history. | Yes |
| `GET` | `/api/admin/` | Returns system-wide customer, account, and transaction counts. | Admin Only |
| `GET` | `/api/admin/accounts` | Returns directory of all registered accounts. | Admin Only |
| `GET` | `/api/admin/transactions` | Returns full bank global audit log. | Admin Only |
| `POST` | `/api/admin/deposit` | Teller-assisted cash deposit to any client account. | Admin Only |
| `POST` | `/api/admin/transfer` | Teller-assisted inter-account wire transfer. | Admin Only |

---


## 📄 License

This project is licensed under the [MIT License](LICENSE).
