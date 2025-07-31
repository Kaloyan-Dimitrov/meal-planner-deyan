package com.deyan.mealplanner.config;
import java.time.Duration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.*;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.*;

@Configuration
@EnableCaching
public class RedisConfig {

    /** Human-readable JSON values. */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory cf) {
        RedisTemplate<String, Object> tpl = new RedisTemplate<>();
        tpl.setConnectionFactory(cf);

        var jsonSer = new GenericJackson2JsonRedisSerializer();
        tpl.setKeySerializer(new StringRedisSerializer());
        tpl.setValueSerializer(jsonSer);
        tpl.setHashKeySerializer(new StringRedisSerializer());
        tpl.setHashValueSerializer(jsonSer);

        return tpl;
    }

    /** Cache manager with a default TTL of 24 h on cache “recipes”. */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory cf) {
        RedisCacheConfiguration cfg = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(24))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJackson2JsonRedisSerializer()
                        )
                );
        return RedisCacheManager.builder(cf)
                .withCacheConfiguration("recipes", cfg)
                .build();
    }

    /** Optional: shorter key names (fewer Redis bytes). */
    @Bean
    public SimpleKeyGenerator keyGenerator() { return new SimpleKeyGenerator(); }
}