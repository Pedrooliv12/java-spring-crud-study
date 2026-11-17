# Project Context & Agent Guidelines: api-product

> Arquivo de entrada para agentes. Mantenha-o curto: o detalhe de cada assunto vive em `docs/`.
> Antes de começar qualquer tarefa, leia a seção 8 e o `docs/roadmap.md` para saber o que já foi feito e o que vem a seguir.

## 1. Visão Geral do Projeto
- **Nome:** `api-product`
- **Propósito:** backend de um **mini PDV com controle de estoque e fluxo de caixa** para uma loja pequena ("Balcão"). Nasceu como API de produtos e autenticação, feita como estudo aprofundado do ecossistema Java e Spring Boot, e agora evolui para um projeto de portfólio com front-end e um serviço de pagamentos separado.
- **Princípio Fundamental:** código limpo e explícito, compreendendo os mecanismos internos do framework **sem atalhos como Lombok**.
- **Usuários:** `ADMIN` (dono/gerente: caixa, produtos, vendas) e `USER` (vendedor: nova venda e consulta de produtos). Detalhes em `docs/api.md`.

---

## 2. Tech Stack & Versões
- **Linguagem:** Java 21
- **Framework Principal:** Spring Boot 4.1.2 (Web MVC, Data JPA, Security, Validation)
- **Banco de Dados:** PostgreSQL 16 (Hibernate com `ddl-auto=validate`)
- **Versionamento de Schema:** Flyway (`src/main/resources/db/migration/`)
- **Mapeamento:** MapStruct 1.6.3 (annotation processor configurado no Maven)
- **Segurança:** Stateless JWT com JJWT 0.12.6 e senhas com BCrypt
- **Documentação de API:** SpringDoc OpenAPI 2.8.9 (Swagger UI em `/swagger-ui.html` e `/v3/api-docs`)
- **Testes:** JUnit 5, Mockito, AssertJ, MockMvc, Testcontainers (PostgreSQL 16), JaCoCo
- **Qualidade & CI/CD:** SonarCloud, GitHub Actions, Docker (build multi-stage, usuário não-root)
- **Planejado (ver `docs/roadmap.md`):** `payment-service` separado (Spring Boot), front-end React + Vite + TypeScript.

---

## 3. Arquitetura do Sistema e Fluxo de Dados

Arquitetura em Camadas (Layered Architecture) clássica e desacoplada:

```text
HTTP Request
     │
     ▼
[ Controller ]  ◄── Injeta Service (via construtor)
     │          ◄── Recebe Request DTO (record) + Bean Validation (@Valid)
     │          ◄── Documentado com anotações OpenAPI (@Operation, @ApiResponse)
     ▼
[ Service ]     ◄── Regras de negócio, transações (@Transactional quando necessário)
     │          ◄── Utiliza MapStruct Mapper para converter Entity ⇄ DTO
     │          ◄── Lança exceções de domínio tipadas (ex: ProductNotFoundException)
     ▼
[ Repository ]  ◄── Spring Data JPA interfaces (métodos de consulta / derivados)
     │
     ▼
[ PostgreSQL ]  ◄── Schema versionado unicamente via migrações Flyway
```

Visão entre serviços (vendas e pagamentos): `docs/architecture.md`.

### Princípios Arquiteturais Obrigatórios:
1. **Injeção de Dependências:** sempre via construtor explícito. **Proibido `@Autowired` em atributos/campos**.
2. **Isolamento de Camadas (DTOs Obrigatórios):** entidades JPA (`@Entity`) **nunca** são recebidas nem retornadas diretamente pelos Controllers. Toda entrada e saída usa DTOs (`record`).
3. **Mapeamento via MapStruct:** conversões Entity ⇄ DTO por mappers MapStruct (`componentModel = "spring"`), sem boilerplate manual em controllers ou services.
4. **Tratamento Centralizado de Exceções:** Services e Controllers não capturam exceções para montar `ResponseEntity` de erro. Exceções de domínio são lançadas e tratadas pelo [`GlobalExceptionHandler`](src/main/java/com/example/api_product/exception/GlobalExceptionHandler.java).

---

## 4. Convenções de Código e Padrões por Camada

### A. Entidades JPA (`com.example.api_product.model`)
- **SEM LOMBOK:** não use `@Data`, `@Getter`, `@Setter`, `@Builder`, etc. Declare construtores, getters e setters explicitamente.
- **Nomes e Mapeamento Explícitos:** `@Table(name = "...")`, `@Column(name = "...")`, `@JoinColumn(name = "...")`.
- **Relacionamentos:** `FetchType.LAZY` como padrão em `@ManyToOne` e `@OneToMany`.
- **Enums:** sempre como String no banco: `@Enumerated(EnumType.STRING)`.
- **Valores Monetários:** sempre `BigDecimal` na entidade e `NUMERIC(precision, scale)` no banco.

### B. DTOs & Validação (`com.example.api_product.dto`)
- Declarados exclusivamente como Java `record`.
- Validação de entrada via Bean Validation (`@NotBlank`, `@NotNull`, `@Positive`, `@Email`, `@Size`, etc.).
- Respostas paginadas sempre envelopadas pelo record [`PageResponseDTO`](src/main/java/com/example/api_product/dto/PageResponseDTO.java).

