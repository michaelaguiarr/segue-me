# CLAUDE.md — Segue-Me

## Domínio de Negócio

O Segue-Me é um movimento de retiros católicos para casais e jovens.
O sistema tem dois propósitos centrais:

### 1. Quadrante do Encontro
Geração do quadrante (escala/grade de serviço) de um retiro, distribuindo
casais e seguidores nos círculos, equipes e funções do encontro.
- Um retiro (`SegueMe`) possui vários `Evento`s e `Circulo`s
- Casais e seguidores são inscritos (`Venda`/`Inscricao`) e alocados
- Os crachás (JasperReports) identificam cada participante no encontro

### 2. Histórico de Serviço
Registro longitudinal de todos os retiros em que um casal ou seguidor
serviu, participou ou coordenou — permitindo consultas como:
- "Quantas vezes esse casal já serviu?"
- "Em quais encontros esse seguidor participou?"
- "Quem está apto para assumir uma função de liderança?"

### Perfis de Usuário
- **Casal:** participante/servo casado
- **Seguidor:** membro jovem da equipe
- **Equipe:** grupo de serviço dentro do retiro
- **Padre/Convidado:** participantes especiais (apenas crachá)

### Regra crítica
O histórico de serviço é a memória do movimento — nunca remover
registros de participação, apenas inativar.

## Visão Geral

Sistema web de gestão para o movimento **Segue-Me** (retiros e encontros católicos), gerenciando casais, seguidores, inscrições, eventos, círculos, paróquias, equipes e relatórios de crachás.

- **Artifact:** `br.com.segue-me:segue-me:3.0.0-beta`
- **Packaging:** WAR
- **Java:** 1.8
- **Deploy:** JBoss/WildFly (ver `jboss-web.xml`)
- **Banco:** PostgreSQL (driver `postgresql:9.1-901.jdbc4`)

---

## Stack Tecnológica

| Camada | Tecnologia |
|---|---|
| Frontend | JSF 2.2 + PrimeFaces 6.1 (tema *paradise*) + OmniFaces 2.6.5 |
| Backend | CDI (Weld), JPA 2 (Hibernate), Spring Security 4.1 |
| Build | Maven 3 |
| Relatórios | JasperReports 6.20 + iText 2.1.7 (PDF) + Apache POI 3.11 (Excel) |
| E-mail | JavaMail + Apache Velocity (templates) |
| Segurança | Spring Security com `Md5PasswordEncoder`, `DaoAuthenticationProvider` |

---

## Comandos Principais

```bash
# Compilar e gerar WAR
mvn clean package

# Compilar sem rodar testes
mvn clean package -DskipTests

# Apenas compilar (sem WAR)
mvn compile

# WAR gerado em:
# target/segue-me.war
```

O deploy é feito copiando o WAR para o diretório `deployments/` do JBoss/WildFly.

---

## Estrutura de Pacotes

```
src/main/java/com/segue/
├── controller/          # Managed Beans JSF (@Named + @ViewScoped/@SessionScoped)
├── filter/              # Objetos de filtro para pesquisas (sem lógica)
├── model/               # Entidades JPA
├── repository/          # Acesso a dados via EntityManager (padrão DAO)
├── security/            # Spring Security (SecurityConfig, AppUserDetailsService, Seguranca)
├── service/             # Regras de negócio, lança NegocioException
└── util/
    ├── cdi/             # CDIServiceLocator (lookup manual de beans)
    ├── jpa/             # Interceptor @Transactional customizado
    └── report/          # ExecutorRelatorio / ExecutorRelatorioDownload (JasperReports)

src/main/resources/
├── META-INF/
│   └── persistence.xml  # Persistence unit "segue-mePU" (JTA, Hibernate)
└── jasper/              # Arquivos .jrxml (fonte) e .jasper (compilado) para crachás

src/main/webapp/
├── WEB-INF/
│   ├── facelets/        # Componentes reutilizáveis (toolbars por perfil)
│   ├── template/        # Templates Facelets (template.xhtml, template-login, template-public)
│   ├── faces-config.xml # Locale pt_BR, PrimeFaces handlers
│   ├── web.xml          # Configuração do servlet JSF
│   └── jboss-web.xml    # Context root JBoss
├── casal/               # Páginas área logada — casais
├── casal-public/        # Páginas públicas — casais
├── seguidor/            # Páginas área logada — seguidores
├── seguidor-public/     # Páginas públicas — seguidores
├── inscricao/           # Inscrições e fichas
├── circulo/             # Círculos
├── ecc/                 # ECC (Encontro de Casais com Cristo)
├── equipe/              # Equipes
├── evento/              # Eventos
├── paroquia/            # Paróquias
├── segue-me/            # Gestão do retiro Segue-Me
├── venda/               # Vendas
├── login/               # Telas de login, recuperação de senha
├── meu-perfil/          # Perfil do usuário
└── dashboard.xhtml      # Tela inicial pós-login
```

