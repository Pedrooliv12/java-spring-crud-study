<div align="center">

# 📦 api-product

A RESTful product API built with Spring Boot — secured with JWT, versioned with Flyway, tested with Testcontainers, and shipped through a full CI/CD pipeline.

> ⚠️ **This repository is for personal study purposes.**
> Built to learn Java and the Spring ecosystem from the ground up — without Lombok or other code-generation shortcuts.

[![CI](https://github.com/Pedrooliv12/java-spring-crud-study/actions/workflows/ci.yml/badge.svg)](https://github.com/Pedrooliv12/java-spring-crud-study/actions/workflows/ci.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Pedrooliv12_java-spring-crud-study&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=Pedrooliv12_java-spring-crud-study)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=Pedrooliv12_java-spring-crud-study&metric=coverage)](https://sonarcloud.io/summary/new_code?id=Pedrooliv12_java-spring-crud-study)
[![Docker Image](https://img.shields.io/docker/image-size/pedrooliv12/api-product/latest?label=docker%20image&logo=docker)](https://hub.docker.com/r/pedrooliv12/api-product)

[![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.2-brightgreen?style=flat-square&logo=springboot)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?style=flat-square&logo=postgresql)](https://www.postgresql.org/)

</div>

---

## 📋 Table of Contents

- [About](#-about)
- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
- [Authentication](#-authentication)
- [API Reference](#-api-reference)
- [API Documentation](#-api-documentation)
- [Database Migrations](#-database-migrations)
- [Tests](#-tests)
- [CI/CD Pipeline](#-cicd-pipeline)
- [Code Quality & Security](#-code-quality--security)
- [Sobre o Projeto (PT-BR)](#-sobre-o-projeto-pt-br)

---

## 📖 About

A product management REST API developed as a hands-on study of the Spring Boot ecosystem. The goal is to understand what happens under the hood and to apply the practices expected in a professional codebase:

- layered architecture (controller → service → repository) with DTOs and centralized error handling;
- stateless authentication with JWT and BCrypt-hashed passwords;
- database schema versioned with Flyway and validated by Hibernate;
- integration tests against a real PostgreSQL (Testcontainers) and unit tests with Mockito;
- a CI/CD pipeline that tests, analyzes and publishes a Docker image on every push.

---

## 🛠 Tech Stack

| Area | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.1.2 (Web MVC, Data JPA, Validation, Security) |
| Persistence | PostgreSQL 16, Hibernate, Flyway |
| Security | Spring Security, JJWT (JWT), BCrypt |
| Mapping | MapStruct |
| API docs | springdoc-openapi (Swagger UI) |
| Tests | JUnit 5, MockMvc, Testcontainers, Mockito, AssertJ, JaCoCo |
| Containers | Docker (multistage build), Docker Compose |
| CI/CD | GitHub Actions, Docker Hub |
| Quality & security | SonarQube Cloud, Snyk |
| Build | Maven (wrapper included) |

---

## 🗂 Project Structure

```
api-product/
├── .github/workflows/ci.yml          # CI/CD pipeline: test, analyze, publish image
├── Dockerfile                        # Multistage build: JDK + Maven → JRE only, non-root user
├── docker-compose.yaml               # PostgreSQL + API
├── .env.example                      # Template for the local .env (never commit .env)
└── src/
    ├── main/
    │   ├── java/com/example/api_product/
    │   │   ├── config/               # OpenAPI (Swagger) configuration
    │   │   ├── controller/           # HTTP layer: ProductController, AuthController
    │   │   ├── dto/                  # Request/response records with Bean Validation
    │   │   ├── exception/            # Custom exceptions + GlobalExceptionHandler
    │   │   ├── mapper/               # MapStruct mapper (entity → DTO)
    │   │   ├── model/                # JPA entities: Product, User
    │   │   ├── repository/           # Spring Data JPA repositories
    │   │   ├── security/             # SecurityConfig, JwtFilter, JwtUtil
    │   │   └── service/              # Business logic: ProductService, AuthService
    │   └── resources/
    │       ├── application.properties
    │       └── db/migration/         # Flyway migrations (V1, V2, V3...)
    └── test/java/com/example/api_product/
        ├── *IntegrationTest.java     # Integration tests (MockMvc + Testcontainers)
        ├── security/                 # Unit tests for JwtUtil
        └── service/                  # Unit tests for services (Mockito)
```

---

## 🚀 Getting Started

### Prerequisites

- Java 21+
- Docker (Docker Desktop on Windows/macOS)

Maven is not required: the project ships with the Maven wrapper (`./mvnw`).

### 1. Clone and configure the environment

```bash
git clone https://github.com/Pedrooliv12/java-spring-crud-study.git
cd java-spring-crud-study

# Create your local environment file from the template
cp .env.example .env

# Generate a random 256-bit key and paste it after JWT_SECRET= in .env
openssl rand -base64 32
```

| Variable | Required | Description |
|---|---|---|
| `JWT_SECRET` | yes | Base64-encoded 256-bit key used to sign JWT tokens |

> The `.env` file is git-ignored and must never be committed. Without `JWT_SECRET` the application refuses to start.

### 2a. Run everything with Docker Compose

```bash
docker compose up -d --build
```

This builds the API image and starts it together with PostgreSQL. The API waits for the database health check before starting and is available at `http://localhost:8080`.

### 2b. Or run the API locally

```bash
# Start only the database
docker compose up -d postgres

# Run the API with the Maven wrapper
./mvnw spring-boot:run
```

> **Note:** the database runs in a container. `docker compose down` removes the container and its data; Flyway recreates the schema on the next start.

### Published image

Every push to `main` that passes the pipeline publishes an image to Docker Hub:

```bash
docker pull pedrooliv12/api-product:latest
```

---

## 🔐 Authentication

All `/products` endpoints require a JWT token. Get one by registering or logging in, then send it in the `Authorization` header.

```bash
# 1. Register (returns a token)
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email": "user@example.com", "password": "password123"}'

# 2. Or log in with an existing account
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "user@example.com", "password": "password123"}'

# Response
# {"token": "eyJhbGciOiJIUzI1NiJ9..."}

# 3. Call a protected endpoint
curl http://localhost:8080/products \
  -H "Authorization: Bearer <token>"
```

- Tokens expire after **1 hour**.
- Passwords are stored as **BCrypt** hashes, never in plain text.
- Login returns the **same 401 message** for a wrong password and for an unknown email, so the API never reveals which emails are registered.

---

## 📡 API Reference

### Endpoints

| Method | Endpoint | Auth | Success | Possible errors |
|---|---|---|---|---|
| `POST` | `/auth/register` | public | `201` `{token}` | `400`, `409` |
| `POST` | `/auth/login` | public | `200` `{token}` | `400`, `401` |
| `GET` | `/products` | Bearer | `200` list | `401` |
| `GET` | `/products/{id}` | Bearer | `200` product | `401`, `404` |
| `POST` | `/products` | Bearer | `201` product | `400`, `401` |
| `PUT` | `/products/{id}` | Bearer | `200` product | `400`, `401`, `404` |
| `DELETE` | `/products/{id}` | Bearer | `204` | `401`, `404` |

### Request bodies and validation

**Register** — `POST /auth/register`
```json
{ "email": "user@example.com", "password": "password123" }
```
`email` must be a valid email; `password` must have between 8 and 72 characters.

**Login** — `POST /auth/login`
```json
{ "email": "user@example.com", "password": "password123" }
```

**Product** — `POST /products` and `PUT /products/{id}`
```json
{ "name": "Keyboard", "price": 49.99, "stockQuantity": 10 }
```

| Field | Rules |
|---|---|
| `name` | required, not blank |
| `price` | required, greater than 0, at most 8 integer digits and 2 decimal places |
| `stockQuantity` | required, zero or positive |

**Product response**
```json
{ "id": 1, "name": "Keyboard", "price": 49.99 }
```
> `stockQuantity` is stored but not exposed in responses.

### Error responses

| Status | When | Body |
|---|---|---|
| `400` | Invalid request body | one entry per invalid field, e.g. `{"price": "must be greater than 0"}` |
| `401` | Missing or invalid token | `{"error": "Authentication required"}` |
| `401` | Wrong email or password on login | `{"error": "Invalid email or password"}` |
| `404` | Product not found | `{"error": "Product not found with id: 999"}` |
| `409` | Email already registered | `{"error": "Email already exists"}` |

---

## 📄 API Documentation

Interactive documentation is generated by springdoc-openapi:

| Interface | URL |
|---|---|
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` |

To call protected endpoints from Swagger UI: register or log in, copy the token, click **Authorize** and paste **only the token** (Swagger adds the `Bearer ` prefix).

---

## 🗄 Database Migrations

The schema is managed by **Flyway**. Hibernate runs with `ddl-auto=validate`: it never changes the database, it only checks that the entities match the tables.

| Version | Migration |
|---|---|
| V1 | Create `products` table |
| V2 | Create `tb_users` table (unique email) |
| V3 | Change `price` to `NUMERIC(10, 2)` (exact decimal for money) |

New schema changes go in a **new** file (`V4__...sql`). A migration that has already run must never be edited: Flyway stores its checksum and refuses to start if it changes.

---

## 🧪 Tests

```bash
./mvnw verify
```

> Docker must be running: integration tests start a real PostgreSQL with Testcontainers.

| Class | Type | Tests | Covers |
|---|---|---|---|
| `ApiProductApplicationTests` | Integration | 13 | Product validation and not-found cases |
| `ProductIntegrationTest` | Integration | 3 | Create, find, update and delete flows |
| `AuthIntegrationTest` | Integration | 8 | Register and login, including 400/401/409 |
| `SecurityIntegrationTest` | Integration | 3 | 401 without/with invalid token, public API docs |
| `JwtUtilTest` | Unit | 4 | Token generation, validation and signature check |
| `AuthServiceTest` | Unit (Mockito) | 5 | Password hashing, duplicate email, login failures |
| `ProductServiceTest` | Unit (Mockito) | 7 | CRUD logic and not-found handling |
| **Total** | | **43** | |

The coverage report is generated by JaCoCo at `target/site/jacoco/index.html`.

---

## ⚙️ CI/CD Pipeline

Every push to `main` runs the [CI workflow](.github/workflows/ci.yml):

```
git push
   │
   ▼
Job "test"
   ├── build with JDK 21 (Maven cache)
   ├── run the 43 tests (PostgreSQL via Testcontainers)
   ├── measure coverage (JaCoCo)
   └── send the analysis to SonarQube Cloud
   │
   ▼  only if the test job passes, and only on push to main
Job "docker"
   ├── multistage build of the image
   └── push to Docker Hub: pedrooliv12/api-product:latest and :<commit-sha>
```

Pull requests run the tests and the analysis but never publish an image.

---

## 🛡 Code Quality & Security

- **SonarQube Cloud** analyzes every push (bugs, code smells, duplication, coverage, security) and enforces a Quality Gate.
- **Snyk** monitors the dependencies and the base image daily for known vulnerabilities.

Security decisions in this project:

| Decision | Reason |
|---|---|
| CSRF protection disabled | Stateless API; the JWT travels in the `Authorization` header, which browsers never send automatically. Must be re-enabled if the token is ever stored in a cookie. |
| `JWT_SECRET` from the environment | Secrets are never committed; CI generates a throwaway key for each test run. |
| Generic login error | The same 401 message for wrong password and unknown email prevents user enumeration. |
| Container runs as a non-root user | Limits the impact if the application is ever compromised. |
| Third-party GitHub Actions pinned by commit SHA | A moved tag cannot run unreviewed code with access to repository secrets. |
| Jackson versions overridden in `pom.xml` | Temporary fix for a critical CVE until a Spring Boot release ships the patched versions. |

---

## 🇧🇷 Sobre o Projeto (PT-BR)

Este repositório é destinado a **estudos pessoais** de Java com Spring Boot. O objetivo é entender o que acontece por baixo das abstrações e aplicar as práticas esperadas em um projeto profissional, sem ferramentas de geração de código como o Lombok.

### Conceitos estudados

**API e arquitetura**
- Arquitetura em camadas (controller → service → repository) e injeção de dependências por construtor
- DTOs com Java Records e validação com Bean Validation (`@NotBlank`, `@Email`, `@Size`, `@Digits`...)
- Tratamento de erros centralizado com `@ControllerAdvice` e exceções customizadas
- Mapeamento de objetos com MapStruct
- `BigDecimal` para valores monetários

**Segurança**
- Autenticação stateless com JWT e Spring Security
- Senhas com hash BCrypt
- Proteção contra enumeração de usuários
- Segredos fora do código (`.env` local e secrets no CI)

**Banco de dados**
- PostgreSQL com Docker
- Versionamento do schema com Flyway e validação com Hibernate

**Testes**
- Testes de integração com MockMvc e Testcontainers (PostgreSQL real)
- Testes unitários com Mockito e AssertJ
- Cobertura de testes com JaCoCo

**DevOps**
- Dockerfile multistage com usuário sem privilégios
- Docker Compose com healthcheck
- Pipeline de CI/CD no GitHub Actions
- Análise de qualidade com SonarQube Cloud e de vulnerabilidades com Snyk
- Publicação da imagem no Docker Hub
