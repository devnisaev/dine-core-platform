# DineCore — Implementation Technical Specification

| | |
|---|---|
| Product | DineCore, multi-branch restaurant operating system |
| Status | Build specification. Supersedes the five source notes where they disagree. |
| Version | 1.0 |
| Date | 28 September 2026 |
| Audience | Engineers implementing the Java backend |

This document states what must be built. It is the working contract for the first runnable version. The notes already in the repository stay as background: brand pitch, architecture sketch, service list, and Hibernate samples.

---

## 1. What this version is

DineCore v1 is a set of Java services behind one API gateway. A restaurant group has many branches. Each branch is a tenant. Waiters take orders, the kitchen sees them immediately, and billing closes the cheque for that branch only.

Design load, not a scaling project: **5–10 branches**, about **30 concurrent waiters per branch**. Stateless services, correct indexes, and a message bus are enough. No sharding, no Kubernetes, no mobile client in this version.

### In scope

- Organization (restaurant group) and branches
- Users and JWT login with four roles
- Shared menu with per-branch price and availability
- Tables, orders, and a kitchen live channel
- Cheques, split payments recorded as cash or card, and a shift Z-report
- Tenant isolation on every branch-owned row

### Out of scope

- Waiter offline queue (SQLite / IndexedDB)
- Inventory, recipes, and stock deduction
- Analytics service and corporate dashboards
- Card-network or payment-gateway integration
- Public self-signup and subscription billing
- Kubernetes, service mesh, and multi-region deployment

---

## 2. Decisions locked for the build

The source notes leave several choices open. These are now fixed.

| Topic | Decision |
|---|---|
| Language | Java 21 |
| Application framework | Spring Boot 4.1.x |
| Cloud components | Spring Cloud 2025.1.x Oakwood (Gateway only) |
| Build | One Maven multi-module repository |
| Database | PostgreSQL 16. One server. One schema per service |
| Migrations | Flyway, owned by the service that owns the schema |
| Cache | Redis 7 for the branch menu only |
| Messages | Apache Kafka. RabbitMQ is not used |
| Live updates | Spring WebSocket with STOMP, inside `order-service` |
| Auth | Stateless JWT access tokens. No refresh token in v1 |
| Money | `BigDecimal` scale 2. Currency lives on the branch |
| Time | Store UTC. Business day uses the branch timezone |
| Identifiers | UUID |
| API | JSON over HTTP, prefix `/api/v1` |
| Local runtime | Docker Compose: Postgres, Redis, Kafka |
| Package root | `com.dinecore` |

### Data ownership

Each service owns its schema and is the only writer of its tables. Services do not share a Hibernate session or a single `tenantFilter`. The sample `HibernateTenantAspect` in the notes is not the implementation. It uses a Hibernate 5 filter API, a pointcut that matches one package level, and a `ThreadLocal` that will not follow Kafka listeners.

Tenant isolation inside a service is a repository rule: every branch-owned query includes `tenant_id` from the request context. A test must prove branch A cannot read branch B.

### Why a shared menu still has a tenant

A branch is the tenant. The dish catalog belongs to the **organization**, because every branch sells from the same card and then overrides price or availability. `tenant_id` on an order is the branch. `organization_id` on a dish is the group.

---

## 3. Repository to create

```text
dine-core-platform/
  pom.xml
  docker-compose.yml
  dinecore-common/            # events, error model, header names — plain Java
  dinecore-tenant-web/        # servlet filter + ThreadLocal for MVC services
  gateway-service/            # WebFlux. Own tenant filter. No JPA
  tenant-service/
  menu-service/
  order-service/
  billing-service/
```

`dinecore-common` must not depend on Spring Web. The gateway is reactive. The four business services are Spring MVC. Sharing one web starter would force the wrong stack onto one of them.

Suggested local ports:

| Process | Port |
|---|---|
| gateway-service | 8080 |
| tenant-service | 8081 |
| menu-service | 8082 |
| order-service | 8083 |
| billing-service | 8084 |
| PostgreSQL | 5432 |
| Redis | 6379 |
| Kafka | 9092 |

Schemas: `tenant`, `menu`, `orders`, `billing`.

---

## 4. Request path

