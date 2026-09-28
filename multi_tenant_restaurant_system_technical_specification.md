# Technical Specification: Multi-Tenant Restaurant Management System
**Target Scale:** 5-7 Restaurant Branches | 20-30 Waiters per Branch | High Concurrency Real-Time Orders  
**Tech Stack:** Java 21, Spring Boot 4, Spring Cloud, Spring Security, Hibernate/JPA, PostgreSQL, Redis, WebSocket (STOMP), Apache Kafka / RabbitMQ

---

## 1. Architecture Overview & Multi-Tenancy Strategy

To support branch-level autonomy (custom menus, pricing, orders, cheques) while maintaining a centralized admin umbrella, the system utilizes a **Shared Database with Discriminator (`tenant_id`) & Hibernate Filters** approach. 

### 1.1 Tenant Context Propagation Flow
1. **Request Origin:** Client (Waiter App or Admin Web Dashboard) sends a request with header `X-Tenant-ID: branch_bisk_01` or via JWT claims.
2. **API Gateway (Spring Cloud Gateway):** Intercepts the request, validates the JWT, extracts `tenant_id`, and injects it into downstream request headers.
3. **Tenant Context Holder (`ThreadLocal`):** A custom Spring interceptor captures `X-Tenant-ID` at the service boundary and stores it in a thread-safe context bean.
4. **Data Layer Enforcement:** Hibernate automatically applies a global SQL `WHERE tenant_id = :currentTenant` filter to all entity queries, preventing cross-tenant data leaks.

---

## 2. Core Microservices & Java Implementation Details

### 2.1 API Gateway & Security Service (`gateway-service`)
* **Framework:** Spring Cloud Gateway, Spring Security 7, OAuth2 Resource Server.
* **Key Components:**
  * `TenantHeaderFilter.java`: Global WebFilter extracting tenant context.
  * `JwtAuthenticationManager.java`: Validates tokens and maps roles (`ROLE_SUPER_ADMIN`, `ROLE_BRANCH_ADMIN`, `ROLE_WAITER`) with branch scope restrictions.

### 2.2 Tenant & Branch Management Service (`tenant-service`)
* **Role:** Manages restaurant metadata, branch locations, operating hours, and configuration settings.
* **Key Entities (`JPA`):**
  * `Tenant` (`id`, `name`, `code`, `currency`, `timezone`, `status`, `created_at`)
  * `BranchSettings` (`id`, `tenant_id`, `tax_rate`, `service_charge_pct`, `receipt_footer_text`)

### 2.3 Menu & Catalog Service (`menu-service`)
* **Role:** Branch-specific dishes, categories, modifiers, and dynamic pricing overrides.
* **Key Technical Detail:** Since branches can override global base prices, use a polymorphic inheritance or override table model.
  * `GlobalDish` (`id`, `name`, `category_id`, `base_price`, `description`)
  * `BranchMenuOverride` (`id`, `tenant_id`, `dish_id`, `custom_price`, `is_available`)
* **Caching Strategy:** Cache active branch menus in Redis using keys structured as `menu:branch:{tenant_id}:categories` with a TTL of 2 hours, invalidated instantly via event listeners when a branch admin updates prices.

### 2.4 Order & Table Management Service (`order-service`)
* **Role:** Handles high-concurrency order placement for 20-30 active waiters per branch.
* **Key Entities:**
  * `Table` (`id`, `tenant_id`, `table_number`, `seating_capacity`, `status` [AVAILABLE, OCCUPIED, RESERVED])
  * `Order` (`id`, `tenant_id`, `table_id`, `waiter_id`, `status` [DRAFT, SENT_TO_KITCHEN, READY, SERVED, CANCELLED], `total_amount`)
  * `OrderItem` (`id`, `order_id`, `dish_id`, `quantity`, `unit_price`, `modifiers_json`, `status`)
* **Real-Time Communication:** 
  * Configure Spring WebSocket with STOMP protocol.
  * Kitchen Display System (KDS) and waiter devices subscribe to topics like `/topic/branch/{tenant_id}/kitchen` and `/topic/branch/{tenant_id}/waiter/{waiter_id}`.

### 2.5 Billing & Statement Cheque Service (`billing-service`)
* **Role:** Manages final customer bills, splits, payment logs, and Z-reports.
* **Key Requirements:**
  * Atomic cheque generation ensuring table locks during payment processing to prevent race conditions.
  * Statement calculation aggregating daily sub-totals, taxes, discounts, and tips per waiter shift.

---

## 3. Database Schema & Multi-Tenancy Implementation (Hibernate Filter Example)

### 3.1 Base Auditable & Tenant-Aware Entity Entity Pattern
```java
@MappedSuperclass
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = String.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public abstract class TenantAuditableEntity {
    
    @Column(name = "tenant_id", nullable = false, updatable = false)
    private String tenantId;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Getters and Setters
}
```

### 3.2 Aspect-Oriented Programming (AOP) for Session Filtering
```java
@Aspect
@Component
public class HibernateTenantAspect {

    @PersistenceContext
    private EntityManager entityManager;

    @Before("execution(* com.restaurant.service.*.*(..))")
    public void enableTenantFilter() {
        String tenantId = TenantContext.getCurrentTenant();
        if (tenantId != null) {
            Session session = entityManager.unwrap(Session.class);
            org.hibernate.Filter filter = session.enableFilter("tenantFilter");
            filter.setParameter("tenantId", tenantId);
        }
    }
}
```

---

## 4. Event-Driven Architecture (Kafka / RabbitMQ)

To decouple order creation from kitchen notifications and inventory tracking, use asynchronous messaging:

* **Event Topic:** `restaurant.orders.events`
* **Producer (`order-service`):** Publishes `OrderCreatedEvent` when a waiter submits an active order.
* **Consumers:**
  * **Kitchen Service / KDS:** Listens to events to immediately render the order on the branch kitchen screen.
  * **Analytics Service:** Aggregates hourly sales and popular dishes per branch.

---

## 5. Non-Functional Requirements & Scalability Checklist
1. **Connection Pooling:** Configure HikariCP per service to manage database connection bounds effectively across multiple microservices.
2. **Database Indexing:** Ensure composite indexes on `(tenant_id, status)` for `orders` and `(tenant_id, table_number)` for `tables` to maintain sub-50ms query speeds under heavy waiter usage.
3. **Offline Resilience (Mobile/Tablet Waiter App):** Implement local state caching (using SQLite/IndexedDB on the client side) so waiters can queue orders locally if Wi-Fi drops briefly inside the restaurant floor.