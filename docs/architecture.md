# Arquitetura

## Visão geral
Dois serviços Spring Boot independentes, cada um com seu próprio banco.

```text
 [ Front React ]  ──HTTP/JWT──▶  [ api-product ]  ──HTTP + chave interna──▶  [ payment-service ]
                                       │                                           │
                                  PostgreSQL                                  PostgreSQL
                                (produtos, vendas, caixa)                     (pagamentos)
```

- **api-product** (este repositório): usuários, produtos, categorias, vendas, estoque e caixa. Único serviço exposto ao front.
- **payment-service** (repositório separado): só pagamentos. Nunca acessa o banco do `api-product` e vice-versa.
- O front **não** chama o `payment-service` diretamente.

## Modelo de dados (novas tabelas)
Use o próximo número livre de migration Flyway. Valores monetários em `NUMERIC(12,2)`.

| Tabela | Campos principais |
|---|---|
| `categories` | id, name (único) |
| `products` (já existe) | + `category_id` (FK, opcional), + `active` (soft delete), + `created_at`, `updated_at` |
| `sales` | id, user_id, status (`PENDING`, `PAID`, `FAILED`, `CANCELED`), payment_method (`PIX`, `CARD`, `CASH`), total, payment_id (id no payment-service), created_at |
| `sale_items` | id, sale_id, product_id, quantity, **unit_price** (preço no momento da venda) |
| `cash_entries` | id, type (`IN`, `OUT`), amount, description, sale_id (opcional), created_by, created_at |
| `stock_movements` | id, product_id, quantity (positivo entra, negativo sai), reason (`SALE`, `PURCHASE`, `ADJUSTMENT`), sale_id (opcional), created_at |

Regras: `unit_price` é copiado do produto na criação da venda; mudar o preço depois não altera vendas antigas. O saldo do caixa é calculado a partir de `cash_entries`, nunca guardado.

## Fluxo da venda
1. `POST /sales` (ADMIN ou USER) valida os itens e confere que todos os produtos existem, estão ativos e têm estoque suficiente (falha rápida com `409`).
2. Cria a venda `PENDING` com os itens e o total. **O estoque ainda não baixa.**
3. Chama `POST /payments` no `payment-service` com `saleId` como chave de idempotência.
4. Resposta `APPROVED`: em **uma única transação**, baixa o estoque, registra `stock_movements`, cria a entrada em `cash_entries` e marca a venda `PAID`.
5. Resposta `DECLINED`: marca `FAILED`; nada mais muda.
6. Pagamento `CASH` não chama o serviço: a venda vai direto para `PAID` pelo mesmo passo 4.

### Estoque e concorrência
A baixa deve ser atômica e conferir o saldo no próprio banco, por exemplo:
`UPDATE products SET stock_quantity = stock_quantity - :qty WHERE id = :id AND stock_quantity >= :qty`
Se nenhuma linha for afetada, a venda falha com estoque insuficiente (mesmo que a checagem inicial tenha passado). Cobrir com teste de duas vendas concorrentes do último item.

## Contrato do payment-service
Autenticação entre serviços: header `X-Internal-Key` com valor de `PAYMENT_SERVICE_API_KEY`. O front nunca vê essa chave.

`POST /payments`
```json
{ "idempotencyKey": "sale-123", "amount": 179.88, "method": "PIX", "cardLast4": null }
```
Resposta `200`:
```json
{ "paymentId": "uuid", "status": "APPROVED", "reason": null }
```
`status`: `APPROVED`, `DECLINED` ou `PENDING`. `GET /payments/{paymentId}` consulta o estado.

**Idempotência:** repetir o mesmo `idempotencyKey` devolve o pagamento já criado, sem cobrar de novo.

**Gateway simulado (fase inicial):** `PIX` aprova sempre; `CARD` recusa se `cardLast4 = "0000"`; qualquer valor acima de 10.000,00 é recusado. Trocar por um gateway em modo sandbox depois, sem mudar o contrato.

## Falhas e resiliência
- Chamada ao `payment-service` com timeout curto (conexão 2s, leitura 5s).
- Timeout ou serviço fora do ar: a venda permanece `PENDING` e a API responde `202` com a venda pendente. Um job agendado reconsulta vendas `PENDING` antigas por `idempotencyKey` e conclui ou marca `FAILED`.
- Nunca marcar `PAID` sem confirmação `APPROVED`.
- Opcional depois: Resilience4j (retry e circuit breaker) e troca do REST por mensageria (RabbitMQ).

## Infra local
`docker-compose.yml` sobe `postgres-main`, `postgres-payment`, `api-product` e `payment-service`, com healthchecks e rede interna. Só o `api-product` publica porta para o host.
