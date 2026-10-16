# Tonik Bank

A REST backend for managing bank accounts and transfers, built with Java 21 and Spring Boot. Accounts are persisted in PostgreSQL, and the banking domain is kept separate from the HTTP API and infrastructure.

## Features

| Method | Route | Description |
| --- | --- | --- |
| `POST` | `/api/accounts` | Opens an account with an initial balance and currency. |
| `GET` | `/api/accounts/{accountId}` | Retrieves an account. |
| `POST` | `/api/accounts/{accountId}/debits` | Debits an amount from an account. |
| `POST` | `/api/accounts/{accountId}/credits` | Credits an amount to an account. |
| `POST` | `/api/transfers` | Transfers funds between two accounts. |

The application layer coordinates use cases and transactions. `Money` represents amounts with `BigDecimal` and a currency; `BankAccount` enforces balance rules. PostgreSQL stores accounts, and Flyway manages the schema.

## Run locally

Requirements: Java 21 and Docker Desktop.

Start PostgreSQL and the application:

```powershell
docker compose up -d
.\mvnw.cmd spring-boot:run
```

The API is available at `http://localhost:8080`.

**Open an account**

```powershell
$body = @{
    openingBalance = 100.00
    currency = 'USD'
} | ConvertTo-Json

Invoke-RestMethod `
    -Method Post `
    -Uri 'http://localhost:8080/api/accounts' `
    -ContentType 'application/json' `
    -Body $body
```

Save the `id` from the response. Use it to retrieve the account or perform balance operations.

**Transfer between accounts**

Replace the identifiers with those of two existing accounts. A successful transfer returns `204 No Content`.

```powershell
$body = @{
    sourceAccountId = 'SOURCE-ACCOUNT-UUID'
    targetAccountId = 'TARGET-ACCOUNT-UUID'
    amount = 25.00
    currency = 'USD'
} | ConvertTo-Json

Invoke-RestMethod `
    -Method Post `
    -Uri 'http://localhost:8080/api/transfers' `
    -ContentType 'application/json' `
    -Body $body
```

## Tests

```powershell
.\mvnw.cmd `
    "-Dtest=BankAccountTest,MoneyTest,Money1Test,AccountApplicationServiceTest,TransferApplicationServiceTest,TransferTransactionIntegrationTest,AccountControllerTest,AccountHttpIntegrationTest,TransferControllerTest,TransferHttpIntegrationTest,AccountJpaEntityMappingTest,PostgresAccountRepositoryTest" `
    test
```

PostgreSQL integration tests use Testcontainers and require Docker Desktop.

## Scope

Tonik Bank is a demonstrative backend, not a production-ready banking system. It currently does not include authentication or authorization, an accounting ledger, or idempotency guarantees for repeated requests.
