# 1. System Architecture & Multi-Tenancy Strategy

## 1.1 Architectural Style
To support branch-level autonomy across 5-7 restaurant locations with 20-30 concurrent waiters per branch, the system uses a **Microservices Architecture** built on **Spring Boot 4** and **Spring Cloud**.

## 1.2 Tenant Context Propagation Flow
1. **Request Origin:** Client sends a request with header `X-Tenant-ID: branch_01` or via JWT claims.
2. **API Gateway (Spring Cloud Gateway):** Intercepts the request, validates security tokens, extracts the `tenant_id`, and propagates it to downstream microservices.
3. **Tenant Context Holder (`ThreadLocal`):** A custom Spring interceptor captures the header and stores it in a thread-safe context bean for the duration of the request lifecycle.
4. **Data Isolation:** Hibernate automatically appends a global SQL filter (`WHERE tenant_id = :currentTenant`) to eliminate cross-tenant data leakage.