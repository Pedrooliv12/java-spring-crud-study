# Design do front-end ("Balcão")

Protótipos de referência (desktop, 1440 px): [`01-caixa.png`](01-caixa.png), [`02-produtos.png`](02-produtos.png), [`03-nova-venda.png`](03-nova-venda.png). Reproduza o layout, os espaçamentos e a hierarquia dessas imagens.
Obs.: nos prints, a fonte dos títulos aparece em fonte de reserva; a fonte correta é a da tabela abaixo.

## Stack sugerida
React + Vite + TypeScript, Tailwind CSS e shadcn/ui para os componentes base (botões, tabela, inputs).

## Cores
| Token | Valor | Uso |
|---|---|---|
| `ink` | `#1B1B18` | texto principal e menu lateral |
| `paper` | `#F4F1EA` | fundo da página |
| `surface` | `#FFFFFF` | cards e tabelas |
| `line` | `#DDD8CB` | bordas e divisores |
| `muted` | `#5E5B52` | texto secundário |
| `brand` | `#1F4D3A` | botão primário, entradas, gráfico |
| `danger` | `#A63D2A` | saídas e erros |
| `ok-bg / ok-fg` | `#E3EDE6 / #1F4D3A` | etiqueta "Em estoque" |
| `warn-bg / warn-fg` | `#F6E6C8 / #6B4A00` | etiqueta "Baixo" |
| `bad-bg / bad-fg` | `#F3D9D3 / #7A2A1B` | etiqueta "Zerado" |

## Tipografia
- Títulos e valores grandes: **Space Grotesk** (500/700). Corpo: **Inter** (400/500/700).
- Títulos de página 36 px; valores de card 30 px; corpo 14 px; legendas 12-13 px.

## Forma e espaçamento
- Cards: fundo `surface`, borda 1 px `line`, raio 12 px, padding 22 px. Sem sombras.
- Botão primário: fundo `brand`, texto branco, raio 8 px, altura mínima 44 px.
- Menu lateral: 232 px, fundo `ink`; item ativo com fundo `paper` e texto `ink`.
- Conteúdo: padding 36 px 48 px, largura máxima ~1200 px. Grades com espaçamento de 20-24 px.
- Evite gradientes, emojis e sombras fortes.

## Telas
1. **Caixa** (somente ADMIN): título e botão "Lançar despesa"; 3 cards (saldo, entradas, saídas); gráfico de barras "Entradas por dia"; lista de últimas movimentações com valor verde (entrada) ou vermelho (saída).
2. **Produtos**: busca, filtro de categoria, filtro de estoque baixo, tabela (produto, categoria, preço, estoque, etiqueta de estado), paginação. "Novo produto", editar e excluir só para ADMIN.
3. **Nova venda**: à esquerda busca e grade de produtos (desabilitar os sem estoque); à direita carrinho, forma de pagamento (Pix, cartão, dinheiro), total e "Finalizar venda". Mostrar estado do pagamento (pendente, aprovado, recusado).

## Regras de interface
- O menu lateral depende do role do token: USER não vê "Caixa".
- Esconder o que o role não pode usar, mas nunca confiar nisso: o backend responde 403.
- Valores em reais com `Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })`.
- Estados de carregamento, erro e lista vazia em toda tela; erros do backend (`{ "error": "..." }`) aparecem ao usuário.
