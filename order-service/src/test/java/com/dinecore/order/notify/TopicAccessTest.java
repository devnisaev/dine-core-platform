package com.dinecore.order.notify;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TopicAccessTest {

    @Test
    void aSessionMaySubscribeOnlyToItsOwnBranch() {
        String branchA = "11111111-1111-1111-1111-111111111111";
        String branchB = "22222222-2222-2222-2222-222222222222";
        String waiter = "33333333-3333-3333-3333-333333333333";

        assertTrue(TopicAccess.allows("/topic/branch/" + branchA + "/kitchen", branchA));
        assertTrue(TopicAccess.allows("/topic/branch/" + branchA + "/waiter/" + waiter, branchA));
        assertFalse(TopicAccess.allows("/topic/branch/" + branchB + "/kitchen", branchA));
        assertFalse(TopicAccess.allows("/topic/branch/" + branchA + "/kitchen", null));
    }
}
