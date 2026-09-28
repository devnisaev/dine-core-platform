# 2. Core Microservices Breakdown

## 2.1 API Gateway & Security Service (`gateway-service`)
* **Framework:** Spring Cloud Gateway, Spring Security 7, OAuth2 Resource Server.
* **Responsibilities:** JWT validation, role-based access control (`ROLE_SUPER_ADMIN`, `ROLE_BRANCH_ADMIN`, `ROLE_WAITER`), and tenant header injection.

## 2.2 Menu & Catalog Service (`menu-service`)
* **Responsibilities:** Branch-specific dishes, categories, modifiers, and dynamic price overrides.
* **Caching:** Redis caching using keys structured as `menu:branch:{tenant_id}:categories` with instant invalidation via event listeners.

## 2.3 Order & Table Management Service (`order-service`)
* **Responsibilities:** Real-time table status tracking and order lifecycle management (Draft $\rightarrow$ Submitted $\rightarrow$ Sent to Kitchen $\rightarrow$ Ready $\rightarrow$ Served).
* **Real-Time Sync:** Spring WebSocket with STOMP protocol pushing updates to Kitchen Display Systems (KDS).

## 2.4 Billing & Statement Cheque Service (`billing-service`)
* **Responsibilities:** Itemized cheque generation, split payments, cash/card logs, and shift Z-reports.