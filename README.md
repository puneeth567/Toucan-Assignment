# Toucan Payments — Transaction Processing Service

A Spring Boot RESTful web service designed for processing and managing financial transactions, built for the Toucan Payments Engineering Challenge.

---

## Overview

The **Transaction Processing Service** provides robust financial transaction management capabilities. It supports creating transactions, querying transaction details, transitioning transaction states via status updates, and retrieving transaction history for specific customers.

### Key Operations Implemented

1. **Create Transaction** (`POST /api/v1/transactions`)
2. **Get Transaction by ID** (`GET /api/v1/transactions/{id}`)
3. **Update Transaction Status** (`PATCH /api/v1/transactions/{id}/status`)
4. **Get Customer Transactions** (`GET /api/v1/customers/{customerId}/transactions`)

---

## Technology Stack

* **Language**: Java 17
* **Framework**: Spring Boot 3.5.5
* **Web**: Spring Web (MVC / REST API)
* **Data Persistence**: Spring Data JPA / Hibernate
* **Database**: H2 Database (In-Memory Engine)
* **Validation**: Jakarta Bean Validation (`jakarta.validation-api`)
* **Build System**: Apache Maven (via Maven Wrapper `mvnw.cmd` / `mvnw`)
* **Testing**: JUnit 5, Spring Boot Test, MockMvc, Hamcrest

---

## Project Structure

```text
src/
├── main/
│   ├── java/
│   │   └── com/example/transactionstarter/
│   │       ├── controller/
│   │       │   └── TransactionController.java
│   │       ├── dto/
│   │       │   ├── CreateTransactionRequest.java
│   │       │   ├── TransactionResponse.java
│   │       │   └── UpdateStatusRequest.java
│   │       ├── exception/
│   │       │   ├── DuplicateTransactionIdException.java
│   │       │   ├── ErrorResponse.java
│   │       │   ├── GlobalExceptionHandler.java
│   │       │   ├── InvalidStatusTransitionException.java
│   │       │   └── ResourceNotFoundException.java
│   │       ├── model/
│   │       │   ├── Transaction.java
│   │       │   ├── TransactionStatus.java
│   │       │   └── TransactionType.java
│   │       ├── repository/
│   │       │   └── TransactionRepository.java
│   │       ├── service/
│   │       │   ├── TransactionService.java
│   │       │   └── TransactionServiceImpl.java
│   │       └── TransactionStarterApplication.java
│   └── resources/
│       └── application.yml
└── test/
    └── java/
        └── com/example/transactionstarter/
            ├── TransactionIntegrationTests.java
            └── TransactionStarterApplicationTests.java
```

---

## How to Run the Application

### Prerequisites

* Java 17 Development Kit (JDK 17)

### Building and Running

#### Windows (Command Prompt / PowerShell)

```cmd
mvnw.cmd clean test
mvnw.cmd spring-boot:run
```

#### Linux / macOS / Git Bash

```bash
./mvnw.cmd clean test
./mvnw.cmd spring-boot:run
```

### Application URLs

* **REST Base URL**: `http://localhost:8080`
* **H2 Console**: `http://localhost:8080/h2-console`
  * JDBC URL: `jdbc:h2:mem:transactions`
  * Username: `sa`
  * Password: *(blank)*

---

## API Endpoints

| Method | Endpoint | Description | Success Status |
| :--- | :--- | :--- | :--- |
| **POST** | `/api/v1/transactions` | Create a new payment transaction | `210 Created` |
| **GET** | `/api/v1/transactions/{id}` | Retrieve details of a transaction by ID | `200 OK` |
| **PATCH** | `/api/v1/transactions/{id}/status` | Update transaction status | `200 OK` |
| **GET** | `/api/v1/customers/{customerId}/transactions` | Retrieve all transactions for a customer | `200 OK` |

---

## Request / Response Examples

### 1. Create Transaction

**Request (`POST /api/v1/transactions`)**:
```json
{
  "transactionId": "TX-1001",
  "customerId": "CUST-001",
  "amount": 150.75,
  "currency": "USD",
  "transactionType": "PAYMENT",
  "description": "Online Order Payment"
}
```

*Note: If `transactionId` is omitted or blank, the service automatically generates a unique identifier prefixed with `TX-` (e.g., `TX-f47ac10b-58cc-4372-a567-0e02b2c3d479`).*

