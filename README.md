# Spring Modulith Order Platform

[![CI](https://github.com/DanieleMasone/spring-modulith-order-platform/actions/workflows/ci.yml/badge.svg)](https://github.com/DanieleMasone/spring-modulith-order-platform/actions/workflows/ci.yml)
![Java](https://img.shields.io/badge/Java-21-000?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1.0-6DB33F?logo=springboot)
![Spring Modulith](https://img.shields.io/badge/Spring_Modulith-2.0.6-6DB33F)
![OpenAPI](https://img.shields.io/badge/OpenAPI-Contract_First-85EA2D?logo=openapiinitiative)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-4169E1?logo=postgresql)

Production-oriented modular monolith for order management. The repository demonstrates modular design, contract-first APIs, domain events, architecture governance, reliable PostgreSQL persistence and automated documentation without adding distributed infrastructure that this use case does not need.

For local setup, API examples, architecture details and troubleshooting, see the [published User Guide](https://danielemasone.github.io/spring-modulith-order-platform/user-guide/).

## Architecture

The application is one Spring Boot 4 deployable governed by Spring Modulith.

```text
com.example.orderplatform

|-- customers
|-- pricing
|-- orders
|-- payments
`-- notifications
```

Each business module uses only the packages it needs from this pragmatic layout:

```text
module

|-- api
|-- application
|-- domain
`-- infrastructure
```

The `api` package is the public module surface. Spring Modulith `@NamedInterface("api")` annotations make those surfaces explicit, and module-level `@ApplicationModule(allowedDependencies = ...)` declarations keep dependencies intentional. `ApplicationModules.of(OrderPlatformApplication.class).verify()` is part of the test suite, so illegal cross-module dependencies fail the build.

The project follows a pragmatic ports-and-adapters style inside each Spring Modulith module: domain logic remains framework-independent, application services orchestrate use cases, and infrastructure adapters contain persistence and technical integrations. Outbound ports are introduced only where they keep Spring Data and JPA entities out of application services; the package layout remains `api`, `application`, `domain` and `infrastructure` instead of adding cosmetic `ports` or `adapters` folders.

## Module Collaboration

Orders uses synchronous module API dependencies where immediate consistency is required:

* `customers :: api` validates that a customer exists.
* `pricing :: api` quotes products before an order is accepted.

Downstream collaboration uses domain events:

* `OrderCreatedEvent` prepares a payment record and creates a customer notification.
* `PaymentAuthorizedEvent` creates an operational notification.

JPA entities and repositories stay inside module `infrastructure` packages. Public module APIs expose records and interfaces, not persistence types.

## OpenAPI First

The source contract is:

```text
src/main/resources/openapi/order-platform-api.json
```

Maven generates Java interfaces and DTOs during `generate-sources`. Controllers implement the generated interfaces and map requests to module use cases. Generated Java sources are not committed.

After `./mvnw clean verify`, Maven also generates:

```text
target/generated-docs/openapi/index.html
target/generated-docs/openapi/openapi.json
```

## Persistence

The application uses PostgreSQL, Spring Data JPA and Flyway. PostgreSQL is also used in integration tests through Testcontainers. Hibernate validates the schema and does not generate it.

```text
src/main/resources/db/migration
|-- V1__initial_schema.sql
`-- V2__seed_price_catalog.sql
```

The seed data is limited to the reference price catalog required for realistic order and pricing flows.

## API Surface

The main workflow endpoints are:

```text
POST /customers
POST /pricing/quote
POST /orders
POST /payments/authorize
GET  /notifications
```

The contract also exposes read endpoints for customer, order and payment lookup. Validation, not-found, conflict and business-rule failures are returned as RFC7807 `ProblemDetail` responses.

See the [User Guide](https://danielemasone.github.io/spring-modulith-order-platform/user-guide/) for practical cURL examples.

## Testing Strategy

The build includes:

* Domain unit tests for customer registration, pricing rules, order creation, payment authorization and notification drafts.
* Application tests for use case orchestration, duplicate-customer conflicts and module API behavior.
* Spring Boot integration tests with Testcontainers and PostgreSQL for REST happy paths, validation failures, not-found failures, business-rule failures, persistence and event consumption.
* Spring Modulith architecture verification through `ApplicationModules.of(OrderPlatformApplication.class).verify()`.
* Maven artifact checks for JaCoCo, Javadoc, OpenAPI documentation and the GitHub Pages artifact.

The project intentionally has no H2 dependency.

## Generated Documentation

Maven is the single assembly path. Running `./mvnw clean verify` copies the maintained landing page, HTML User Guide and shared site assets, generates the reports and assembles the Pages artifact:

* [GitHub Pages landing page](https://danielemasone.github.io/spring-modulith-order-platform/)
* [User Guide](https://danielemasone.github.io/spring-modulith-order-platform/user-guide/)
* [OpenAPI documentation](https://danielemasone.github.io/spring-modulith-order-platform/openapi/)
* [OpenAPI JSON](https://danielemasone.github.io/spring-modulith-order-platform/openapi/openapi.json)
* [Javadoc](https://danielemasone.github.io/spring-modulith-order-platform/javadoc/)
* [JaCoCo coverage](https://danielemasone.github.io/spring-modulith-order-platform/jacoco/)

The maintained site sources are `src/site/index.html`, `src/site/user-guide/index.html` and `src/site/assets/`. GitHub Pages publishes only `target/pages`; generated reports, copied site output and generated OpenAPI sources are not committed.

## Local Development

Quick start:

```bash
docker compose up -d
./mvnw clean verify
./mvnw spring-boot:run
```

The [published User Guide](https://danielemasone.github.io/spring-modulith-order-platform/user-guide/) contains the full local workflow, sample requests and troubleshooting notes. Maven copies its maintained HTML source into the Pages artifact.

## Docker Usage

Build verification should happen before image creation:

```bash
./mvnw clean verify
docker build -t spring-modulith-order-platform:local .
```

The Dockerfile packages the already-built jar and does not run tests again.

## CI/CD

The GitHub Actions workflow uses Java 21 with Maven cache, runs exactly one Maven `clean verify`, validates generated coverage, Javadoc, OpenAPI, User Guide and Pages artifacts, validates Docker Compose, builds the Docker image after tests pass, uploads only `target/pages` and deploys GitHub Pages through the official Pages Actions flow.

## Excluded Technologies

The repository intentionally excludes Kubernetes, microservices, Redis, Kafka, OAuth2, CQRS frameworks, outbox pattern, Arquillian and schema registry. These technologies can be useful in other systems, but they would add operational and conceptual weight without improving this modular monolith.

## Design Trade-Offs

* The service favors a modular monolith over microservices so module boundaries can be demonstrated without network hops, distributed transactions or deployment sprawl.
* Domain events are used for post-commit payment and notification workflows, while customer validation and pricing stay synchronous because the order cannot be accepted without them.
* OpenAPI-first generation is limited to REST contracts. Internal module APIs remain hand-written records and interfaces so generated DTOs do not leak into the domain model.
* PostgreSQL and Flyway are used in both runtime and integration tests to keep schema behavior realistic. H2 is deliberately excluded.
* The project does not include an outbox, external broker or production notification adapters. The current notification module records notification intents inside the monolith.