1. The client calls the gateway with `Authorization: Bearer <jwt>` and, on branch-scoped calls, `X-Tenant-ID`.
2. The gateway checks the signature and expiry.
3. For `WAITER`, `KITCHEN`, and `BRANCH_ADMIN`, `X-Tenant-ID` must equal the `tenant_id` claim. A mismatch is `403`.
4. `SUPER_ADMIN` may pass any branch id in `X-Tenant-ID` when acting on one branch.
5. The gateway forwards the header. It does not open a database.
6. The target service copies `X-Tenant-ID` into a request-scoped context and clears it when the request ends, including on failure.
7. Repositories read that context. They never trust a tenant id in the JSON body.

Login (`POST /api/v1/auth/login`) is the only public route.

---

## 5. Identity and access

Roles:

| Role | May do |
|---|---|
| `SUPER_ADMIN` | Create organizations, branches, and users. Read any branch by setting the header |
| `BRANCH_ADMIN` | Settings, menu overrides, staff in their branch, cancel a kitchen order, close a shift |
| `WAITER` | Tables and orders for their branch, request and take payment |
| `KITCHEN` | Read the kitchen queue and move an item to ready. No bills, no menu edits |

JWT claims: `sub` (user id), `role`, `organization_id`, `tenant_id` (absent for super admin), `exp`.

Passwords are stored as BCrypt hashes. Login returns the access token, expiry, role, organization id, and tenant id.

---

## 6. Service work

### 6.1 `gateway-service`

No database. Routes:

| Prefix | Downstream |
|---|---|
| `/api/v1/auth/**`, `/api/v1/organizations/**`, `/api/v1/branches/**`, `/api/v1/users/**`, `/api/v1/settings/**`, `/api/v1/shifts/**` | tenant-service |
| `/api/v1/categories/**`, `/api/v1/dishes/**`, `/api/v1/menu/**` | menu-service |
| `/api/v1/tables/**`, `/api/v1/orders/**`, `/ws/**` | order-service |
| `/api/v1/cheques/**`, `/api/v1/reports/**` | billing-service |

Also: CORS for a local admin origin, propagate `X-Request-Id`, and refuse branch-scoped calls that omit `X-Tenant-ID`.

### 6.2 `tenant-service`

Schema `tenant`.

| Entity | Fields that matter |
|---|---|
| Organization | id, name, code, status |
| Branch | id (this is the tenant id), organization_id, name, code, currency, timezone, status |
| BranchSettings | branch_id, tax_rate, service_charge_pct, receipt_footer_text |
| User | id, organization_id, branch_id (null for super admin), username, password_hash, role, status |
| Shift | id, branch_id, user_id, opened_at, closed_at |

Endpoints:

| Method | Path | Who |
|---|---|---|
| POST | `/api/v1/auth/login` | public |
| POST | `/api/v1/organizations` | super admin |
| GET | `/api/v1/organizations` | super admin |
| POST | `/api/v1/branches` | super admin |
| GET | `/api/v1/branches` | super admin, or branch admin (own org) |
| GET | `/api/v1/settings` | branch roles, current tenant |
| PUT | `/api/v1/settings` | branch admin |
| POST | `/api/v1/users` | super admin, or branch admin for waiter and kitchen in their branch |
| GET | `/api/v1/users` | same scope as create |
| POST | `/api/v1/shifts/open` | waiter, kitchen, branch admin |
| POST | `/api/v1/shifts/close` | the user who opened it, or branch admin |

### 6.3 `menu-service`

Schema `menu`. Catalog is organization-wide. Overrides are per branch.

| Entity | Fields that matter |
|---|---|
| Category | id, organization_id, name, sort_order, active |
| Dish | id, organization_id, category_id, name, description, base_price, active |
| Modifier | id, dish_id, name, price_delta |
| BranchMenuOverride | branch_id, dish_id, custom_price (null means base price), available |

Effective price = `custom_price` when set, otherwise `base_price`. A dish with `available = false` is hidden from the waiter menu.

Endpoints:

| Method | Path | Who |
|---|---|---|
| GET | `/api/v1/menu` | any branch role. Resolved menu for `X-Tenant-ID` |
| POST / PATCH | `/api/v1/categories`, `/api/v1/dishes`, `/api/v1/dishes/{id}/modifiers` | super admin |
| PUT | `/api/v1/dishes/{id}/override` | branch admin. Body: custom price and available flag |

Cache: Redis key `menu:branch:{branchId}`, TTL 2 hours. Delete that key when this branch’s override changes. When a dish or category changes, delete every cached key for branches of that organization (publish `MenuChanged`, or delete known keys). The waiter menu read path uses the cache. A cache miss loads from Postgres and fills Redis.