**Response (`210 Created`)**:
*Header*: `Location: /api/v1/transactions/TX-1001`
```json
{
  "transactionId": "TX-1001",
  "customerId": "CUST-001",
  "amount": 150.75,
  "currency": "USD",
  "transactionType": "PAYMENT",
  "transactionStatus": "PENDING",
  "description": "Online Order Payment",
  "createdAt": "2026-08-31T14:00:00Z",
  "updatedAt": "2026-08-31T14:00:00Z"
}
```

---

### 2. Get Transaction by ID

**Request (`GET /api/v1/transactions/TX-1001`)**

**Response (`200 OK`)**:
```json
{
  "transactionId": "TX-1001",
  "customerId": "CUST-001",
  "amount": 150.75,
  "currency": "USD",
  "transactionType": "PAYMENT",
  "transactionStatus": "PENDING",
  "description": "Online Order Payment",
  "createdAt": "2026-08-31T14:00:00Z",
  "updatedAt": "2026-08-31T14:00:00Z"
}
```

---

### 3. Update Transaction Status

**Request (`PATCH /api/v1/transactions/TX-1001/status`)**:
```json
{
  "status": "PROCESSING"
}
```

**Response (`200 OK`)**:
```json
{
  "transactionId": "TX-1001",
  "customerId": "CUST-001",
  "amount": 150.75,
  "currency": "USD",
  "transactionType": "PAYMENT",
  "transactionStatus": "PROCESSING",
  "description": "Online Order Payment",
  "createdAt": "2026-08-31T14:00:00Z",
  "updatedAt": "2026-08-31T14:05:00Z"
}
```

---

### 4. Get Customer Transactions

**Request (`GET /api/v1/customers/CUST-001/transactions`)**

**Response (`200 OK`)**:
```json
[
  {
    "transactionId": "TX-1001",
    "customerId": "CUST-001",
    "amount": 150.75,
    "currency": "USD",
    "transactionType": "PAYMENT",
    "transactionStatus": "PROCESSING",
    "description": "Online Order Payment",
    "createdAt": "2026-08-31T14:00:00Z",
    "updatedAt": "2026-08-31T14:05:00Z"
  }
]
```

---

## Validation Rules

| Field | Validation Constraints | Business Rationale |
| :--- | :--- | :--- |
| `transactionId` | Optional string; if provided, trimmed and checked for uniqueness | Prevents duplicate processing of identical transaction IDs. |
| `customerId` | Required, non-blank string (`@NotBlank`) | Ensures every transaction is linked to a valid customer. |
| `amount` | Required, positive decimal (`@NotNull`, `@DecimalMin("0.01")`) | Financial transactions must represent positive monetary values. |
| `currency` | Required, 3-letter uppercase code (`@Pattern(regexp = "^[A-Z]{3}$")`) | Enforces standard 3-letter ISO currency formatting (e.g., USD, EUR, GBP). |
| `transactionType` | Required enum (`@NotNull`, `PAYMENT`, `REFUND`, `DEPOSIT`, `WITHDRAWAL`, `TRANSFER`) | Restricts transactions to supported business types. |
| `transactionStatus` | Initialized automatically to `PENDING` on creation; valid status required on PATCH | Enforces predictable transaction lifecycle management. |

---

## Transaction Status Lifecycle

Transactions follow a strict state machine to prevent illegal status modifications (such as modifying completed or cancelled transactions):

| Current Status | Allowed Next Statuses | Terminal State? |
| :--- | :--- | :--- |
| **`PENDING`** | `PROCESSING`, `COMPLETED`, `FAILED`, `CANCELLED` | No |
| **`PROCESSING`** | `COMPLETED`, `FAILED`, `CANCELLED` | No |
| **`COMPLETED`** | *None* | **Yes** |
| **`FAILED`** | *None* | **Yes** |
| **`CANCELLED`** | *None* | **Yes** |

Attempting to transition from a terminal status (`COMPLETED`, `FAILED`, `CANCELLED`) results in an `HTTP 422 Unprocessable Entity` response.

---

## Error Handling

