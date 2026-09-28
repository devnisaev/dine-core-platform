package com.dinecore.tenant.web;

import com.dinecore.common.RequestHeaders;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TenantContextFilterTest {

    private final TenantContextFilter filter = new TenantContextFilter();

    @Test
    void storesTheHeaderOnlyForTheRequest() throws Exception {
        MockHttpServletRequest request = request("branch-a");
        filter.doFilter(request, new MockHttpServletResponse(), seen("branch-a"));
        assertNull(TenantContext.get());
    }

    @Test
    void secondRequestDoesNotSeeThePreviousTenant() throws Exception {
        filter.doFilter(request("branch-a"), new MockHttpServletResponse(), seen("branch-a"));
        filter.doFilter(request("branch-b"), new MockHttpServletResponse(), seen("branch-b"));
        assertNull(TenantContext.get());
    }

    private static MockHttpServletRequest request(String tenantId) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(RequestHeaders.TENANT_ID, tenantId);
        return request;
    }

    private static FilterChain seen(String expected) {
        return (req, res) -> assertEquals(expected, TenantContext.get());
    }
}
