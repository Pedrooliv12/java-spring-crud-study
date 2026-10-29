<div align="center">

# 📦 api-product

A lightweight RESTful CRUD API built with Spring Boot for managing products.

> ⚠️ **This repository is for personal study purposes only.**  
> Built to explore Java, Spring Boot, Spring Data JPA, and REST API design from the ground up — without magic abstractions.

[![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-brightgreen?style=flat-square&logo=springboot)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-Build-red?style=flat-square&logo=apachemaven)](https://maven.apache.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?style=flat-square&logo=postgresql)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=flat-square&logo=docker)](https://www.docker.com/)

</div>

---

## 📋 Table of Contents

- [About](#-about)
- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
- [Running Tests](#-running-tests)
- [API Documentation](#-api-documentation)
- [API Reference](#-api-reference)
- [Sobre o Projeto (PT-BR)](#-sobre-o-projeto-pt-br)

---

## 📖 About

This project is a simple Product CRUD REST API, developed as a hands-on study of the Spring Boot ecosystem. The goal is to understand what happens under the hood — JPA entity mapping, repository pattern, service layer separation, input validation, DTO pattern, and HTTP response handling — without relying on code-generation tools like Lombok.

---

## 🛠 Tech Stack

| Technology | Purpose |
|---|---|
| Java 21 | Programming language |
| Spring Boot | Application framework |
| Spring Web (MVC) | REST controller layer |
| Spring Data JPA | Data access layer |
| Hibernate | JPA implementation (ORM) |
| PostgreSQL 16 | Relational database |
| Docker Compose | Container orchestration for local development |
| Maven | Build and dependency management |

---

## 🗂 Project Structure

```
api-product/
├── docker-compose.yaml                           # PostgreSQL container definition
├── src/main/java/com/example/api_product/
│   ├── controller/
│   │   └── ProductController.java                # HTTP layer — endpoints and status codes
│   ├── service/
│   │   └── ProductService.java                   # Business logic
│   ├── repository/
│   │   └── ProductRepository.java                # Data access layer (Spring Data JPA)
│   ├── model/
│   │   └── Product.java                          # JPA entity mapped to the products table
│   ├── dto/
│   │   ├── ProductRequestDTO.java                # Input contract (record)
│   │   └── ProductResponseDTO.java               # Output contract (record)
│   ├── mapper/
│   │   └── ProductMapper.java                    # MapStruct mapper
│   └── exception/
│       ├── GlobalExceptionHandler.java           # Centralized error handling
│       └── ProductNotFoundException.java         # Custom 404 exception
```

---

## 🚀 Getting Started

### Prerequisites

- Java 21+
- Maven 3.8+
- Docker

### Running the application

```bash
# Clone the repository
git clone https://github.com/Pedrooliv12/java-spring-crud-study.git

# Navigate to the project directory
cd java-spring-crud-study

# Start the PostgreSQL container
docker compose up -d

# Run with Maven wrapper
./mvnw spring-boot:run
```

The application will start at `http://localhost:8080`.

### Database

PostgreSQL runs in a Docker container. Connection details:

| Field | Value |
|---|---|
| Host | `localhost` |
| Port | `5432` |
| Database | `apiproduct` |
| Username | `postgres` |
| Password | `postgres` |

> **Note:** Data persists between application restarts as long as the Docker container is running.

---

## 🧪 Running Tests

```bash
./mvnw test
```

The test suite uses **MockMvc** with `@SpringBootTest` and `@Transactional` to ensure each test runs against a clean database state.

### Test coverage

| Test | Scenario |
|---|---|
| `shouldReturnEmptyListWhenNoProducts` | `GET /products` returns `200` with empty array |
| `shouldCreateProductAndReturn201` | `POST /products` returns `201` with correct body |
| `shouldReturn404WhenProductNotFound` | `GET /products/999` returns `404` |
| `shouldReturn400WhenNameIsBlank` | `POST /products` with blank name returns `400` |

---

## 📄 API Documentation

This project uses **SpringDoc OpenAPI** to generate interactive API documentation automatically.

After starting the application, access:

| Interface | URL |
|---|---|
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` |

The Swagger UI allows you to explore and test all endpoints directly from the browser — no Postman required.

---

## 📡 API Reference

### Base URL

```
http://localhost:8080/products
```

### Endpoints

#### Get all products

```http
GET /products
```

**Response** `200 OK`
```json
[
  {
    "id": 1,
    "name": "Keyboard",
    "price": 49.99
  }
]
```

---

#### Get product by ID

```http
GET /products/{id}
```

| Parameter | Type | Description |
|---|---|---|
| `id` | `Long` | Product ID |

**Response** `200 OK` or `404 Not Found`

---

#### Create product

```http
POST /products
```

**Request body**
```json
{
  "name": "Keyboard",
  "price": 49.99,
  "stockQuantity": 100
}
```

> `stockQuantity` must be zero or positive. `name` must not be blank. `price` must be positive.

**Response** `201 Created`
```json
{
  "id": 1,
  "name": "Keyboard",
  "price": 49.99
}
```

> Note: `stockQuantity` is stored internally but never exposed in responses.

---

#### Update product

```http
PUT /products/{id}
```

| Parameter | Type | Description |
|---|---|---|
| `id` | `Long` | Product ID |

**Request body**
```json
{
  "name": "Mechanical Keyboard",
  "price": 89.99,
  "stockQuantity": 50
}
```

**Response** `200 OK` or `404 Not Found`

---

#### Delete product

```http
DELETE /products/{id}
```

| Parameter | Type | Description |
|---|---|---|
| `id` | `Long` | Product ID |

**Response** `204 No Content` or `404 Not Found`

---

## 🇧🇷 Sobre o Projeto (PT-BR)

Este repositório é destinado exclusivamente a **estudos pessoais** de Java com Spring Boot.

O objetivo é aprender de verdade o que acontece por baixo das abstrações — mapeamento de entidades com JPA, o padrão Repository, separação de camadas com Service, e boas práticas de design de APIs REST — sem o uso de ferramentas de geração de código como Lombok.

### Conceitos estudados

- Mapeamento objeto-relacional com JPA e Hibernate (ORM)
- Anotações de entidade: `@Entity`, `@Id`, `@GeneratedValue`
- Repositórios com `JpaRepository`
- Camada de serviço e injeção de dependências por construtor
- Controllers REST com `@RestController`, `ResponseEntity` e verbos HTTP
- Uso de `Optional<T>` para tratamento seguro de valores ausentes
- Validação de entrada com Bean Validation (`@NotBlank`, `@Positive`, `@PositiveOrZero`, `@Valid`)
- Tratamento de erros global com `@ControllerAdvice` e `@ExceptionHandler`
- Padrão DTO — separação entre o contrato público da API e a entidade interna
- Base de dados PostgreSQL com Docker Compose
- Testes de API com MockMvc (`@SpringBootTest`, `@AutoConfigureMockMvc`, `@Transactional`)
- Documentação interativa com SpringDoc OpenAPI e Swagger UI
- Mapeamento de objetos com MapStruct
- Exceções customizadas com `@ControllerAdvice`
- Records do Java para DTOs imutáveis