### 6.4 `order-service`

Schema `orders`.

| Entity | Fields that matter |
|---|---|
| DiningTable | id, branch_id, table_number, seats, status |
| Order | id, branch_id, table_id, waiter_id, status, note |
| OrderItem | id, order_id, dish_id, name_snapshot, unit_price, quantity, modifiers_json, status |

Table status: `AVAILABLE`, `OCCUPIED`, `RESERVED`, `PAYMENT_IN_PROGRESS`.

Order status:

| From | To | Who |
|---|---|---|
| — | `DRAFT` | waiter opens a table |
| `DRAFT` | `SUBMITTED` | waiter sends the order |
| `SUBMITTED` | `SENT_TO_KITCHEN` | service, in the same action as a successful publish |
| `SENT_TO_KITCHEN` | `READY` | kitchen, when every item is ready |
| `READY` | `SERVED` | waiter |
| `DRAFT` or `SUBMITTED` | `CANCELLED` | waiter or branch admin |
| `SENT_TO_KITCHEN` | `CANCELLED` | branch admin only |

Item status follows the kitchen: `QUEUED`, `READY`, `CANCELLED`. Opening an order on an available table sets the table to `OCCUPIED`. Price on the item is copied from the menu at submit time and does not change if the menu later changes.

Menu prices are read from `menu-service` over HTTP at submit time. `order-service` does not query the `menu` schema.

Endpoints:

| Method | Path | Who |
|---|---|---|
| GET / POST | `/api/v1/tables` | branch admin creates. Waiter reads |
| POST | `/api/v1/orders` | waiter. Body: table id |
| POST | `/api/v1/orders/{id}/items` | waiter, while `DRAFT` |
| POST | `/api/v1/orders/{id}/submit` | waiter |
| POST | `/api/v1/orders/{id}/cancel` | as in the status table |
| POST | `/api/v1/orders/{id}/items/{itemId}/ready` | kitchen |
| POST | `/api/v1/orders/{id}/served` | waiter |
| GET | `/api/v1/orders?status=` | branch roles, filtered by role need |

STOMP endpoint: `/ws`.

| Subscribe | Payload |
|---|---|
| `/topic/branch/{branchId}/kitchen` | order submitted, item ready, order cancelled |
| `/topic/branch/{branchId}/tables` | table status |
| `/topic/branch/{branchId}/waiter/{waiterId}` | ready and served notices for that waiter |

The connect handshake requires the same JWT. A session may subscribe only to its own branch. Kitchen and waiter topics are server-push; clients do not send orders over the socket in v1. Orders are created with HTTP so they are transactional. The socket is the notification channel.

On submit, after the order is stored, publish `OrderSubmitted` and push the kitchen topic. If Kafka publish fails, the submit fails and the order stays `DRAFT`.

### 6.5 `billing-service`

Schema `billing`. Consumes order events. Does not read the `orders` tables.

| Entity | Fields that matter |
|---|---|
| Cheque | id, branch_id, order_id, table_id, waiter_id, shift_id, status, subtotal, tax, service_charge, discount, tip, total |
| ChequeLine | cheque_id, dish name, quantity, unit price, line total |
| Payment | id, cheque_id, method (`CASH` or `CARD`), amount |
| ProcessedEvent | event_id unique |
| ZReport | id, branch_id, shift_id, business_date, totals by method, tax, tips |

Cheque status: `OPEN`, `PAID`, `VOID`.

Flow:

1. Consume `OrderServed`. Create an `OPEN` cheque from the event payload (lines and prices already snapshotted). Ignore duplicate `event_id`.
2. Waiter requests the cheque. Billing asks order-service to set the table to `PAYMENT_IN_PROGRESS` (HTTP). A second payment request for the same order returns the existing open cheque.
3. Record payments until the paid sum equals the total. Then mark `PAID`, publish `ChequePaid`, and tell order-service to set the order `CLOSED` and the table `AVAILABLE`.
4. Partial payments are allowed. Overpayment is rejected.
5. Void is branch admin only, and only before any payment.

Totals: subtotal from lines, tax = subtotal × branch tax rate, service charge = subtotal × service charge percent, total = subtotal + tax + service charge − discount + tip. Rates come from tenant-service settings for that branch. Round half-up to 2 decimal places.

