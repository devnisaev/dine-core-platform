package com.dinecore.menu.cache;

import com.dinecore.menu.api.MenuView;
import org.springframework.data.redis.core.StringRedisTemplate;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class RedisMenuCache implements MenuCache {

    private static final Duration TTL = Duration.ofHours(2);

    private final StringRedisTemplate redis;
    private final JsonMapper json;

    public RedisMenuCache(StringRedisTemplate redis, JsonMapper json) {
        this.redis = redis;
        this.json = json;
    }

    @Override
    public Optional<MenuView> get(UUID branchId) {
        String raw = redis.opsForValue().get(branchKey(branchId));
        if (raw == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(parse(raw));
    }

    @Override
    public void put(UUID organizationId, UUID branchId, MenuView view) {
        redis.opsForValue().set(branchKey(branchId), json.writeValueAsString(view), TTL);
        redis.opsForSet().add(orgKey(organizationId), branchId.toString());
    }

    @Override
    public void evictBranch(UUID branchId) {
        redis.delete(branchKey(branchId));
    }

    @Override
    public void evictOrganization(UUID organizationId) {
        Set<String> members = redis.opsForSet().members(orgKey(organizationId));
        if (members != null) {
            members.forEach(id -> redis.delete(branchKey(UUID.fromString(id))));
        }
    }

    private MenuView parse(String raw) {
        try {
            return json.readValue(raw, MenuView.class);
        } catch (JacksonException ex) {
            return null;
        }
    }

    private static String branchKey(UUID branchId) {
        return "menu:branch:" + branchId;
    }

    private static String orgKey(UUID organizationId) {
        return "menu:org:" + organizationId + ":branches";
    }
}
