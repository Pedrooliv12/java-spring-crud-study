<div align="center">

# 📦 api-product

A lightweight RESTful CRUD API built with Spring Boot for managing products.

> ⚠️ **This repository is for personal study purposes only.**  
> Built to explore Java, Spring Boot, Spring Data JPA, and REST API design from the ground up — without magic abstractions.

[![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-brightgreen?style=flat-square&logo=springboot)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-Build-red?style=flat-square&logo=apachemaven)](https://maven.apache.org/)
[![H2](https://img.shields.io/badge/H2-In--Memory%20DB-blue?style=flat-square)](https://www.h2database.com/)

</div>

---

## 📋 Table of Contents

- [About](#-about)
- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
- [API Reference](#-api-reference)
- [Sobre o Projeto (PT-BR)](#-sobre-o-projeto-pt-br)

---

## 📖 About

This project is a simple Product CRUD REST API, developed as a hands-on study of the Spring Boot ecosystem. The goal is to understand what happens under the hood — JPA entity mapping, repository pattern, service layer separation, and HTTP response handling — without relying on code-generation tools like Lombok.

---

## 🛠 Tech Stack

| Technology | Purpose |
|---|---|
| Java 21 | Programming language |
| Spring Boot | Application framework |
| Spring Web (MVC) | REST controller layer |
| Spring Data JPA | Data access layer |
| Hibernate | JPA implementation (ORM) |
| H2 Database | In-memory database for development |
| Maven | Build and dependency management |

---

## 🗂 Project Structure

```
src/main/java/com/example/api_product/
├── controller/
│   └── ProductController.java   # HTTP layer — endpoints and status codes
├── service/
│   └── ProductService.java      # Business logic layer
├── repository/
│   └── ProductRepository.java   # Data access layer (Spring Data JPA)
└── model/
    └── Product.java             # JPA entity mapped to the products table
```

---

## 🚀 Getting Started

### Prerequisites

- Java 21+
- Maven 3.8+

### Running the application

```bash
# Clone the repository
git clone https://github.com/your-username/api-product.git

# Navigate to the project directory
cd api-product

# Run with Maven wrapper
./mvnw spring-boot:run
```

The application will start at `http://localhost:8080`.

### H2 Console

The in-memory database console is available at:

```
http://localhost:8080/h2-console
```

| Field | Value |
|---|---|
| JDBC URL | `jdbc:h2:mem:testdb` |
| Username | `sa` |
| Password | *(leave blank)* |

> **Note:** All data is reset every time the application restarts, as H2 runs in-memory mode.

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
  "price": 49.99
}
```

**Response** `201 Created`
```json
{
  "id": 1,
  "name": "Keyboard",
  "price": 49.99
}
```

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
  "price": 89.99
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
- Base de dados H2 em memória para desenvolvimento
