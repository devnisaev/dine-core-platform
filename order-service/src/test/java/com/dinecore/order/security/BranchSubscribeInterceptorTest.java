package com.dinecore.order.security;

import com.dinecore.common.JwtClaims;
import com.dinecore.order.error.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ExecutorSubscribableChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BranchSubscribeInterceptorTest {

    private static final String BRANCH_A = "11111111-1111-1111-1111-111111111111";
    private static final String BRANCH_B = "22222222-2222-2222-2222-222222222222";

    @Test
    void otherBranchCannotSubscribe() {
        BranchSubscribeInterceptor interceptor = new BranchSubscribeInterceptor(token -> jwt(BRANCH_A));
        interceptor.preSend(connect(), new ExecutorSubscribableChannel());

        ApiException rejected = assertThrows(ApiException.class,
                () -> interceptor.preSend(subscribe(BRANCH_B), new ExecutorSubscribableChannel()));
        assertEquals("TENANT_MISMATCH", rejected.code());
    }

    @Test
    void ownBranchMaySubscribe() {
        BranchSubscribeInterceptor interceptor = new BranchSubscribeInterceptor(token -> jwt(BRANCH_A));
        interceptor.preSend(subscribe(BRANCH_A, new SocketUser("waiter", BRANCH_A)), new ExecutorSubscribableChannel());
    }

    private static Message<?> connect() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setNativeHeader("Authorization", "Bearer token");
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    private static Message<?> subscribe(String branchId) {
        return subscribe(branchId, new SocketUser("waiter", BRANCH_A));
    }

    private static Message<?> subscribe(String branchId, SocketUser user) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/topic/branch/" + branchId + "/kitchen");
        accessor.setUser(user);
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    private static Jwt jwt(String tenantId) {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("waiter")
                .claim(JwtClaims.TENANT_ID, tenantId)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .build();
    }
}
