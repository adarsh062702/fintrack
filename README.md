# FinTrack — Personal Finance Management Backend

<p align="center">
  <strong>A secure and scalable REST API backend for personal finance management.</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-orange?logo=openjdk" alt="Java 17" />
  <img src="https://img.shields.io/badge/Spring%20Boot-4.1.1-brightgreen?logo=springboot" alt="Spring Boot" />
  <img src="https://img.shields.io/badge/MySQL-8-blue?logo=mysql" alt="MySQL" />
  <img src="https://img.shields.io/badge/Security-JWT-black?logo=jsonwebtokens" alt="JWT" />
  <img src="https://img.shields.io/badge/Build-Maven-C71A36?logo=apachemaven" alt="Maven" />
</p>

---

## Overview

**FinTrack** is a personal finance management system designed to help users track their income, expenses, budgets, and financial goals through a centralized platform.

The backend is developed using Java and Spring Boot, following a layered architecture to maintain separation of concerns, modularity, and maintainability.

It provides RESTful APIs for financial operations, integrates with MySQL for persistent data storage, and uses Spring Security with JWT-based authentication to secure protected resources.

The backend is designed to integrate with the FinTrack React frontend.

## Key Features

### Authentication & Security
- JWT-based stateless authentication.
- Password encryption using BCrypt.
- Spring Security integration.
- Protected API endpoints.

### Transaction Management
- Create and manage financial transactions.
- Organize transactions using categories.
- Persist transaction records using MySQL.

### Budget Management
- Create and manage budgets.
- Associate budgets with financial categories.
- Track budget-related financial activity.

### Category Management
- Manage transaction categories.
- Retrieve category-wise spending information.
- Generate category spending summaries using repository projections.

### Financial Goals
- Create and manage financial goals.
- Record contributions towards goals.
- Track accumulated contributions and progress.

## Technology Stack

| Technology | Usage |
|---|---|
| Java 17 | Core programming language |
| Spring Boot | Backend framework |
| Spring Security | Authentication and authorization |
| JWT | Stateless authentication |
| Spring Data JPA | Data access and persistence |
| Hibernate | Object-relational mapping |
| MySQL | Relational database |
| Maven | Dependency management and build |
| JUnit | Automated testing |
| Mockito | Unit testing and mocking |
| Postman | API testing |

## Application Architecture

FinTrack follows a layered architecture to separate application responsibilities.

```text
Client (React Frontend)
        |
        v
   REST Controllers
        |
        v
    Service Layer
        |
        v
  Repository Layer
        |
        v
   MySQL Database
```

### Architectural Layers

| Layer | Responsibility |
|---|---|
| Controller | Handles HTTP requests and responses |
| Service | Implements business logic |
| Repository | Handles database operations |
| Entity | Represents persistent database models |
| DTO | Transfers data between application layers and clients |
| Exception | Centralized exception handling |
| Security | Authentication and authorization |

## Project Structure

```text
src/
├── main/
│   ├── java/com/adarsh/fintrack/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── entity/
│   │   ├── exception/
│   │   ├── repository/
│   │   ├── service/
│   │   └── FintrackApplication.java
│   │
│   └── resources/
│       └── application.properties
│
└── test/
    ├── java/com/adarsh/fintrack/
    │   ├── service/
    │   └── TestDatabaseConnectionTest.java
    │
    └── resources/
        └── application-test.properties
```

## API Documentation

The backend exposes RESTful endpoints for authentication and financial management.

The main API modules include:

| Module | Functionality |
|---|---|
| Authentication | User registration and login |
| Transactions | Transaction management |
| Categories | Category management |
| Budgets | Budget management |
| Category Spending | Category-wise spending summaries |
| Goals | Financial goal management |
| Goal Contributions | Tracking goal contributions |

**Base URL (local development):**

```text
http://localhost:8080/api
```

API requests can be tested using Postman. Protected endpoints require a valid JWT access token.

## Getting Started

### Prerequisites

- Java 17 or later
- MySQL 8+
- Git
- Maven (optional, Maven Wrapper is included)

### 1. Clone the repository

```bash
git clone https://github.com/adarsh062702/fintrack.git
cd fintrack
```

### 2. Create the database

```sql
CREATE DATABASE fintrack;
```

### 3. Configure environment variables

Configure the following environment variables:

| Variable | Description |
|---|---|
| `DB_USERNAME` | MySQL username |
| `DB_PASSWORD` | MySQL password |
| `FINTRACK_JWT_SECRET` | JWT signing secret |

Example for macOS/Linux:

```bash
export DB_USERNAME='root'
export DB_PASSWORD='your_mysql_password'
export FINTRACK_JWT_SECRET='your_secure_jwt_secret'
```

Use a sufficiently strong JWT signing secret and never commit actual credentials to the repository.

### 4. Run the application

macOS/Linux:

```bash
./mvnw spring-boot:run
```

Windows:

```bash
mvnw.cmd spring-boot:run
```

The backend will start at:

```text
http://localhost:8080
```

## Testing

FinTrack includes automated tests for application and service-layer functionality.

Run the complete test suite:

```bash
./mvnw clean test
```

The integration tests use a dedicated database:

```sql
CREATE DATABASE fintrack_test;
```

Configure `DB_USERNAME` and `DB_PASSWORD` before running tests.

## Engineering Practices

The project incorporates several backend engineering practices:

- Layered architecture and separation of concerns.
- DTO-based request and response handling.
- Relational database modelling.
- Spring Data JPA repository abstractions.
- Centralized exception handling.
- Stateless JWT authentication.
- Automated unit and integration testing.
- Environment-based configuration for sensitive credentials.

## Future Improvements

- API documentation with Swagger / OpenAPI.
- Advanced financial analytics and reporting.
- Improved financial health scoring.
- Recurring transaction support.
- Enhanced authorization and account management.
- Performance optimization and caching.
- Docker-based deployment.
- CI/CD automation.

## Related Project

**FinTrack Frontend:** React-based user interface for interacting with the FinTrack backend.

## Author

**Adarsh**

[GitHub Profile](https://github.com/adarsh062702)

[FinTrack Repository](https://github.com/adarsh062702/fintrack)

---

<p align="center">
  <strong>FinTrack — Take control of your finances.</strong>
</p>