---

## Entidades Principais (model)

| Entidade | Tabela | Descrição |
|---|---|---|
| `Casal` | `casal` | Casal participante |
| `Seguidor` | `seguidor` | Membro da equipe |
| `SegueMe` | `segue_me` | Retiro/encontro |
| `Evento` | `evento_segue_me` | Evento dentro de um retiro |
| `ECC` | `ecc` | Encontro de Casais |
| `Circulo` | `circulo` | Círculo de um retiro |
| `Equipe` | — | Equipe de serviço |
| `Paroquia` | — | Paróquia |
| `Usuario` | — | Usuário do sistema |
| `Perfil` | — | Perfil/role de acesso |
| `Venda` | — | Venda/inscrição |
| `NumeroRomano` | — | Tabela auxiliar de numeração |

---

## Arquitetura em Camadas

```
View (XHTML/Facelets)
    ↓ chama
Controller (@Named Bean) — validação de entrada, navegação
    ↓ delega
Service — regras de negócio, lança NegocioException
    ↓ usa
Repository — EntityManager, queries JPQL com JOIN FETCH
    ↓ persiste
Banco PostgreSQL (via JNDI DataSource no context.xml)
```

### Convenções de nomeação

- **Controllers:** `Cadastro<Entidade>Bean` / `Pesquisa<Entidade>Bean`
- **Services:** `Cadastro<Entidade>Service`
- **Repositories:** `<Entidade>Repository`
- **Filters:** `<Entidade>Filter`
- **Métodos repositório:** `porId()`, `guardar()`, `remover()`, `listaAll()`, `lista<Critério>()`
- **Views:** `cadastro-<entidade>.xhtml`, `pesquisa-<entidade>.xhtml`, `detalhe-<entidade>.xhtml`

---

## Segurança

- `SecurityConfig` — configura Spring Security (MD5 para senhas, rotas protegidas)
- `AppUserDetailsService` — carrega `Usuario` do banco para o Spring Security
- `Seguranca` — bean CDI que expõe o usuário logado para as views
- Páginas públicas ficam em `*-public/` e `login/`
- Filtros JSF (`CasalFilter`, `SeguidorFilter`) complementam o controle de acesso por tipo de usuário

---

## Relatórios (Crachás)

Os crachás são gerados via JasperReports e ficam em `src/main/resources/jasper/`:

| Arquivo | Tipo |
|---|---|
| `crachaCasal` | Crachá do casal participante |
| `crachaCasalEquipe` | Crachá do casal na equipe |
| `crachaSeguidor` | Crachá do seguidor |
| `crachaSeguidorEquipe` | Crachá do seguidor na equipe |
| `crachaConvidado` | Crachá de convidado |
| `crachaPadre` | Crachá do padre |
| `crachaSeguimista` | Crachá do seguimista |

- `ExecutorRelatorio` — gera e exibe inline no browser
- `ExecutorRelatorioDownload` — gera e força download

---

## Configuração de Banco

`src/main/webapp/META-INF/context.xml` define o DataSource JNDI usado pelo `persistence.xml`.

`persistence.xml` usa persistence unit `"segue-mePU"` com transações JTA gerenciadas pelo servidor.

---

## Observações

- Locale padrão: `pt_BR` (configurado em `faces-config.xml`)
- E-mails usam templates Apache Velocity em `src/main/resources/`
- `CDIServiceLocator` permite obter beans CDI fora do contexto de injeção (ex.: dentro de listeners)
- O interceptor `@Transactional` customizado em `util/jpa/` é usado nos repositories; algumas services usam `@javax.transaction.Transactional` diretamente