Z-report: `GET /api/v1/reports/z?shiftId=` for the shift’s waiter or a branch admin. It sums paid cheques whose `shift_id` is that shift. The waiter’s open shift id is sent when the cheque is requested. Billing does not read the `tenant` schema to discover the shift.

---

## 7. Events

Topic names:

| Topic | Producers | Consumers |
|---|---|---|
| `dinecore.orders` | order-service | billing-service, and the STOMP bridge inside order-service if the push is not in-process |
| `dinecore.menu` | menu-service | menu-service cache invalidation (same service, other instances) |
| `dinecore.billing` | billing-service | order-service (close table) |

Envelope, JSON:

```json
{
  "eventId": "uuid",
  "eventType": "OrderSubmitted",
  "occurredAt": "2026-09-28T12:00:00Z",
  "organizationId": "uuid",
  "tenantId": "uuid",
  "payload": {}
}
```

| eventType | When | Payload must include |
|---|---|---|
| `OrderSubmitted` | waiter submits | orderId, tableNumber, waiterId, items (dishId, name, qty, unitPrice, modifiers) |
| `OrderReady` | kitchen finishes | orderId, waiterId |
| `OrderServed` | waiter marks served | full priced lines, so billing can create a cheque without reading order tables |
| `OrderCancelled` | cancel after submit | orderId, reason |
| `MenuChanged` | dish, category, or override write | organizationId, branchId or null if the whole catalog changed |
| `ChequePaid` | cheque fully paid | orderId, chequeId, tableId |

Consumers store `eventId` and skip repeats. Tenant id is taken from the envelope, never from a leftover thread local.

---

## 8. Persistence rules

- Branch-owned tables have `branch_id NOT NULL`. Organization catalog tables have `organization_id NOT NULL` and no `branch_id`.
- Composite indexes:
  - `orders.orders (branch_id, status)`
  - `orders.dining_table (branch_id, table_number)` unique
  - `menu.branch_menu_override (branch_id, dish_id)` unique
  - `billing.cheque (branch_id, status)`
  - `billing.processed_event (event_id)` unique
- Foreign keys stay inside one schema. Cross-service references are UUID columns without a database foreign key.
- HikariCP pool per service. A small pool is enough at the design load (about 10 connections each).
- Optimistic locking (`@Version`) on `Order` and `Cheque`.

---

## 9. Errors and headers

Error body:

```json
{
  "code": "ORDER_STATE",
  "message": "Cannot submit an empty order",
  "traceId": "same as X-Request-Id"
}
```

| HTTP | When |
|---|---|
| 400 | Validation, illegal state change, empty submit, overpayment |
| 401 | Missing or bad token |
| 403 | Role or tenant mismatch |
| 404 | Id not found in this tenant (do not reveal that it exists in another tenant) |
| 409 | Version conflict or duplicate username / table number |

Header names: `Authorization`, `X-Tenant-ID`, `X-Request-Id`.

---

## 10. What to do, in order

Each phase ends with `./mvnw test` green and the acceptance checks below. Do not start a later phase by stubbing its database inside an earlier service.

### Phase 0 — Repository skeleton

- Parent POM, Java 21, dependency management for Boot and Cloud.
- Modules listed in section 3, each with a bootable application class and a health endpoint.
- `docker-compose.yml` for Postgres, Redis, and Kafka.
- Checkstyle or none. No extra architecture frameworks.

**Done when:** `docker compose up -d` brings the infrastructure up, and each service answers `/actuator/health`.

### Phase 1 — Identity and gateway

- `dinecore-common` and `dinecore-tenant-web`.
- Flyway schema `tenant`.
- Login, organizations, branches, settings, users, shifts.
- Gateway route and JWT checks from section 6.1 and section 4.
- Seed one organization, two branches, one user of each role (dev profile only).

**Done when:**

- A waiter token cannot call super-admin organization create.
- A waiter of branch A with `X-Tenant-ID` of branch B receives `403`.
- Branch settings of A are not returned to a user of B.

### Phase 2 — Menu

- Flyway schema `menu`.
- Catalog CRUD, branch override, `GET /api/v1/menu`.
- Redis cache and `MenuChanged` invalidation.
- Integration test with Redis (Testcontainers).

**Done when:** changing an override changes the next menu read for that branch only, and the other branch still sees its own price.

### Phase 3 — Orders and kitchen channel

