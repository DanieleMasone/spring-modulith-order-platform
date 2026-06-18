# User Guide

## Overview

Spring Modulith Order Platform is a modular monolith for order management. It runs as one Spring Boot application and keeps module boundaries explicit through Spring Modulith, package-level rules and architecture tests.

Use this guide for local setup and common API workflows. For the full REST contract, use the generated OpenAPI reference instead of treating this document as endpoint documentation.

## Prerequisites

Install:

* Java 21
* Docker with Docker Compose
* A POSIX-like shell for the examples, or equivalent commands on Windows

The Maven wrapper is committed, so a local Maven installation is not required.

## Local Setup

From the repository root, verify the wrapper:

```bash
./mvnw -v
```

On Windows PowerShell, use:

```powershell
.\mvnw.cmd -v
```

## Running PostgreSQL

Start the local database:

```bash
docker compose up -d
```

The compose file starts PostgreSQL 17 with:

```text
database: order_platform
username: order_platform
password: order_platform
port:     5432
```

If port `5432` is already in use, choose another host port:

```bash
DB_PORT=15432 docker compose up -d
```

Then adjust `spring.datasource.url` when running the application outside the default configuration.

## Running The Application

Run the full build first:

```bash
./mvnw clean verify
```

This compiles the project, generates OpenAPI interfaces and DTOs, runs unit and integration tests, verifies Spring Modulith boundaries, creates JaCoCo and Javadoc reports, generates OpenAPI documentation and assembles the GitHub Pages artifact under `target/pages`.

Start the application:

```bash
./mvnw spring-boot:run
```

The API is available at:

```text
http://localhost:8080
```

## API Workflow

A typical flow is:

1. Create a customer.
2. Quote catalog prices for the desired products.
3. Create an order for that customer.
4. Authorize the payment for the submitted order amount.
5. List generated notification records.

The seed catalog includes:

```text
SKU-COFFEE-MUG  14.99 EUR
SKU-NOTEBOOK    19.99 EUR
SKU-DESK-LAMP   49.99 EUR
SKU-BACKPACK    89.00 EUR
```

## Example Requests

Create a customer:

```bash
curl -i -X POST http://localhost:8080/customers \
  -H "Content-Type: application/json" \
  -d '{
    "email": "ada.lovelace@example.com",
    "fullName": "Ada Lovelace"
  }'
```

Quote pricing:

```bash
curl -i -X POST http://localhost:8080/pricing/quote \
  -H "Content-Type: application/json" \
  -d '{
    "items": [
      { "productCode": "SKU-COFFEE-MUG", "quantity": 2 },
      { "productCode": "SKU-NOTEBOOK", "quantity": 1 }
    ]
  }'
```

Create an order using the `id` returned by the customer response:

```bash
curl -i -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "11111111-1111-4111-8111-111111111111",
    "items": [
      { "productCode": "SKU-COFFEE-MUG", "quantity": 2 },
      { "productCode": "SKU-NOTEBOOK", "quantity": 1 }
    ]
  }'
```

Authorize the payment using the `id` returned by the order response:

```bash
curl -i -X POST http://localhost:8080/payments/authorize \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "22222222-2222-4222-8222-222222222222",
    "amount": {
      "amount": 49.97,
      "currency": "EUR"
    }
  }'
```

List notifications:

```bash
curl -i http://localhost:8080/notifications
```

Validation, missing resources, duplicate customers and business-rule failures are returned as RFC7807 `ProblemDetail` responses.

## Generated Documentation

Published documentation:

* [OpenAPI documentation](https://danielemasone.github.io/spring-modulith-order-platform/openapi/)
* [OpenAPI JSON](https://danielemasone.github.io/spring-modulith-order-platform/openapi/openapi.json)
* [Javadoc](https://danielemasone.github.io/spring-modulith-order-platform/javadoc/)
* [JaCoCo coverage](https://danielemasone.github.io/spring-modulith-order-platform/jacoco/)

Local generated outputs after `./mvnw clean verify`:

```text
target/generated-docs/openapi/index.html
target/generated-docs/openapi/openapi.json
target/site/apidocs/index.html
target/site/jacoco/index.html
target/pages/index.html
```

Generated OpenAPI interfaces, DTOs and HTML reports are build artifacts and are not committed.

## Quality Gates

`./mvnw clean verify` is the main verification command. It runs:

* unit tests for business rules and value objects;
* Spring Boot integration tests with Testcontainers and PostgreSQL;
* Spring Modulith boundary verification;
* Javadoc generation;
* JaCoCo report generation;
* OpenAPI source and documentation generation;
* Maven checks that required generated artifacts exist.

H2 is intentionally excluded. PostgreSQL is used locally and in integration tests so schema behavior stays close to production.

## Troubleshooting

If PostgreSQL does not start, check that Docker is running:

```bash
docker compose ps
docker compose logs postgres
```

If port `5432` is already used, restart compose with another host port:

```bash
DB_PORT=15432 docker compose up -d
```

If tests fail because Docker is unavailable, start Docker and rerun:

```bash
./mvnw clean verify
```

If generated documentation is missing, do not create it manually. Run:

```bash
./mvnw clean verify
```

If the application cannot connect to the database, confirm the datasource URL, username and password match `docker-compose.yml`.
