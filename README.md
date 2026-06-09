# Spring Modulith Order Platform

[![CI](https://github.com/DanieleMasone/spring-modulith-order-platform/actions/workflows/ci.yml/badge.svg)](https://github.com/YOUR_USERNAME/spring-modulith-order-platform/actions/workflows/ci.yml)
![Java](https://img.shields.io/badge/Java-21-000?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.x-6DB33F?logo=springboot)
![Spring Modulith](https://img.shields.io/badge/Spring_Modulith-1.x-6DB33F)
![OpenAPI](https://img.shields.io/badge/OpenAPI-Contract_First-85EA2D?logo=openapiinitiative)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-4169E1?logo=postgresql)
![License](https://img.shields.io/badge/License-MIT-blue.svg)

Production-oriented modular monolith built with Spring Boot, Spring Modulith, OpenAPI-first APIs, PostgreSQL, Flyway, Testcontainers and domain events.

The project demonstrates how to build a maintainable and scalable business platform using a modular monolith architecture, explicit module boundaries, contract-driven APIs, automated architecture verification and reproducible builds.

---

# Architecture

The platform manages the lifecycle of customer orders through a set of independent business modules.

```text
Customer
    │
    ▼
Orders
    │
    ├──────────────► Pricing
    │
    ├──────────────► Payments
    │
    ├──────────────► Fulfillment
    │
    └──────────────► Notifications
```

Modules collaborate primarily through domain events to minimize coupling and preserve modularity.

---

# Module Structure

```text
com.example.orderplatform

├── orders
├── customers
├── pricing
├── payments
├── fulfillment
└── notifications
```

Each module follows a consistent layered structure:

```text
module

├── api
├── application
├── domain
└── infrastructure
```

| Layer          | Responsibility                          |
| -------------- | --------------------------------------- |
| api            | REST endpoints, DTOs, OpenAPI contracts |
| application    | Use cases, orchestration, transactions  |
| domain         | Business rules, aggregates, events      |
| infrastructure | Persistence and technical integrations  |

---

# Why Spring Modulith

This project intentionally adopts a modular monolith architecture.

Benefits:

* Single deployment unit
* Strong transactional consistency
* Lower operational complexity
* Explicit module boundaries
* Easier maintainability
* Future microservice extraction path

Why not microservices?

The business complexity demonstrated by this project does not justify the operational overhead of distributed systems, service discovery, network resiliency concerns and multi-service deployments.

---

# Spring Modulith Verification

Spring Modulith is used as an architectural governance tool.

Example:

```java
ApplicationModules.of(Application.class)
                  .verify();
```

The build fails when:

* module boundaries are violated
* illegal dependencies are introduced
* architecture rules are broken

Architecture validation is executed automatically during CI.

---

# OpenAPI First

The platform follows a contract-first development approach.

Contracts are defined before implementation.

Location:

```text
src/main/resources/openapi
```

Examples:

```text
orders-api.yaml
customers-api.yaml
payments-api.yaml
```

Generated artifacts include:

* API contracts
* DTOs
* Interfaces
* OpenAPI documentation

Benefits:

* Consistent API design
* Explicit contracts
* Easier versioning
* Improved collaboration

---

# Domain Events

Modules collaborate through domain events whenever possible.

Example:

```text
Order Created
      │
      ▼
OrderCreatedEvent
      │
      ├── Pricing
      ├── Payments
      └── Notifications
```

Advantages:

* Reduced coupling
* Clear module responsibilities
* Improved modularity
* Easier future service extraction

---

# Persistence

Database:

```text
PostgreSQL
```

Database migrations:

```text
Flyway
```

Migration location:

```text
src/main/resources/db/migration
```

Example:

```text
V1__initial_schema.sql
V2__orders.sql
V3__payments.sql
```

---

# Testing Strategy

The project uses multiple testing layers.

## Unit Tests

Focus:

* Domain logic
* Aggregates
* Value objects
* Business rules

Tools:

* JUnit 5
* Mockito

## Integration Tests

Focus:

* Persistence
* REST APIs
* Transactions
* Event flows

Tools:

* Spring Boot Test
* Testcontainers
* PostgreSQL

## Architecture Tests

Focus:

* Module boundaries
* Dependency validation
* Architectural governance

Tools:

* Spring Modulith

---

# Error Handling

REST APIs use RFC 7807 Problem Details.

Example:

```json
{
  "type": "about:blank",
  "title": "Order Not Found",
  "status": 404,
  "detail": "Order 123 was not found"
}
```

---

# Observability

The project includes lightweight production-oriented observability.

Features:

* Structured logging
* Correlation IDs
* Spring Boot Actuator
* Health checks
* Metrics readiness

Technology stack:

```text
Micrometer
Actuator
```

Heavy observability infrastructure is intentionally excluded to keep the project focused and maintainable.

---

# Generated Documentation

Maven is the single source of truth.

Running:

```bash
./mvnw clean verify
```

generates:

```text
target/
├── site/
│   └── jacoco/
│
├── generated-docs/
│   └── openapi/
│
└── pages/
```

No generated documentation is committed to Git.

---

# GitHub Pages

GitHub Pages publishes only:

```text
target/pages
```

The landing page provides access to:

* Project overview
* Coverage report
* OpenAPI documentation
* OpenAPI JSON
* Repository links

All published assets are generated automatically during the build.

---

# Local Development

Start infrastructure:

```bash
docker compose up -d
```

Run verification:

```bash
./mvnw clean verify
```

Start the application:

```bash
./mvnw spring-boot:run
```

---

# Docker

Docker is used for:

* Local development
* Reproducible environments
* Packaging validation

Tests are executed before image creation through:

```bash
./mvnw clean verify
```

Docker builds do not re-run test suites.

---

# Technology Stack

| Area          | Technology      |
| ------------- | --------------- |
| Language      | Java 21         |
| Framework     | Spring Boot     |
| Modularity    | Spring Modulith |
| API Contracts | OpenAPI         |
| Database      | PostgreSQL      |
| Migration     | Flyway          |
| Testing       | JUnit 5         |
| Containers    | Testcontainers  |
| Build         | Maven           |
| CI/CD         | GitHub Actions  |
| Documentation | GitHub Pages    |
| Coverage      | JaCoCo          |

---

# CI Pipeline

The CI pipeline validates:

```text
Checkout
Setup Java
Maven Verify
Verify Generated Artifacts
Docker Compose Validation
Docker Image Build
Upload Pages Artifact
Deploy Pages
```

The pipeline avoids duplicated Maven executions and does not commit generated artifacts.

---

# Design Trade-Offs

## Chosen

* Modular Monolith
* OpenAPI First
* Domain Events
* PostgreSQL
* Flyway
* Testcontainers
* Architecture Verification

## Intentionally Deferred

The following technologies are intentionally excluded until justified by concrete requirements:

* Microservices
* Kubernetes
* Service Mesh
* Outbox Pattern
* Event Publication Registry
* Redis Caching
* OAuth2 Resource Server
* CQRS Read Models
* OpenTelemetry Infrastructure
* Distributed Tracing Platforms

The objective is to demonstrate production-oriented software engineering with appropriate complexity rather than artificial enterprise features.

---

# Repository Principles

The project follows these principles:

* API First
* Contract Driven Development
* Modular Monolith First
* Event-Driven Collaboration
* Architecture as Code
* Build Reproducibility
* Documentation Automation
* Production-Ready Engineering

The goal is to demonstrate how modern enterprise applications can remain modular, maintainable and scalable without prematurely adopting a distributed architecture.
