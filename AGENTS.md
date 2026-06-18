# AGENTS.md

## Project Goal

Build and maintain a production-oriented modular monolith order management platform suitable for senior backend and architecture interviews. The repository should demonstrate disciplined engineering choices, not tutorial code or artificial enterprise complexity.

## Architecture Principles

* The application is a single Spring Boot 4 deployable governed by Spring Modulith.
* Business modules are direct packages below `com.example.orderplatform`: `customers`, `pricing`, `orders`, `payments`, `notifications`.
* Each module may use `api`, `application`, `domain` and `infrastructure` packages. Do not add layers unless they remove real complexity.
* Use a pragmatic ports-and-adapters style inside those packages: domain logic remains framework-independent, application services orchestrate use cases, and infrastructure adapters contain persistence and technical integrations.
* Introduce outbound ports only for meaningful technical boundaries, such as keeping Spring Data repositories and JPA entities out of application services. Do not add cosmetic `ports` or `adapters` package trees.
* Cross-module references must go through named API interfaces or domain events.
* Module dependency rules live in module-level `package-info.java` files and must remain enforceable by `ApplicationModules.verify()`.
* Domain events are used for meaningful collaboration after state changes. Do not create decorative event chains.

## Generated Files Policy

Do not commit generated files from:

* `target/`
* `target/generated-sources/openapi/`
* `target/generated-docs/openapi/`
* `target/site/jacoco/`
* `target/pages/`

Generated OpenAPI interfaces and DTOs must come from the contract in `src/main/resources/openapi`.

## Documentation Generation Policy

Maven is the source of truth. `./mvnw clean verify` must generate:

* `target/site/jacoco/`
* `target/site/apidocs/`
* `target/generated-docs/openapi/index.html`
* `target/generated-docs/openapi/openapi.json`
* `target/pages/`

Maintain only `src/site/index.html` for the landing page. Do not create or commit duplicate HTML pages.

Manually maintained Markdown under `docs/` is allowed when it provides practical guidance that is not duplicated from generated OpenAPI, Javadoc or JaCoCo reports. OpenAPI HTML, Javadoc and JaCoCo remain Maven-generated artifacts and must not be committed.

## Testing Policy

* Unit tests cover business rules and value objects.
* Integration tests use Testcontainers with PostgreSQL.
* Do not add H2.
* Architecture tests must fail the build when module boundaries are violated.
* Keep tests focused on behavior and architectural risk.

## CI/CD Policy

The CI workflow must:

* Run a single Maven `clean verify`.
* Verify generated artifacts.
* Validate Docker Compose.
* Build the Docker image after tests pass.
* Publish only `target/pages`, including Javadoc under `target/pages/javadoc/`, to GitHub Pages.

Avoid duplicate Maven executions and unnecessary matrix builds.

## Excluded Technologies

Do not introduce Kubernetes, microservices, Redis, Kafka, OAuth2, CQRS frameworks, outbox pattern, Arquillian or schema registry unless the project goals are explicitly changed.

## Coding Conventions

* Use Java 21 language features conservatively.
* Prefer records for immutable API and domain snapshots.
* Keep JPA entities inside module `infrastructure` packages.
* Use RFC7807 `ProblemDetail` for REST errors.
* Use constructor injection.
* Keep public types in module API packages intentionally small.
* Do not duplicate generated DTOs manually.

## Versioned Tooling

Keep Maven wrapper scripts and `.mvn/wrapper/maven-wrapper.jar` versioned so local and CI builds can run without a preinstalled Maven distribution.
