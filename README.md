# Tonik Bank

Backend REST para gestionar cuentas bancarias y transferencias con Java 21 y Spring Boot. Las cuentas se persisten en PostgreSQL; el dominio bancario está separado de la API y de la infraestructura.

## Funcionalidad

| Método | Ruta | Descripción |
| --- | --- | --- |
| `POST` | `/api/accounts` | Abre una cuenta con saldo inicial y moneda. |
| `GET` | `/api/accounts/{accountId}` | Consulta una cuenta. |
| `POST` | `/api/accounts/{accountId}/debits` | Debita un monto. |
| `POST` | `/api/accounts/{accountId}/credits` | Acredita un monto. |
| `POST` | `/api/transfers` | Transfiere fondos entre dos cuentas. |

La capa de aplicación coordina las operaciones y sus transacciones. `Money` representa los montos con `BigDecimal` y moneda; `BankAccount` protege las reglas de saldo. PostgreSQL conserva las cuentas y Flyway administra el esquema.

## Ejecución local

Requisitos: Java 21 y Docker Desktop.

Inicia PostgreSQL y la aplicación:

```powershell
docker compose up -d
.\mvnw.cmd spring-boot:run
```

La API queda disponible en `http://localhost:8080`.

**Abrir una cuenta**

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

Guarda el `id` de la respuesta. Puedes usarlo para consultar la cuenta o ejecutar operaciones de saldo.

**Transferir entre cuentas**

Reemplaza los identificadores por los de dos cuentas existentes. Una transferencia correcta responde `204 No Content`.

```powershell
$body = @{
    sourceAccountId = 'UUID-CUENTA-ORIGEN'
    targetAccountId = 'UUID-CUENTA-DESTINO'
    amount = 25.00
    currency = 'USD'
} | ConvertTo-Json

Invoke-RestMethod `
    -Method Post `
    -Uri 'http://localhost:8080/api/transfers' `
    -ContentType 'application/json' `
    -Body $body
```

## Pruebas

```powershell
.\mvnw.cmd `
    "-Dtest=BankAccountTest,MoneyTest,Money1Test,AccountApplicationServiceTest,TransferApplicationServiceTest,TransferTransactionIntegrationTest,AccountControllerTest,AccountHttpIntegrationTest,TransferControllerTest,TransferHttpIntegrationTest,AccountJpaEntityMappingTest,PostgresAccountRepositoryTest" `
    test
```

Las pruebas de integración con PostgreSQL usan Testcontainers y requieren Docker Desktop.

## Alcance

Tonik Bank es un backend demostrativo, no un sistema bancario listo para producción. Actualmente no incluye autenticación ni autorización, historial contable de movimientos ni garantías de idempotencia para solicitudes repetidas.