### C. Controllers & Documentação OpenAPI (`com.example.api_product.controller`)
- Anotados com `@RestController` e `@RequestMapping`.
- Endpoints paginados recebem `@ParameterObject @PageableDefault(size = 20, sort = "id") Pageable pageable`.
- OpenAPI obrigatório em cada endpoint: `@Tag`, `@Operation`, `@ApiResponse` para cada status retornado (200/201, 400, 401, 403, 404, 409 quando existir) e `@SecurityRequirement(name = "bearerAuth")` nas rotas protegidas.

### D. Tratamento de Exceções (`com.example.api_product.exception`)
- Exceções de domínio estendem `RuntimeException`.
- Tratadas em [`GlobalExceptionHandler`](src/main/java/com/example/api_product/exception/GlobalExceptionHandler.java) com `@ControllerAdvice`:
  - Validação: campo a campo (`{ "campo": "mensagem" }`).
  - Negócio/não encontrado: `{ "error": "mensagem descritiva" }`.
  - Conflito de negócio (ex.: estoque insuficiente): `409` no mesmo formato `{ "error": "..." }`.

### E. Banco de Dados e Migrations (`db/migration/`)
- `ddl-auto=validate`: o Hibernate **nunca** cria nem altera tabelas.
- Toda mudança de schema exige uma nova migration (`V{n}__descricao.sql`), usando o próximo número livre.
- **Migrations já executadas nunca devem ser alteradas**.
- Dados de demonstração (usuários admin/vendedor) entram só por migration ou seed restrito ao profile `dev`/`demo`, nunca com senha fraca em produção.

---

## 5. Segurança & Autenticação (RBAC)

- **Modelo:** JWT stateless via `JwtFilter` antes de `UsernamePasswordAuthenticationFilter`.
- **Hashing de Senha:** BCrypt (`BCryptPasswordEncoder`).
- **Tokens:** validade de 1 hora, no header `Authorization: Bearer <token>`.
- **Matriz de Autorização (estado atual):**

  | Endpoint | Método | Papel Mínimo |
    |---|---|---|
  | `/auth/register`, `/auth/login` | `POST` | Público |
  | `/swagger-ui/**`, `/v3/api-docs/**` | `GET` | Público |
  | `/products/**` | `GET` | Autenticado (`USER` ou `ADMIN`) |
  | `/products/**` | `POST`, `PUT`, `DELETE` | `ADMIN` |

  Endpoints planejados (vendas, caixa, categorias) e seus papéis: `docs/api.md`.
- **Anti-Enumeração:** a autenticação retorna o mesmo `401 {"error": "Invalid email or password"}` para e-mail inexistente e senha incorreta.
- **Atribuição de Papéis:** todo novo usuário é cadastrado como `USER` pelo backend. `ADMIN` é atribuído internamente/direto no banco.
- **Regra de ouro:** o front-end só esconde telas; **quem barra de verdade é o backend** (403). Todo endpoint novo nasce com o papel definido e testado.

---

## 6. Configuração de Ambiente
- Importação de `.env` local (`spring.config.import=optional:file:.env[.properties]`).
- Variável obrigatória: `JWT_SECRET` (chave de 256 bits em Base64).
- `.env` é ignorado pelo Git (`.gitignore`). Nunca commitar segredos.
- Variáveis do pagamento (quando existir): `PAYMENT_SERVICE_URL`, `PAYMENT_SERVICE_API_KEY` (ver `docs/architecture.md`).

---

## 7. Comandos Frequentes (CLI / Terminal)
Os comandos abaixo são para Windows (PowerShell/cmd). Em Linux/macOS use `./mvnw` no lugar de `.\mvnw.cmd`.
- **Compilar:** `.\mvnw.cmd clean compile`
- **Testes (unitários + Testcontainers):** `.\mvnw.cmd test`
- **Executar localmente:** `.\mvnw.cmd spring-boot:run`
- **Subir o banco via Docker Compose:** `docker compose up -d postgres`

---

## 8. Documentação do Projeto (leia antes de implementar)
| Arquivo | Conteúdo |
|---|---|
| [`docs/architecture.md`](docs/architecture.md) | Os dois serviços, modelo de dados, fluxo da venda, contrato com o `payment-service`, falhas e idempotência |
| [`docs/api.md`](docs/api.md) | Endpoints atuais e planejados, com papéis e erros |
| [`docs/roadmap.md`](docs/roadmap.md) | Fases com checkboxes (memória de progresso) |
| [`docs/design/tokens.md`](docs/design/tokens.md) | Identidade visual, telas e regras de interface do front |
| [`docs/design/`](docs/design/) | Protótipos em imagem: `01-caixa.png`, `02-produtos.png`, `03-nova-venda.png` |

---

## 9. Como trabalhar neste repositório
1. Faça **uma fase do roadmap por vez**, em passos pequenos, e rode `.\mvnw.cmd test` antes de dar algo como concluído.
2. Não quebre testes existentes e não altere migrations já executadas.
3. Todo código novo traz testes: unitários nos services e integração com Testcontainers nos fluxos críticos (venda, estoque, permissões).
4. Ao terminar uma tarefa, marque o item no `docs/roadmap.md` e, se mudou contrato ou modelo, atualize `docs/api.md` ou `docs/architecture.md` no mesmo commit.
5. Se uma instrução de `docs/` conflitar com as regras deste arquivo, **este arquivo prevalece**; avise o autor do conflito.
6. Dúvida de regra de negócio? Pergunte antes de inventar.
