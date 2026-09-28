package com.dinecore.order.security;

import com.dinecore.order.error.ApiException;
import com.dinecore.order.notify.TopicAccess;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BranchSubscribeInterceptor implements ChannelInterceptor {

    private final JwtDecoder decoder;

    public BranchSubscribeInterceptor(JwtDecoder decoder) {
        this.decoder = decoder;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            accessor.setUser(principal(accessor));
        }
        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            requireBranch(accessor);
        }
        return message;
    }

    private SocketUser principal(StompHeaderAccessor accessor) {
        String header = first(accessor.getNativeHeader("Authorization"));
        if (header == null || !header.startsWith("Bearer ")) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "AUTH", "Authentication required");
        }
        Jwt jwt = decoder.decode(header.substring("Bearer ".length()));
        return new SocketUser(jwt.getSubject(), jwt.getClaimAsString("tenant_id"));
    }

    private static void requireBranch(StompHeaderAccessor accessor) {
        String tenant = accessor.getUser() instanceof SocketUser user ? user.tenantId() : null;
        if (!TopicAccess.allows(accessor.getDestination(), tenant)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "TENANT_MISMATCH", "Tenant header does not match the token");
        }
    }

    private static String first(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.get(0);
    }
}
