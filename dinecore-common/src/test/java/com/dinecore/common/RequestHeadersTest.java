package com.dinecore.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RequestHeadersTest {

    @Test
    void namesMatchTheContract() {
        assertEquals("X-Tenant-ID", RequestHeaders.TENANT_ID);
        assertEquals("X-Request-Id", RequestHeaders.REQUEST_ID);
    }
}
