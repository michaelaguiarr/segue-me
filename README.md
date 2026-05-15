# Segue-Me — Guia do Ambiente Docker

## Pré-requisitos

| Ferramenta | Versão mínima |
|---|---|
| Docker Desktop | 4.x (com BuildKit habilitado) |
| Java | 8 (compilação do fonte) |
| Maven | 3.6+ |

---

## Visão geral dos ambientes

| | Desenvolvimento | Produção |
|---|---|---|
| Arquivo compose | `docker-compose.yml` | `docker-compose.prod.yml` |
| Script restore | `./scripts/restore-dev.sh` | automático pelo `deploy.sh` |
| Script deploy | — | `./scripts/deploy.sh` |
| Credenciais | hardcoded (segueme/segueme123) | via arquivo `.env` |
| Porta app | `8080` | `${APP_PORT}` (padrão `8080`) |
| Porta banco | `5432` exposta | não exposta externamente |
| Adminer | ✅ `8081` | ❌ não incluído |
| Restore automático | manual | automático pelo `deploy.sh` |
| Restart policy | — | `always` |

---

## Ambiente de Desenvolvimento

### Serviços

| Serviço | Imagem | Porta | Função |
|---|---|---|---|
| `postgres` | `postgres:15` | `5432` | Banco de dados |
| `tomcat` | `tomcat:9.0-jre11` | `8080` | Servidor de aplicação |
| `adminer` | `adminer:4` | `8081` | Interface web do banco |

### Subindo do zero

```bash
# 1. Clonar o projeto
git clone <url-do-repositorio>
cd segue-me

# 2. Subir os containers
docker compose up --build -d

# 3. Restaurar o banco
./scripts/restore-dev.sh

# 4. Compilar e publicar o WAR
mvn clean package -DskipTests
cp target/segue-me.war deployments/

# 5. Verificar (deve retornar 302)
curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/segue-me/
```

### Restaurar o banco (dev)

O script apaga o banco existente, recria do zero e restaura o backup em `backup/`.

```bash
./scripts/restore-dev.sh
```

O que o script faz:
1. Localiza o primeiro arquivo em `backup/` (`*.sql`, `*.dump`, `*.backup`, `*.tar`)
2. Valida que o container `segue-me-postgres-1` está rodando
3. Encerra conexões abertas no banco (inclusive do Tomcat)
4. `DROP DATABASE` + `CREATE DATABASE`
5. Cria os roles auxiliares referenciados no dump (`michaels_segueme`, `michaels_segue-me`)
6. Restaura o backup e exibe a lista de tabelas ao final

### Credenciais de desenvolvimento

| Campo | Valor |
|---|---|
| Banco host | `localhost:5432` |
| Banco / Usuário / Senha | `segueme` / `segueme` / `segueme123` |
| Adminer | http://localhost:8081 (Sistema: PostgreSQL, Servidor: postgres) |
| Aplicação | http://localhost:8080/segue-me |

### Comandos do dia a dia — Dev

```bash
# Subir
docker compose up -d

# Parar (mantém banco)
docker compose down

# Parar e apagar banco (reset completo)
docker compose down -v

# Restaurar banco do zero a partir do backup
./scripts/restore-dev.sh

# Redeploy do WAR sem reiniciar container
mvn clean package -DskipTests && cp target/segue-me.war deployments/

# Rebuild da imagem (após alterar config/ ou Dockerfile.tomee)
docker compose up --build -d

# Logs em tempo real
docker compose logs -f tomcat
docker compose logs -f postgres

# Shell nos containers
docker compose exec tomcat bash
docker compose exec postgres psql -U segueme -d segueme
```

---

## Ambiente de Produção

### Serviços

| Serviço | Imagem | Porta | Função |
|---|---|---|---|
| `postgres` | `postgres:15` | não exposta | Banco de dados (rede interna) |
| `tomcat` | `tomcat:9.0-jre11` | `${APP_PORT}` | Servidor de aplicação |

> O proxy reverso externo (nginx) aponta para a porta do Tomcat. SSL é gerenciado pelo nginx, fora do compose.

### Subindo do zero

```bash
# 1. Criar o .env com as credenciais reais
cp .env.prod.example .env
nano .env   # preencha DB_PASSWORD e ajuste APP_PORT se necessário

# 2. Rodar o script de deploy
./scripts/deploy.sh
```

O `deploy.sh` executa automaticamente:
1. Valida o `.env` e as variáveis obrigatórias
2. Compila o WAR com Maven
3. Copia para `deployments/`
4. Sobe o PostgreSQL e aguarda ficar pronto
5. Cria os roles necessários e restaura o backup em `backup/` (se banco estiver vazio)
6. Sobe todos os serviços

