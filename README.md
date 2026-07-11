# 📈 Stock Mate - Smart Average Down Planner

Stock Mate is a premium monolithic web application designed to help retail investors plan, calculate, and manage their stock average-down strategies with maximum mathematical precision. Built with Spring Boot and Java 21, it features a modern, gorgeous, dark-themed responsive UI styled with Tailwind CSS and enhanced by Alpine.js.

Unlike simple average-down calculators, Stock Mate integrates real-world constraints such as broker commission fees, multi-step transaction laddering, real-time Yahoo Finance price queries, and target-price profit simulators (Take Profit) alongside multi-portfolio persistence.

---

## ✨ Features

### 1. Advanced Math & Broker Fees 📉
- Multi-precision calculations using `BigDecimal` to ensure zero rounding errors.
- Pre-filled custom broker buy and sell fee settings (defaults to IDX standard `0.15%` buy and `0.25%` sell).
- Displays gross vs. net capital spent, showing exactly how fees impact your average down price.

### 2. Multi-Step Average Down (Laddering) 🪜
- Add or remove multiple buying tranches (stages) dynamically without full page reload.
- Supports mixed price levels (e.g. buying 5 lots at Rp900, 10 lots at Rp850, and 20 lots at Rp800).
- Aggregates all steps to output a highly accurate blended net average cost.

### 3. Take Profit & Sell Price Simulator 🎯
- Project returns by defining a target sell price.
- Computes gross return, selling transaction fees/taxes, net returns, net profit/loss, and ROI percentages.

### 4. Real-time Yahoo Finance Integration ⚡
- Type a ticker code (e.g., `BBCA` or `GOTO`). Stock Mate automatically resolves IDX tickers by appending `.JK` and fetches the current live market price.
- Live price instantly autofills the first buying tranche with a single click.

### 5. Multi-Portfolio Persistence & Accounts 💾
- Create an account and sign in using a custom, secure dashboard.
- Create multiple portfolios (e.g., "Retirement Portfolio", "Trading Basket").
- Save stock calculation snapshots to portfolios and reload them directly with a single click.

---

## 🛠️ Technology Stack

Stock Mate uses a modern, lightweight, best-practice production stack:
- **Backend**: Spring Boot 4.x / Java 21, Spring Security, Spring Data JPA
- **Database**: MySQL 8.0 (Containerized), H2 Database (In-Memory for unit tests)
- **Frontend**: HTML5, Thymeleaf, Tailwind CSS (via CDN), Alpine.js
- **Testing**: JUnit 5, Mockito, Spring Security Test
- **Containerization**: Docker, Docker Compose

---

## 🚀 Getting Started

### Method A: Running with Docker Compose (Recommended)
You can run the entire application stack including the application service and the MySQL database with a single command. 

**Prerequisites:**
- **Docker** and **Docker Compose** installed on your system.

1. Start the stack from the project root:
   ```bash
   docker compose up --build -d
   ```
2. Once started, the application will automatically wait for the MySQL container to be healthy and then start up at:
   👉 **[http://localhost:8080](http://localhost:8080)**

3. To shut down the services and preserve database volume data:
   ```bash
   docker compose down
   ```

### Method B: Running Locally (Manual Development)

**Prerequisites:**
- **Java Development Kit (JDK)** version 21 or higher.
- **MySQL** server running locally (or configured in `application.properties`).

1. Make sure you have a MySQL database named `stockmate` running, and your credentials match the default fallbacks in `application.properties` (User: `stockuser`, Pass: `stockpass`). Or configure them via environment variables:
   ```bash
   export DB_HOST=localhost
   export DB_USER=my_db_user
   export DB_PASSWORD=my_db_password
   ```

2. Start the application:
   ```bash
   ./mvnw spring-boot:run
   ```

3. Open the browser at:
   👉 **[http://localhost:8080](http://localhost:8080)**

---

## 🔒 Security Configuration
The application is pre-configured with Spring Security:
- **Public Pages**: Guest calculator (`/`), registration (`/register`), login (`/login`), and stock APIs (`/api/stock/**`).
- **Protected Pages**: Dashboard (`/dashboard`) and saving/deleting portfolio endpoints require authentication.
- **Password Hashing**: Uses secure BCrypt encryption.

---

## 🧪 Verification & Testing
To run the automated test suite verifying mathematical precision, broker fee formulas, and user registration:
```bash
./mvnw test
```
