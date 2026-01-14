
# Itaú - Desafio Técnico

Este repositório contém dois microserviços desenvolvidos como desafio técnico:

- `account-service` (porta 5000) — gerencia contas
- `transaction-service` (porta 5001) — gerencia saldos e transações

Objetivo deste README: ser simples e direto — explicar como subir o sistema com Docker Compose, mostrar exemplos de uso e descrever o fluxo e as decisões arquiteturais principais.

## Rodando o sistema (Docker Compose)

Pré-requisitos:
- Docker e Docker Compose instalados

A partir da raiz do repositório execute:

```powershell  
# sobe todos os serviços (Zookeeper, Kafka, Postgres, account-service, transaction-service)  
docker-compose up -d  
  
# acompanhar logs (opcional)  
docker-compose logs -f  
  
# parar e remover  
docker-compose down  
```  

O `docker-compose.yml` na raiz já configura:
- Kafka + Zookeeper
- PostgreSQL (cria `account_db` e `transaction_db` via `create-databases.sql`)
- account-service (map porta 5000)
- transaction-service (map porta 5001)

Ambos os serviços esperam variáveis de ambiente para conexão com Kafka e Postgres (já definidas no compose). Se quiser rodar localmente com `mvn spring-boot:run`, ajuste as variáveis `SPRING_DATASOURCE_URL` e `SPRING_KAFKA_BOOTSTRAP_SERVERS`.


## Endpoints principais

Account Service (porta 5000):
- POST /v1/accounts — criar conta

Transaction Service (porta 5001):
- POST /v1/transactions — criar transação (Header `Idempotency-Key` obrigatório)

Exemplo: criação de conta

```bash  
curl --location 'http://localhost:5000/v1/accounts' \--header 'Content-Type: application/json' \  
--data-raw '{  
 "nome": "João da Silva", "cpf": "04747103198", "dataNascimento": "1995-05-20", "email": "joao.silva@teste.com", "telefone": "5511999998888"}'  
```  
Resposta esperada: 202 Accepted com corpo contendo `accountId` e `status: PENDING`.

Exemplo: criar transação (withdrawal)

> Observação: troque o campo `accountId` abaixo pelo `accountId` retornado na criação da conta.

```bash  
curl --location 'http://localhost:5001/v1/transactions' \--header 'Idempotency-Key: ABC125' \  
--header 'Content-Type: application/json' \  
--data '{  
 "accountId": "d290f1ee-6c54-4b01-90e6-d701748f0851", "operationType": "WITHDRAWAL", "amount": 150.50}'  
```  
Resposta esperada: 201 Created com status da transação.

## Swagger / OpenAPI

Ambos os serviços incluem o `springdoc-openapi` (Swagger UI). Quando os serviços estiverem rodando (via Docker Compose ou localmente), você pode acessar a documentação interativa:

- Account Service (rodando em localhost:5000):
  - Swagger UI: http://localhost:5000/swagger-ui.html
  - Alternativa: http://localhost:5000/swagger-ui/index.html
  - OpenAPI JSON: http://localhost:5000/v3/api-docs

- Transaction Service (rodando em localhost:5001):
  - Swagger UI: http://localhost:5001/swagger-ui.html
  - Alternativa: http://localhost:5001/swagger-ui/index.html
  - OpenAPI JSON: http://localhost:5001/v3/api-docs

Se a UI não aparecer, verifique:
- Se o serviço está de fato rodando (ver `docker-compose logs` ou `mvn spring-boot:run`).
- Se há configuração customizada em `application.yml` (propriedade `springdoc.swagger-ui.path`).
- Logs do serviço (pode haver erro de inicialização do SpringDoc se faltar dependência).



## Fluxo principal (simples)