### Subindo manualmente (sem script)

```bash
# Subir
docker compose -f docker-compose.prod.yml --env-file .env up -d --build

# Compilar e publicar WAR
mvn clean package -DskipTests
cp target/segue-me.war deployments/

# Verificar (deve retornar 302)
curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/segue-me/
```

### Desligar e remover

```bash
# Para e remove containers e rede (mantém o banco)
docker compose -f docker-compose.prod.yml --env-file .env down

# Para, remove containers E apaga o banco (reset completo)
docker compose -f docker-compose.prod.yml --env-file .env down -v
```

### Comandos do dia a dia — Prod

```bash
# Status dos containers
docker compose -f docker-compose.prod.yml ps

# Logs em tempo real
docker compose -f docker-compose.prod.yml logs -f tomcat
docker compose -f docker-compose.prod.yml logs -f postgres

# Redeploy do WAR sem rebuild de imagem
mvn clean package -DskipTests && cp target/segue-me.war deployments/

# Rebuild completo da imagem
docker compose -f docker-compose.prod.yml --env-file .env up -d --build

# Shell nos containers
docker compose -f docker-compose.prod.yml exec tomcat bash
docker compose -f docker-compose.prod.yml exec postgres psql -U "$DB_USER" -d "$DB_NAME"
```

---

## Redefinir senha de usuário

Senhas armazenadas em MD5. Para redefinir:

```bash
# Gera o hash MD5 da nova senha
echo -n "nova_senha" | md5sum | cut -d' ' -f1

# Dev — atualiza no banco
docker exec -i segue-me-postgres-1 psql -U segueme -d segueme \
  -c "UPDATE usuario SET senha = '<hash>' WHERE email = 'email@dominio.com';"

# Prod — atualiza no banco
docker compose -f docker-compose.prod.yml exec postgres \
  psql -U "$DB_USER" -d "$DB_NAME" \
  -c "UPDATE usuario SET senha = '<hash>' WHERE email = 'email@dominio.com';"
```

---

## Estrutura de pastas relevante

```
segue-me/
├── backup/
│   └── MICHAELS_SEGUE_ME14052026.sql   # Dump plain SQL (685 MB) — restaurado automaticamente em prod
├── config/
│   └── tomcat-context.xml              # DataSource JNDI + BeanManager (montado no Tomcat)
├── deployments/
│   └── segue-me.war                    # WAR publicado (gerado por mvn package, ignorado pelo git)
├── scripts/
│   ├── deploy.sh                       # Deploy de produção (build + restore + up)
│   └── restore-dev.sh                  # Restaura banco de dev do zero a partir do backup
├── sql/
│   └── (scripts .sql executados na 1ª inicialização do postgres, em ordem alfabética)
├── .env.prod.example                   # Modelo de variáveis de ambiente para produção
├── docker-compose.yml                  # Ambiente de desenvolvimento (com Adminer)
├── docker-compose.prod.yml             # Ambiente de produção (sem Adminer, restart always)
└── Dockerfile.tomee                    # Imagem Tomcat 9 com driver PostgreSQL
```

---

## Solução de problemas conhecidos

### Imagem sem suporte ARM64 (Apple Silicon)

**Sintoma:** `docker compose up --build` falha ou roda via Rosetta (lento).

**Causa:** A imagem `tomee:7.1.4-plume` só tem `linux/amd64`.

**Resolução:** Migrado para `tomcat:9.0-jre11`, que tem suporte nativo a `linux/arm64/v8`.

---

### Erro de BeanManager / Hibernate SessionFactory ao usar TomEE

**Sintoma:**
```
HibernateException: Could not access BeanManager ListenerFactory
  Caused by: IllegalStateException: On a thread without an initialized context
```

**Causa:** TomEE 8 usa OpenWebBeans como CDI. O WAR embute Weld-servlet. Os dois conflitavam no bootstrap.

**Resolução:** Migrado para Tomcat 9, que não tem CDI próprio e não conflita com o Weld do WAR.

---

### ClassNotFoundException: javax.xml.bind.JAXBException

**Sintoma:**
```
Caused by: java.lang.ClassNotFoundException: javax.xml.bind.JAXBException
```

**Causa:** JAXB foi removido do JDK no Java 9+. Hibernate 5.2 depende dele.

**Resolução:** Adicionado `javax.xml.bind:jaxb-api:2.3.1` ao `pom.xml`.

---

> **TODO:** Documentar o estado final do login após resolução completa do fluxo de autenticação
> (Spring Security + CDI + EntityManager no Tomcat 9).
