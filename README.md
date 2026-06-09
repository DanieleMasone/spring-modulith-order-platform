# Spring Modulith Order Platform

[![CI](https://github.com/DanieleMasone/spring-modulith-order-platform/actions/workflows/ci.yml/badge.svg)](https://github.com/DanieleMasone/spring-modulith-order-platform/actions/workflows/ci.yml)
![Java](https://img.shields.io/badge/Java-21-000?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.x-6DB33F?logo=springboot)
![Spring Modulith](https://img.shields.io/badge/Spring_Modulith-2.x-6DB33F)
![OpenAPI](https://img.shields.io/badge/OpenAPI-Contract_First-85EA2D?logo=openapiinitiative)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-4169E1?logo=postgresql)

Production-oriented modular monolith for order management. The project is designed as a senior backend portfolio repository: it demonstrates modular design, contract-first APIs, domain events, architecture governance, reliable persistence and automated documentation without pretending that every business problem needs distributed systems.

## Architecture

The application is a single deployable Spring Boot 4 service governed by Spring Modulith.

```text
com.example.orderplatform

├── customers
├── pricing
├── orders
├── payments
└── notifications
```

Each business module uses the same pragmatic package layout:

```text
module

├── api
├── application
├── domain
└── infrastructure
```

The `api` package is the public module surface. Spring Modulith `@NamedInterface("api")` annotations make those surfaces explicit, and `@ApplicationModule(allowedDependencies = ...)` declarations keep dependencies intentional.

## Module Collaboration

Orders uses synchronous API dependencies only where the business needs immediate consistency:

* `customers :: api` validates that a customer exists.
* `pricing :: api` quotes products before an order is accepted.

Downstream collaboration uses domain events:

* `OrderCreatedEvent` prepares a payment record and creates a customer notification.
* `PaymentAuthorizedEvent` creates an operational notification.

No Kafka, Redis, outbox pattern or schema registry is introduced because those would add operational weight without improving this use case.

## OpenAPI First

The API contract lives under:

```text
src/main/resources/openapi/order-platform-api.json
```

Maven generates Java interfaces and DTOs from that contract during `generate-sources`. Controllers implement generated interfaces and map to module use cases. Generated sources are not committed.

## Persistence

The application uses PostgreSQL, Spring Data JPA and Flyway. Hibernate validates the schema; it does not generate it.

```text
src/main/resources/db/migration
├── V1__initial_schema.sql
└── V2__seed_price_catalog.sql
```

## Testing

The build includes:

* Unit tests for domain rules.
* Integration tests with Spring Boot, Testcontainers and PostgreSQL.
* Architecture tests with `ApplicationModules.of(OrderPlatformApplication.class).verify()`.

The project intentionally has no H2 dependency.

## Generated Documentation

Maven is the single source of truth. Running:

```bash
./mvnw clean verify
```

generates:

```text
target/site/jacoco/
target/generated-docs/openapi/
target/pages/
```

GitHub Pages publishes only `target/pages`, which contains the landing page, JaCoCo report, OpenAPI HTML documentation and OpenAPI JSON contract.

## Local Development

Start PostgreSQL:

```bash
docker compose up -d
```

Run the full verification:

```bash
./mvnw clean verify
```

Run the application:

```bash
./mvnw spring-boot:run
```

Useful endpoints:

```text
POST /customers
POST /pricing/quote
POST /orders
POST /payments/authorize
GET  /notifications
```

## Docker

Build verification happens before image creation:

```bash
./mvnw clean verify
docker build -t order-platform:local .
```

The Dockerfile packages the already-built jar and does not run tests again.

## CI/CD

The GitHub Actions workflow performs one Maven verification, validates generated artifacts, validates Docker Compose, builds the Docker image and uploads `target/pages` for GitHub Pages deployment.

## Excluded Technologies

The repository intentionally excludes Kubernetes, microservices, Redis, Kafka, OAuth2, CQRS frameworks, outbox pattern, Arquillian and schema registry. The architecture stays focused on maintainability, modularity and engineering quality.