All exception handling is centralized in [`GlobalExceptionHandler`](file:///c:/Users/punee/Downloads/toucan-springboot-starter-main/src/main/java/com/example/transactionstarter/exception/GlobalExceptionHandler.java), returning consistent structured JSON error payloads:

```json
{
  "timestamp": "2026-08-31T14:05:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for request",
  "path": "/api/v1/transactions",
  "fieldErrors": {
    "amount": "must be greater than 0",
    "currency": "must be a 3-letter uppercase ISO code"
  }
}
```

### Handled Error Scenarios

* **`400 Bad Request`**: Validation failures (`MethodArgumentNotValidException`), malformed JSON, or missing required body (`HttpMessageNotReadableException`).
* **`404 Not Found`**: Lookup for non-existent transaction IDs (`ResourceNotFoundException`).
* **`409 Conflict`**: Attempt to create a transaction with an existing `transactionId` (`DuplicateTransactionIdException`).
* **`422 Unprocessable Entity`**: Invalid status state machine transition (`InvalidStatusTransitionException`).
* **`500 Internal Server Error`**: Unexpected uncaught server exceptions.

---

## Persistence

* **Engine**: Embedded H2 Database configured via [`application.yml`](file:///c:/Users/punee/Downloads/toucan-springboot-starter-main/src/main/resources/application.yml).
* **Repository**: [`TransactionRepository`](file:///c:/Users/punee/Downloads/toucan-springboot-starter-main/src/main/java/com/example/transactionstarter/repository/TransactionRepository.java) extending `JpaRepository<Transaction, String>`.
* **Automatic Timestamps**: Entity uses JPA `@PrePersist` and `@PreUpdate` callbacks to populate `createdAt` and `updatedAt` timestamps.

---

## Testing

The automated test suite provides integration coverage using `MockMvc` and Spring Boot context verification.

### Test Execution Command

```cmd
mvnw.cmd clean test
```

### Actual Final Test Results

```text
[INFO] Running com.example.transactionstarter.TransactionIntegrationTests
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 9.408 s -- in com.example.transactionstarter.TransactionIntegrationTests
[INFO] Running com.example.transactionstarter.TransactionStarterApplicationTests
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.918 s -- in com.example.transactionstarter.TransactionStarterApplicationTests
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

### Test Suite Coverage

1. `test1_SuccessfulTransactionCreation`: Verifies transaction creation, auto-assignment of `PENDING` status, `Location` header, and DB persistence.
2. `test2_InvalidTransactionRejected`: Verifies validation rejection for negative amounts and invalid currency formats (`400 Bad Request`).
3. `test3_DuplicateTransactionIdRejected`: Verifies conflict handling when creating duplicate `transactionId` (`409 Conflict`).
4. `test4_NonExistentTransactionReturns404`: Verifies resource lookup failure handling (`404 Not Found`).
5. `test5_SuccessfulStatusUpdate`: Verifies valid multi-step status transition (`PENDING` -> `PROCESSING` -> `COMPLETED`).
6. `test6_InvalidStatusTransitionRejected`: Verifies enforcement of state machine rules on terminal status (`422 Unprocessable Entity`).
7. `test7_GetCustomerTransactionsMultipleResults`: Verifies customer transaction list filtering across multiple saved records.
8. `test8_GetCustomerTransactionsEmptyResult`: Verifies querying non-existent customer IDs returns an empty array with `200 OK`.
9. `contextLoads`: Verifies Spring ApplicationContext boots cleanly.

---

## Design Decisions

* **Layered Architecture**: Strict separation of concerns across Controller, Service, Repository, and Entity layers.
* **DTO Decoupling**: Request (`CreateTransactionRequest`, `UpdateStatusRequest`) and Response (`TransactionResponse`) DTOs prevent exposing internal entity models.
* **Precision Handling**: Use of `BigDecimal` for monetary amounts to avoid binary floating-point rounding errors.
* **Domain-Driven Enums**: `TransactionType` and `TransactionStatus` encapsulate valid system states and state transition business rules.
* **Centralized Global Exception Handling**: `@RestControllerAdvice` ensures standard error responses across all REST endpoints.

---

## Assumptions

1. `transactionId` is optional on creation; if omitted, the system generates a unique `TX-<UUID>` string.
2. `customerId` filtering returns an empty array `[]` with HTTP 200 when no transactions exist for that customer.
3. Terminal states (`COMPLETED`, `FAILED`, `CANCELLED`) are permanent and cannot transition to any other status.
4. Currency codes are constrained to 3-letter ISO uppercase format.

---

## Limitations / Future Improvements

* **In-Memory Storage**: Uses H2 in-memory database for rapid demonstration; production environments should configure PostgreSQL or MySQL with persistent storage and Liquibase/Flyway migrations.
* **Security & Auth**: Authentication (OAuth2/JWT) and rate limiting are out of assignment scope and should be added for production API gateway deployment.
* **Pagination**: Customer transaction history returns a full list; adding Spring Data `Pageable` support would optimize large customer histories.
