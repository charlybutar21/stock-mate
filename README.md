# 📈 Stock Mate - Smart Average Down Planner

**Stock Mate** is a premium web application engineered to help retail investors plan, calculate, and manage their stock average-down strategies with maximum mathematical precision. Built on a robust Spring Boot framework and Java 21, it delivers a modern, responsive, dark-themed UI.

Unlike simple average-down calculators, Stock Mate integrates real-world constraints such as broker commission fees, multi-step transaction laddering, target-price profit simulators (Take Profit), and multi-portfolio persistence using a clean, well-documented architecture.

---

## ✨ Features and Use Cases

### 1. Advanced Math & Broker Fees 📉
- **Precision:** Multi-precision calculations using `BigDecimal` to ensure zero rounding errors.
- **Customization:** Pre-filled custom broker buy and sell fee settings (defaults to IDX standard `0.15%` buy and `0.25%` sell).
- **Insight:** Displays gross vs. net capital spent, showing exactly how fees impact your average down price.

### 2. Multi-Step Average Down (Laddering) 🪜
- **Dynamic Modeling:** Add or remove multiple buying tranches (stages) dynamically.
- **Mixed Levels:** Supports mixed price levels (e.g., buying 5 lots at Rp900, 10 lots at Rp850, and 20 lots at Rp800).
- **Aggregation:** Aggregates all steps to output a highly accurate blended net average cost.

### 3. Take Profit & Sell Price Simulator 🎯
- **Target Projection:** Project returns by defining a target sell price.
- **Comprehensive Analysis:** Computes gross return, selling transaction fees/taxes, net returns, net profit/loss, and ROI percentages.

### 4. Multi-Portfolio Persistence & Accounts 💾
- **Secure Access:** Create an account and sign in using a secure Spring Security integration.
- **Organized Portfolios:** Create multiple portfolios (e.g., "Retirement Portfolio", "Trading Basket").
- **State Management:** Save stock calculation snapshots to portfolios and reload them directly with a single click.
- **Consolidated Dashboard:** View an aggregated summary of identical stocks across multiple portfolios.

---

## 🏗️ Code Structure & Architecture

The application strictly adheres to SOLID principles, separation of concerns, and Clean Code practices:

- **`com.stockmate.controller`**: Thin presentation layer. Routes HTTP requests, handles payload validation, and manages view states. Relies entirely on the service layer for business rules.
- **`com.stockmate.service`**: The core business logic layer. Contains all complex algorithms, portfolio aggregation (`PortfolioService`), and mathematical calculations (`CalculatorService`).
- **`com.stockmate.model` & `com.stockmate.dto`**: Data structures leveraging Lombok for boilerplate reduction (`@Getter`, `@Setter`, `@Builder`). Clear boundary between persistence entities and Data Transfer Objects.
- **`com.stockmate.repository`**: Data Access Object layer powered by Spring Data JPA.

---

## 🛠️ Technology Stack

Stock Mate uses a modern, lightweight, best-practice production stack:
- **Backend Framework**: Spring Boot 3.x / Java 21
- **Security**: Spring Security 6 (BCrypt Hashing)
- **Data Persistence**: Spring Data JPA with MySQL 8.0 (Containerized), H2 (In-Memory for unit tests)
- **Frontend**: HTML5, Thymeleaf, Tailwind CSS (via CDN), Alpine.js
- **API Documentation**: OpenAPI (Swagger UI) powered by `springdoc-openapi`
- **Testing**: JUnit 5, Mockito, Spring Security Test, Jacoco (Test Coverage >90%)
- **Code Generation**: Lombok
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

## 📚 API Documentation (OpenAPI / Swagger)

The application provides interactive API documentation exposing all endpoints, payloads, and responses. 

When the application is running, access the Swagger UI at:
👉 **[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**

Or the raw OpenAPI JSON definition at:
👉 **[http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)**

---

## 🧪 Verification & Testing

The project maintains a high test coverage strictly above 90%, managed by Jacoco and verified using JUnit and Mockito.

To run the automated test suite and generate the coverage report:
```bash
./mvnw clean test jacoco:report
```

To view the coverage report locally:
Open `target/site/jacoco/index.html` in your browser.
