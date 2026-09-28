# DineCore

DineCore is a multi-branch restaurant operating system. A restaurant group owns one shared menu. Each branch is a tenant: it overrides price and availability, takes orders, shows them to its own kitchen, and will close cheques only for that branch.

The backend is a set of Java 21 services behind one API gateway. Services do not share a database schema. The branch id travels on `X-Tenant-ID`, never in the JSON body.

## Stack

| | |
|---|---|
| Language | Java 21 |
| Services | Spring Boot 4.1 |
| Gateway | Spring Cloud Gateway 2025.1 (WebFlux) |
| Business services | Spring MVC |
| Database | PostgreSQL 16, one schema per service, Flyway |
| Cache | Redis 7, branch menu only |
| Messages | Apache Kafka |
| Live kitchen channel | STOMP on `/ws` inside order-service |
| Auth | Stateless JWT (HS256). No refresh token |
| Money and ids | `BigDecimal`, UUID, UTC timestamps |
| Build | Maven multi-module (`./mvnw`) |

## Services

| Module | Port | Owns | Responsibility |
|---|---|---|---|
| `gateway-service` | 8080 | — | Routes, JWT check, tenant-header check, CORS, `X-Request-Id` |
| `tenant-service` | 8081 | schema `tenant` | Login, organizations, branches, settings, users, shifts |
| `menu-service` | 8082 | schema `menu` | Organization catalog, branch price overrides, cached waiter menu |
| `order-service` | 8083 | schema `orders` | Tables, orders, price snapshot, kitchen STOMP channel |
| `billing-service` | 8084 | schema `billing` | Cheques, payments, Z-report (not built yet; health only) |
| `dinecore-common` | — | — | Roles, access rules, error JSON, header names |
| `dinecore-tenant-web` | — | — | Servlet filter that stores `X-Tenant-ID` for the request |

Local infrastructure from `docker-compose.yml`: PostgreSQL `5432`, Redis `6379`, Kafka `9092`. Database `dinecore`, user `dinecore`, password `dinecore`. Schemas `tenant`, `menu`, `orders`, and `billing` are created on first Postgres start.

## Roles

| Role | Scope |
|---|---|
| `SUPER_ADMIN` | Organizations, branches, catalog. May act on any branch by setting `X-Tenant-ID`. The token has no `tenant_id`. |
| `BRANCH_ADMIN` | Branch settings, menu overrides, staff, cancel a kitchen order |
| `WAITER` | Tables and orders for their own branch |
| `KITCHEN` | Mark queued items ready. No menu edits and no bills |

JWT claims: `sub`, `role`, `organization_id`, `tenant_id` (omitted for super admin), `exp`.

For `WAITER`, `KITCHEN`, and `BRANCH_ADMIN`, `X-Tenant-ID` must equal the token’s `tenant_id`. A mismatch is `403` `TENANT_MISMATCH`. Login is the only public API: `POST /api/v1/auth/login`.

## Gateway routes

| Prefix | Service |
|---|---|
| `/api/v1/auth`, `/organizations`, `/branches`, `/users`, `/settings`, `/shifts` | tenant-service |
| `/api/v1/categories`, `/dishes`, `/menu` | menu-service |
| `/api/v1/tables`, `/orders`, `/ws` | order-service |
| `/api/v1/cheques`, `/reports` | billing-service |

The catalog belongs to the organization. `GET /api/v1/menu` returns the menu for the branch in `X-Tenant-ID`: override price when set, otherwise the base price. A dish marked unavailable is hidden from that branch only. The resolved menu is cached in Redis as `menu:branch:{branchId}` for two hours.

An order copies the dish price from menu-service at submit time. A later override does not change that line. Submit publishes `OrderSubmitted` and pushes `/topic/branch/{branchId}/kitchen`. A STOMP session may subscribe only to its own branch.

Kafka topics: `dinecore.orders`, `dinecore.menu`, `dinecore.billing`.

## What is implemented

Identity, the gateway, the menu, and orders are in place, with JUnit covering tenant mismatch, menu isolation, empty submit, the submit-time price, and kitchen-topic access.

Billing (cheques, split cash/card payments, Z-report) is the next service. `billing-service` currently answers `/actuator/health` only.

## Run the tests

Tests do not need Docker. Tenant, menu, and order Spring tests use in-memory H2.

```bash
./mvnw test
```

## Run locally

Start Postgres, Redis, and Kafka, then the services. Tenant seed data loads only with the `dev` profile.

```bash
docker compose up -d

./mvnw -pl tenant-service spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=dev
./mvnw -pl menu-service spring-boot:run
./mvnw -pl order-service spring-boot:run
./mvnw -pl billing-service spring-boot:run
./mvnw -pl gateway-service spring-boot:run
```

The `dev` profile seeds organization **Bishkek Group** (`bishkek-group`), branches **Bishkek 01** and **Bishkek 02** (currency KGS, timezone Asia/Bishkek), and users `super`, `admin`, `waiter`, and `kitchen`. Password for each is `password`. `admin`, `waiter`, and `kitchen` belong to Bishkek 01.

```bash
curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"waiter","password":"password"}'
```

Call branch APIs through the gateway with `Authorization: Bearer <token>` and `X-Tenant-ID` set to that user’s branch id.

## Further reading

The build contract is [docs/dinecore-technical-specification.md](docs/dinecore-technical-specification.md). The notes at the repository root (brand pitch, architecture, service breakdown) are background; where they disagree with the specification, the specification wins.
