# Ambiente de Desenvolvimento — Docker

## Pré-requisitos

- Docker Desktop instalado e rodando
- Maven 3.x e Java 8 no PATH

## Serviços

| Serviço | URL / Porta | Descrição |
|---------|-------------|-----------|
| TomEE Plume 7.1.4 | http://localhost:8080 | Servidor de aplicação |
| PostgreSQL 15 | localhost:5432 | Banco de dados |
| Adminer | http://localhost:8081 | Interface web do banco |

### Credenciais do banco (desenvolvimento)

| Campo | Valor |
|-------|-------|
| Host | `localhost` (ou `postgres` dentro do Docker) |
| Porta | `5432` |
| Banco | `segueme` |
| Usuário | `segueme` |
| Senha | `segueme123` |

---

## Fluxo de trabalho

### 1. Subir os serviços na primeira vez

```bash
# Constrói a imagem do TomEE e sobe todos os serviços
docker compose up --build
```

### 2. Compilar e publicar a aplicação

```bash
# Gerar o WAR
mvn clean package -DskipTests

# Copiar para a pasta monitorada pelo TomEE
cp target/segue-me.war deployments/
```

O TomEE detecta o WAR automaticamente e faz o deploy.
A aplicação fica disponível em: **http://localhost:8080/segue-me**

> Para deploy no contexto raiz (`/`), renomeie para `ROOT.war`:
> ```bash
> cp target/segue-me.war deployments/ROOT.war
> ```

### 3. Redeploy após alterações

```bash
mvn clean package -DskipTests && cp target/segue-me.war deployments/
```

O TomEE detecta a substituição do arquivo e faz undeploy + deploy automaticamente.

---

## Comandos úteis

```bash
# Subir em background
docker compose up -d

# Parar os serviços
docker compose down

# Parar e remover volume do banco (reset completo)
docker compose down -v

# Ver logs do TomEE em tempo real
docker compose logs -f tomee

# Ver logs do PostgreSQL
docker compose logs -f postgres

# Rebuild apenas da imagem do TomEE (após alterar config/)
docker compose up --build tomee

# Acessar o shell do container TomEE
docker compose exec tomee bash

# Acessar o psql direto
docker compose exec postgres psql -U segueme -d segueme
```

---

## Scripts SQL iniciais

Coloque arquivos `.sql` na pasta `sql/` para que sejam executados automaticamente
na primeira inicialização do PostgreSQL (em ordem alfabética).

Exemplo:
```
sql/
├── 01-schema.sql   # criação de tabelas
└── 02-dados.sql    # dados iniciais
```

---

## Estrutura dos arquivos Docker

```
├── docker-compose.yml       # Orquestração dos 3 serviços
├── Dockerfile.tomee         # Imagem TomEE + driver PostgreSQL
├── config/
│   ├── tomee.xml            # DataSource JNDI (seguemeDS)
│   └── system.properties    # JSF=Development, locale pt_BR
├── deployments/             # Pasta monitorada pelo TomEE para hot-deploy
│   └── .gitkeep
└── sql/                     # Scripts SQL executados na inicialização do banco
    └── .gitkeep
```

---

## Notas sobre a configuração

- **`persistence.xml`** foi atualizado para usar o DataSource JNDI `seguemeDS`
  definido em `config/tomee.xml`, removendo a dependência de C3P0 com credenciais hardcoded.
- **Produção (JBoss/WildFly):** configure um DataSource com o mesmo nome JNDI
  `seguemeDS` no servidor de produção.
- **`JtaManaged=false`** no DataSource preserva o gerenciamento de transações
  do interceptor customizado `util/jpa/@Transactional` (RESOURCE_LOCAL).
- O driver PostgreSQL 42.7.3 (JDBC 4.2) substitui o `9.1-901.jdbc4` do pom.xml
  ao nível do TomEE — é compatível com Java 8 e PostgreSQL 15.
