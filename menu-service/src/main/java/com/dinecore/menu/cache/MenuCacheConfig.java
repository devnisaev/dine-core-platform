package com.dinecore.menu.cache;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class MenuCacheConfig {

    @Bean
    @ConditionalOnProperty(name = "dinecore.menu.cache", havingValue = "memory")
    MenuCache memoryMenuCache() {
        return new MemoryMenuCache();
    }

    @Bean
    @ConditionalOnProperty(name = "dinecore.menu.cache", havingValue = "redis")
    MenuCache redisMenuCache(StringRedisTemplate redis, JsonMapper json) {
        return new RedisMenuCache(redis, json);
    }
}
