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
- **Database**: H2 Database (File-persisted for local development)
- **Frontend**: HTML5, Thymeleaf, Tailwind CSS (via CDN), Alpine.js
- **Testing**: JUnit 5, Mockito, Spring Security Test

---

## 🚀 Getting Started

### Prerequisites
- **Java Development Kit (JDK)** version 21 or higher.
- **Maven** (or use the included Maven wrapper `./mvnw`).

### 1. Run the Application
Start the application using the Maven wrapper:
```bash
./mvnw spring-boot:run
```

Once started, the application is available at:
👉 **[http://localhost:8080](http://localhost:8080)**

### 2. Database & Console Access
By default, the application is configured to run with a local file-based H2 database saved at `~/stockmate-db`. This ensures your portfolios persist across application restarts.

To access the H2 database console:
- **URL**: `http://localhost:8080/h2-console`
- **JDBC URL**: `jdbc:h2:file:~/stockmate-db`
- **Username**: `sa`
- **Password**: *(leave blank)*

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
