package com.example.cache.config;

import com.example.cache.config.chaincache.ChainedCacheManager;
import com.example.cache.config.logcache.LoggingCacheManager;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j

@Configuration
public class CacheConfig extends CachingConfigurerSupport {

    @Value("${cache.redis.host}")
    private String redisHost;

    @Value("${cache.redis.port}")
    private Integer redisPort;

    @Value("${cache.redis.time-to-live}")
    private Long redisTtlSecond;



    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        return new JedisConnectionFactory(new RedisStandaloneConfiguration(redisHost, redisPort));
    }

    @Bean
    public CacheManager redisCacheManager() {
        RedisCacheManager.RedisCacheManagerBuilder builder = RedisCacheManager.RedisCacheManagerBuilder.fromConnectionFactory(jedisConnectionFactory());

        RedisCacheConfiguration defaultConfig =
                RedisCacheConfiguration.defaultCacheConfig()
                        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                        .entryTtl(Duration.ofSeconds(redisTtlSecond));

        builder.cacheDefaults(defaultConfig);

        return new LoggingCacheManager(builder.build(), "Global-Redis-Cache");
    }

    @Bean
    public CacheManager caffeineCacheManager() {
        log.info("##caffeineCacheManager");
        SimpleCacheManager caffeineCacheManager = new SimpleCacheManager();
        List<Cache> caches = Arrays.stream(CacheType.values())
                .map(cache -> new CaffeineCache(cache.getName(), Caffeine.newBuilder().recordStats()
                                .expireAfterWrite(cache.getExpiredAfterWrite(), TimeUnit.SECONDS)
                                .maximumSize(cache.getMaximumSize())
                                .build()
                        )
                )
                .collect(Collectors.toList());
        caffeineCacheManager.setCaches(caches);
        caffeineCacheManager.initializeCaches();

        return new LoggingCacheManager(caffeineCacheManager, "Local-Caffeine-Cache");

    }

    @Bean
    @Primary
    @Override
    public CacheManager cacheManager() {
        return new ChainedCacheManager(caffeineCacheManager(), redisCacheManager());
    }
}
