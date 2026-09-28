package com.dinecore.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiAccessTest {

    @Test
    void waiterCannotCreateAnOrganization() {
        AccessVerdict verdict = ApiAccess.decide("POST", "/api/v1/organizations", "WAITER", branchA(), branchA());
        assertEquals(AccessVerdict.Decision.FORBIDDEN, verdict.decision());
        assertEquals("FORBIDDEN", verdict.code());
    }

    @Test
    void waiterHeaderForAnotherBranchIsRejected() {
        AccessVerdict verdict = ApiAccess.decide("GET", "/api/v1/settings", "WAITER", branchA(), branchB());
        assertEquals(AccessVerdict.Decision.FORBIDDEN, verdict.decision());
        assertEquals("TENANT_MISMATCH", verdict.code());
    }

    @Test
    void matchingWaiterCanReadSettings() {
        AccessVerdict verdict = ApiAccess.decide("GET", "/api/v1/settings", "WAITER", branchA(), branchA());
        assertEquals(AccessVerdict.Decision.ALLOW, verdict.decision());
    }

    @Test
    void superAdminMayActOnAnotherBranch() {
        AccessVerdict verdict = ApiAccess.decide("GET", "/api/v1/settings", "SUPER_ADMIN", null, branchB());
        assertEquals(AccessVerdict.Decision.ALLOW, verdict.decision());
    }

    @Test
    void branchScopedCallWithoutHeaderIsRejected() {
        AccessVerdict verdict = ApiAccess.decide("GET", "/api/v1/settings", "BRANCH_ADMIN", branchA(), " ");
        assertEquals("TENANT_MISMATCH", verdict.code());
    }

    @Test
    void loginAndHealthStayPublic() {
        assertEquals(AccessVerdict.Decision.ALLOW, ApiAccess.decide("POST", "/api/v1/auth/login", null, null, null).decision());
        assertEquals(AccessVerdict.Decision.ALLOW, ApiAccess.decide("GET", "/actuator/health", null, null, null).decision());
    }

    @Test
    void missingTokenOnAProtectedRouteIsUnauthorized() {
        assertEquals(AccessVerdict.Decision.UNAUTHORIZED, ApiAccess.decide("GET", "/api/v1/branches", null, null, null).decision());
    }

    @Test
    void waiterCannotEditTheCatalog() {
        AccessVerdict verdict = ApiAccess.decide("POST", "/api/v1/categories", "WAITER", branchA(), branchA());
        assertEquals("FORBIDDEN", verdict.code());
    }

    @Test
    void superAdminCanCreateADishWithoutABranchHeader() {
        AccessVerdict verdict = ApiAccess.decide("POST", "/api/v1/dishes", "SUPER_ADMIN", null, null);
        assertEquals(AccessVerdict.Decision.ALLOW, verdict.decision());
    }

    @Test
    void waiterReadsOnlyTheirBranchMenu() {
        assertEquals(AccessVerdict.Decision.ALLOW, ApiAccess.decide("GET", "/api/v1/menu", "WAITER", branchA(), branchA()).decision());
        assertEquals("TENANT_MISMATCH", ApiAccess.decide("GET", "/api/v1/menu", "WAITER", branchA(), branchB()).code());
    }

    @Test
    void onlyBranchAdminSetsAnOverride() {
        String path = "/api/v1/dishes/" + branchA() + "/override";
        assertEquals(AccessVerdict.Decision.ALLOW, ApiAccess.decide("PUT", path, "BRANCH_ADMIN", branchA(), branchA()).decision());
        assertEquals("FORBIDDEN", ApiAccess.decide("PUT", path, "WAITER", branchA(), branchA()).code());
    }

    @Test
    void waiterCannotCreateATable() {
        assertEquals("FORBIDDEN", ApiAccess.decide("POST", "/api/v1/tables", "WAITER", branchA(), branchA()).code());
        assertEquals(AccessVerdict.Decision.ALLOW, ApiAccess.decide("POST", "/api/v1/tables", "BRANCH_ADMIN", branchA(), branchA()).decision());
    }

    @Test
    void kitchenMarksReadyAndWaiterSubmits() {
        String ready = "/api/v1/orders/" + branchA() + "/items/" + branchB() + "/ready";
        assertEquals(AccessVerdict.Decision.ALLOW, ApiAccess.decide("POST", ready, "KITCHEN", branchA(), branchA()).decision());
        assertEquals("FORBIDDEN", ApiAccess.decide("POST", ready, "WAITER", branchA(), branchA()).code());
        assertEquals(AccessVerdict.Decision.ALLOW, ApiAccess.decide("POST", "/api/v1/orders", "WAITER", branchA(), branchA()).decision());
    }

    @Test
    void orderCallForAnotherBranchIsRejected() {
        assertEquals("TENANT_MISMATCH", ApiAccess.decide("GET", "/api/v1/orders", "WAITER", branchA(), branchB()).code());
    }

    private static String branchA() {
        return "11111111-1111-1111-1111-111111111111";
    }

    private static String branchB() {
        return "22222222-2222-2222-2222-222222222222";
    }
}
