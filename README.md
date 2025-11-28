# Wallet Service

## Overview
Wallet Service is a microservice responsible for managing digital wallets in a fintech application. It handles wallet creation, retrieval, and balance management. This service is designed to be independently deployable and maintains its own database following microservices architecture principles.

## Table of Contents
- [Technologies Used](#technologies-used)
- [Project Structure](#project-structure)
- [Architecture Decisions](#architecture-decisions)
- [Dependencies](#dependencies)
- [API Endpoints](#api-endpoints)
- [Running the Service](#running-the-service)
- [Database Schema](#database-schema)
- [Testing with Postman](#testing-with-postman)

## Technologies Used
- **Java 17** - LTS version with modern language features
- **Spring Boot 4.0.0** - Framework for building production-ready applications
- **Spring Data JPA** - Data persistence and ORM
- **H2 Database** - In-memory database for development and testing
- **Lombok** - Reduces boilerplate code
- **Maven** - Build and dependency management

## Project Structure
```
digital-wallet-system/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/wallet/system/
│   │   │       ├── controller/
│   │   │       │   └── WalletController.java
│   │   │       ├── dto/
│   │   │       │   ├── WalletRequest.java
│   │   │       │   ├── WalletResponse.java
│   │   │       │   └── BalanceUpdateRequest.java
│   │   │       ├── entity/
│   │   │       │   └── Wallet.java
│   │   │       ├── exception/
│   │   │       │   ├── GlobalExceptionHandler.java
│   │   │       │   ├── WalletNotFoundException.java
│   │   │       │   ├── InsufficientBalanceException.java
│   │   │       │   └── WalletAlreadyExistsException.java
│   │   │       ├── repository/
│   │   │       │   └── WalletRepository.java
│   │   │       ├── service/
│   │   │       │   └── WalletService.java
│   │   │       └── DigitalWalletSystemApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
├── pom.xml
└── README.md
```

## Architecture Decisions

### Why Separate Wallet and Transaction Services?

This system is split into two microservices following the **Single Responsibility Principle** and **Domain-Driven Design**:

**Wallet Service (this service):**
- **Responsibility**: Manages wallet entities and their state (balance)
- **Domain**: Wallet lifecycle and data integrity
- **Why separate**: Wallets are a distinct bounded context with their own data model and business rules

**Transaction Service:**
- **Responsibility**: Orchestrates money movements (deposits, withdrawals, transfers)
- **Domain**: Transaction processing and history
- **Why separate**: Transaction logic is complex and should be isolated from wallet data management

**Benefits of this separation:**
1. **Independent Scaling**: If transactions surge, only Transaction Service needs scaling
2. **Team Autonomy**: Different teams can own different services
3. **Deployment Independence**: Can update Wallet Service without touching Transaction Service
4. **Fault Isolation**: If Transaction Service fails, wallets data remains accessible
5. **Technology Flexibility**: Each service can use different databases or tech stacks in the future

**Service Communication:**
- Transaction Service calls Wallet Service via REST APIs
- In production, could use message queues for asynchronous operations
- Clear API contracts between services

## Dependencies

### Core Dependencies
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```
**Purpose**: Provides REST API capabilities, embedded Tomcat server, and Spring MVC for building web applications.
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```
**Purpose**: Simplifies database operations with JPA/Hibernate. Provides repository pattern and automatic CRUD operations.
```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
</dependency>
```
**Purpose**: In-memory database perfect for development and testing. No installation required. In production, would use PostgreSQL or MySQL.
```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
</dependency>
```
**Purpose**: Generates boilerplate code (getters, setters, constructors) at compile time. Reduces code verbosity by ~70%.
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```
**Purpose**: Provides Bean Validation (JSR-380) for request validation. Ensures data integrity before processing.

## API Endpoints

### 1. Create Wallet
Creates a new digital wallet for a user.

**Endpoint**: `POST /api/wallets`

**Request Headers**:
```
Content-Type: application/json
```

**Request Body**:
```json
{
    "ownerName": "John Doe"
}
```

**Success Response** (201 Created):
```json
{
    "id": 1,
    "walletId": "WALLET-550e8400-e29b-41d4-a716-446655440000",
    "ownerName": "John Doe",
    "balance": 0.00,
    "createdAt": "2025-11-27T10:30:00",
    "updatedAt": "2025-11-27T10:30:00"
}
```

**Error Response** (400 Bad Request):
```json
{
    "timestamp": "2025-11-27T10:30:00",
    "status": 400,
    "error": "Bad Request",
    "message": "Owner name is required"
}
```

---

### 2. Get Wallet by ID
Retrieves wallet information by wallet ID.

**Endpoint**: `GET /api/wallets/{walletId}`

**Path Parameters**:
- `walletId` (string, required): The unique wallet identifier

**Example Request**:
```
GET http://localhost:8080/api/wallets/WALLET-550e8400-e29b-41d4-a716-446655440000
```

**Success Response** (200 OK):
```json
{
    "id": 1,
    "walletId": "WALLET-550e8400-e29b-41d4-a716-446655440000",
    "ownerName": "John Doe",
    "balance": 5000.00,
    "createdAt": "2025-11-27T10:30:00",
    "updatedAt": "2025-11-27T11:45:00"
}
```

**Error Response** (404 Not Found):
```json
{
    "timestamp": "2025-11-27T10:30:00",
    "status": 404,
    "error": "Not Found",
    "message": "Wallet not found: WALLET-invalid-id"
}
```

---

### 3. Update Wallet Balance
Updates the balance of a wallet. This endpoint is primarily called by Transaction Service.

**Endpoint**: `PUT /api/wallets/{walletId}/balance`

**Path Parameters**:
- `walletId` (string, required): The unique wallet identifier

**Request Body**:
```json
{
    "amount": 1000.00
}
```
*Note: Use positive values to add money, negative values to deduct money.*

**Example - Add Money**:
```json
{
    "amount": 500.00
}
```

**Example - Deduct Money**:
```json
{
    "amount": -200.00
}
```

**Success Response** (200 OK):
```json
{
    "id": 1,
    "walletId": "WALLET-550e8400-e29b-41d4-a716-446655440000",
    "ownerName": "John Doe",
    "balance": 5500.00,
    "createdAt": "2025-11-27T10:30:00",
    "updatedAt": "2025-11-27T12:00:00"
}
```

**Error Response** (400 Bad Request - Insufficient Balance):
```json
{
    "timestamp": "2025-11-27T12:00:00",
    "status": 400,
    "error": "Bad Request",
    "message": "Insufficient balance. Current: 100.00"
}
```

## Running the Service

### Prerequisites
- Java 17 or higher installed
- Maven installed (or use included Maven wrapper)
- Port 8080 available

### Steps to Run

1. **Clone the repository**
```bash
git clone https://github.com/Patricia-Sigei/digital-wallet-system.git
cd digital-wallet-system
```

2. **Build the project**
```bash
./mvnw clean install
```

3. **Run the application**
```bash
./mvnw spring-boot:run
```

Or run directly:
```bash
java -jar target/digital-wallet-system-0.0.1-SNAPSHOT.jar
```

4. **Verify it's running**
```bash
curl http://localhost:8080/actuator/health
```

The service will start on `http://localhost:8080`

### H2 Database Console
Access the in-memory database console for debugging:
- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:walletdb`
- Username: `sa`
- Password: (leave empty)

## Database Schema

### Wallet Table
| Column      | Type         | Constraints           | Description                    |
|-------------|--------------|-----------------------|--------------------------------|
| id          | BIGINT       | PRIMARY KEY, AUTO     | Internal database ID           |
| wallet_id   | VARCHAR(255) | UNIQUE, NOT NULL      | Business identifier (UUID)     |
| owner_name  | VARCHAR(255) | NOT NULL              | Name of wallet owner           |
| balance     | DECIMAL(38,2)| NOT NULL              | Current wallet balance         |
| created_at  | TIMESTAMP    | NOT NULL              | Wallet creation timestamp      |
| updated_at  | TIMESTAMP    | NOT NULL              | Last modification timestamp    |

**Indexes**:
- Primary key index on `id`
- Unique index on `wallet_id`

## Testing with Postman

### Import Collection
Create a Postman collection with these requests:

### Test Scenario 1: Create and Retrieve Wallet

**Step 1: Create a wallet**
```
POST http://localhost:8080/api/wallets
Content-Type: application/json

{
    "ownerName": "Alice Johnson"
}
```

**Step 2: Copy the walletId from response**

**Step 3: Get wallet details**
```
GET http://localhost:8080/api/wallets/{{walletId}}
```

### Test Scenario 2: Update Balance

**Add 5000 to wallet**
```
PUT http://localhost:8080/api/wallets/{{walletId}}/balance
Content-Type: application/json

{
    "amount": 5000
}
```

**Verify new balance**
```
GET http://localhost:8080/api/wallets/{{walletId}}
```

### Test Scenario 3: Error Handling

**Test insufficient balance**
```
PUT http://localhost:8080/api/wallets/{{walletId}}/balance
Content-Type: application/json

{
    "amount": -10000
}
```
Expected: 400 Bad Request with error message

**Test wallet not found**
```
GET http://localhost:8080/api/wallets/INVALID-WALLET-ID
```
Expected: 404 Not Found

## Service Design Patterns

### Repository Pattern
- `WalletRepository` provides abstraction over data access
- Spring Data JPA auto-implements CRUD operations
- Custom queries: `findByWalletId()`, `existsByWalletId()`

### DTO Pattern
- Separates internal entity from API contracts
- `WalletRequest` for input validation
- `WalletResponse` for output formatting
- Prevents exposing internal database structure

### Exception Handling
- Custom exceptions for business logic failures
- `@RestControllerAdvice` for global exception handling
- Consistent error response format across all endpoints

### Transaction Management
- `@Transactional` ensures atomic operations
- Automatic rollback on exceptions
- Prevents partial updates to wallet balance

## Configuration

### application.properties
```properties
# Application Configuration
spring.application.name=wallet-service
server.port=8080

# Database Configuration
spring.datasource.url=jdbc:h2:mem:walletdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA Configuration
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

# H2 Console
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

## Future Enhancements

### Security
- Add JWT authentication
- Implement role-based access control (RBAC)
- Encrypt sensitive data at rest


### Production Readiness
- Replace H2 with PostgreSQL/MySQL
- Implement database migrations (Flyway/Liquibase)
- Add API versioning
- Implement rate limiting
- Add request/response logging


## License
This project is part of a practical assessment for a digital wallet system implementation.
