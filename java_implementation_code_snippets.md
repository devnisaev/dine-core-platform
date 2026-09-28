# 3. Java & Hibernate Implementation Details

## 3.1 Base Tenant-Aware Entity Pattern
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
}
```

## 3.2 AOP Session Filtering Aspect
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