1. Cliente cria conta (HTTP → `account-service`).
2. `account-service` salva a conta no banco e grava um evento na tabela `outbox` (mesma transação).
3. Um worker (`OutboxWorker`) lê a outbox e publica o evento `ACCOUNT_CREATED` no tópico Kafka `account-v1`.
4. `transaction-service` consome `ACCOUNT_CREATED` e inicializa o saldo da conta (cria registro em `tb_account_balance`).
5. `transaction-service` publica `BALANCE_INITIALIZED` no tópico `balance-v1`.
6. `account-service` consome `BALANCE_INITIALIZED` e atualiza o status da conta para `ACTIVE`.

Observação: toda comunicação entre serviços é por eventos Kafka — não há chamadas HTTP síncronas entre os serviços.

## Decisões arquiteturais (resumo)

- Arquitetura baseada em **Ports & Adapters (Hexagonal)**: casos de uso (domain) separados de adaptadores (JPA, Kafka, REST).
- **Outbox Pattern** para garantir entrega de eventos: evento escrito no banco junto com a entidade e publicado por um worker.
- Comunicação **assíncrona via Kafka** (event-driven) para desacoplamento e reprocessamento.
- **Idempotência**: chave de idempotência nas transações e checagens antes de criar recursos.
- **Pessimistic Locking** ao atualizar saldos para evitar race conditions em concorrência alta.
- Cada serviço possui seu próprio schema/DB (Database per Service).

Essas decisões priorizam consistência eventual, resiliência e escalabilidade, com trade-off de maior complexidade operacional.


## Como testar manualmente (sequência sugerida)

1. Suba tudo: `docker-compose up -d`.
2. Crie uma conta usando o endpoint do `account-service`.
3. Verifique no logs do `account-service` que o outbox foi criado e que o worker publicou o evento.
4. Verifique no `transaction-service` logs que o listener consumiu o evento e criou o saldo.
5. Verifique no `account-service` logs que o evento `BALANCE_INITIALIZED` foi consumido e a conta ativada.

Dicas rápidas:
- Ver logs: `docker-compose logs -f account-service` ou `docker-compose logs -f transaction-service`.
- Ver lag do Kafka: use ferramentas como `kafka-consumer-groups` em um container que tenha o CLI.


## Troubleshooting rápido

- Se Kafka/Zookeeper não sobem corretamente: pare e suba novamente (`docker-compose down && docker-compose up -d`) e cheque `docker-compose ps` e `docker-compose logs`.
- Se o Postgres não cria os bancos: veja o conteúdo de `create-databases.sql` e logs do container `postgres`.
- Se consumers não processam eventos: checar `groupId` nos `@KafkaListener`, configuração `bootstrap-servers` e se os tópicos existem.


## Rodando testes unitários

Para rodar os testes localmente em cada serviço:

```powershell  
# Account Service  
cd account-service  
mvn test  
  
# Transaction Service  
cd transaction-service  
mvn test  
```  
  ---  

## Testes de stress (k6)

Há scripts de stress/performace com k6 dentro de cada serviço:

- `account-service/k6/perfomance-test.js`
- `transaction-service/k6/stress-test.js`

Pré-requisito: ter o `k6` instalado localmente (https://k6.io).

Comandos sugeridos (na raiz do repositório):

```powershell  
# Rodar o teste de performance do Account Service  
cd account-service/k6  
k6 run perfomance-test.js  
  
# Rodar o teste de stress do Transaction Service  
cd ../../transaction-service/k6  
k6 run stress-test.js  
```  

O `k6` gera um resumo no terminal com métricas importantes:
- RPS (requests/sec)
- Latência (p50, p95, p99)
- Erros (se houver)

Dica: alguns testes no repositório já geram um arquivo `summary.html` (ver `account-service/k6/summary.html`) — abra no navegador para uma visão mais amigável.

Interpretação rápida:
- Se a latência p95/p99 estiver alta, considere identificar gargalos (DB, locks, GC).
- Se a taxa de erros aumentar com a carga, verifique logs dos serviços e do Kafka/Postgres.
