# Roadmap

Marque cada item ao concluir. Uma fase por vez; testes passando antes de avançar.

## Fase 0: Reposicionar o repositório
- [x] Remover o aviso "estudo pessoal" do README e atualizar a descrição do projeto
- [x] Renomear/ajustar o repo se necessário e adicionar topics (`spring-boot`, `jwt`, `docker`, `postgresql`)
- [x] Corrigir a inconsistência de versões de migration no README
- [x] Expor `stockQuantity` na resposta de produto

## Fase 1: Catálogo
- [x] Migration e entidade `categories`; `category_id` em `products`
- [x] CRUD de categorias (ADMIN)
- [x] Filtros em `GET /products`: `search`, `category`, `lowStock`
- [x] Soft delete e campos de auditoria em produtos
- [x] Testes de integração e permissões

## Fase 2: Vendas e estoque (sem pagamento externo)
- [ ] Migrations `sales`, `sale_items`, `stock_movements`
- [ ] `POST /sales` com baixa atômica de estoque, transação única e `409` por estoque insuficiente
- [ ] Liberar `POST /sales` para USER; `GET /sales` para ADMIN
- [ ] Teste de concorrência no último item do estoque

## Fase 3: Caixa
- [ ] Migration `cash_entries`; entrada automática a cada venda `PAID`
- [ ] `GET /cash/summary`, `GET /cash/entries`, `POST /cash/expenses` (ADMIN)
- [ ] Testes de cálculo de saldo e de permissão (USER recebe 403)

## Fase 4: payment-service
- [ ] Novo projeto Spring Boot com banco próprio e contrato de `docs/architecture.md`
- [ ] Idempotência por `idempotencyKey` e gateway simulado
- [ ] Cliente HTTP no `api-product` com timeout e `X-Internal-Key`
- [ ] Venda `PENDING` -> `PAID`/`FAILED`; job de reconciliação para `PENDING` antigas
- [ ] Docker Compose com os dois serviços e dois bancos
- [ ] Testes do fluxo completo, incluindo recusa e serviço fora do ar
- [ ] (Opcional) gateway sandbox real; mensageria; Resilience4j

## Fase 5: Front-end (React + Vite + TypeScript)
- [ ] Projeto base, login e armazenamento do token
- [ ] Menu lateral por role; rotas protegidas
- [ ] Tela Produtos (tabela, busca, filtros, formulário ADMIN)
- [ ] Tela Nova venda (busca, carrinho, pagamento, finalizar)
- [ ] Tela Caixa (cards, gráfico, movimentações, lançar despesa)
- [ ] CORS configurado no `api-product`

## Fase 6: Produção e vitrine
- [ ] Spring Actuator (`/health`), logs estruturados, rate limiting no login
- [ ] Deploy online (API e front) com Swagger acessível
- [ ] Usuários de demonstração (admin e vendedor) documentados no README
- [ ] README com GIF/prints, diagrama de arquitetura e seção de decisões técnicas
- [ ] Dependabot e coleção Postman/Bruno
