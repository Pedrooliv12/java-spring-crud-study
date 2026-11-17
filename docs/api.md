# API

Formato de erro: `{ "error": "mensagem" }` (validação: `{ "campo": "mensagem" }`). Todos os endpoints novos ficam documentados no Swagger.
Status: **[feito]** já existe, **[plan]** planejado.

## Autenticação
| Endpoint | Método | Papel | Status |
|---|---|---|---|
| `/auth/register` | POST | Público (cria `USER`) | feito |
| `/auth/login` | POST | Público | feito |

## Produtos e categorias
| Endpoint | Método | Papel | Status |
|---|---|---|---|
| `/products` (paginado; filtros `search`, `category`, `lowStock`) | GET | USER, ADMIN | feito (filtros: plan) |
| `/products/{id}` | GET | USER, ADMIN | feito |
| `/products` | POST | ADMIN | feito |
| `/products/{id}` | PUT | ADMIN | feito |
| `/products/{id}` | DELETE (soft delete) | ADMIN | feito (soft delete: plan) |
| `/categories` | GET | USER, ADMIN | feito |
| `/categories/{id}` | GET | USER, ADMIN | feito |
| `/categories` | POST | ADMIN | feito |
| `/categories/{id}` | PUT, DELETE | ADMIN | feito |

`stockQuantity` é exposto na resposta do produto (`ProductResponseDTO`).

## Vendas
| Endpoint | Método | Papel | Status |
|---|---|---|---|
| `/sales` | POST | **USER, ADMIN** | plan |
| `/sales/{id}` | GET | USER (só as suas), ADMIN | plan |
| `/sales` (paginado, filtros por período e vendedor) | GET | ADMIN | plan |

Erros de `POST /sales`: `400` validação, `404` produto inexistente, `409` estoque insuficiente ou produto inativo, `202` venda criada mas pagamento ainda pendente.

## Caixa (somente ADMIN)
| Endpoint | Método | Papel | Status |
|---|---|---|---|
| `/cash/summary?month=2026-11` (saldo, entradas, saídas, série diária) | GET | ADMIN | plan |
| `/cash/entries` (paginado) | GET | ADMIN | plan |
| `/cash/expenses` | POST | ADMIN | plan |

## Matriz de visibilidade no front
| Tela | ADMIN | USER |
|---|---|---|
| Caixa | sim | não |
| Produtos | criar, editar, excluir | somente leitura |
| Nova venda | sim | sim |