- Flyway schema `orders`.
- Tables, draft items, submit, kitchen ready, served, cancel.
- HTTP client to menu-service for the price snapshot.
- STOMP topics and JWT on connect.
- Publish `OrderSubmitted`, `OrderReady`, `OrderServed`, `OrderCancelled`.

**Done when:**

- Submit with no items returns `400` and publishes nothing.
- After submit, a kitchen subscriber for that branch receives the order.
- A subscriber authenticated as the other branch cannot subscribe to this branch’s topic.
- Item `unit_price` stays at the submit-time price after a later override.

### Phase 4 — Billing

- Flyway schema `billing`.
- Consume `OrderServed`, open cheque, take split payments, void, Z-report.
- Call order-service to lock the table and to close it.
- Idempotent consumer.

**Done when:**

- The same `OrderServed` twice creates one cheque.
- Two payments that sum to the total close the cheque and free the table.
- A payment above the remainder returns `400` and does not change the cheque.
- A Z-report for shift A does not include cheques from shift B.

### Phase 5 — Hardening (only after 0–4)

- Structured JSON logs with `traceId` and `tenantId`.
- Prometheus metrics via Actuator. Dashboards are optional and not required to call the phase done.
- Gateway request logging.
- A short `docs/local-run.md` written only when the commands are real: compose, seed login, and the happy path.

**Done when:** one scripted path works through the gateway: login as waiter, read menu, open table, add item, submit, mark ready, mark served, pay cash, read the Z-report.

---

## 11. Tests required

Use JUnit 5. Use Testcontainers for PostgreSQL, and for Redis and Kafka in the modules that need them. Do not mock the database in tenant-isolation tests.

| Test | Where |
|---|---|
| Branch A cannot read or update branch B rows | each business service |
| Role denied by Spring Security | each protected endpoint group |
| Illegal order transition | order-service |
| Empty submit does not publish | order-service |
| Price snapshot | order-service with menu stub or container |
| Cheque idempotency and overpayment | billing-service |
| Cache key deleted on override | menu-service |

Unit tests cover money rounding and the order state machine. WebSocket tests cover rejected cross-branch subscribe.

---

## 12. Happy path (acceptance)

1. Super admin creates organization “Bishkek Group”, branches `bishkek-01` and `bishkek-02`, a branch admin, a waiter, and a kitchen user for branch 01.
2. Super admin creates category “Hot” and dish “Lagman” at base price 450.00.
3. Branch admin of branch 01 sets override price 480.00.
4. Waiter of branch 01 opens table 4, adds two Lagman, submits.
5. Kitchen display subscribed to `/topic/branch/{branch01}/kitchen` shows the ticket.
6. Kitchen marks items ready. Waiter marks served.
7. Billing opens a cheque: subtotal 960.00, plus tax and service charge from branch 01 settings.
8. Waiter records cash for the full total. Table 4 becomes `AVAILABLE`. Order becomes `CLOSED`.
9. Waiter of branch 02 never sees the order, the cheque, or the override.

---

## 13. Risks to watch during implementation

| Risk | What to do |
|---|---|
| Tenant context leaking across requests on a pooled thread | Clear the `ThreadLocal` in a `finally` block. Test with two sequential requests on the same server thread if the runtime allows it |
| Kafka listener using the HTTP tenant context | Pass `tenantId` only inside the event |
| Gateway and MVC filters drifting apart | Header name and mismatch rule live as constants and tests in one place. Behavior is duplicated only at the WebFlux vs servlet boundary |
| Shared database creeping back | Code review rejects a JPA entity that maps another service’s schema |
| Double submit from a double tap | Submit is idempotent for an order already past `DRAFT`: return the current order, do not publish a second `OrderSubmitted` |

---

## 14. Source notes and how they map

| Existing file | Use |
|---|---|
| `dinecore_brand_profile_and_investor_pitch.md` | Product context only. Market and fundraising text is not a build requirement |
| `system_architecture_and_multi_tenancy.md` | Tenant header flow. Implemented as section 4, with the JWT match rule added |
| `core_microservices_breakdown.md` | Service list. `tenant-service` from the longer spec is included |
| `multi_tenant_restaurant_system_technical_specification.md` | Entities, roles, cache key, kitchen topics, cheque and Z-report. Adapted to schema-per-service |
| `java_implementation_code_snippets.md` | Rejected as implementation. See section 2 |

Pitch scale (50+ sites, 150+ waiters) is a later target. This specification builds the 5–10 branch system those notes can run on without a second rewrite of service boundaries.
