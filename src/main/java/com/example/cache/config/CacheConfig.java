package com.example.cache.config;

import com.example.cache.config.chaincache.ChainedCacheManager;
import com.example.cache.config.logcache.LoggingCacheManager;
import com.example.cache.config.redis.RedisService;
import com.example.cache.customer.domain.Customer;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
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
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import javax.inject.Inject;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j

@Configuration
public class CacheConfig extends CachingConfigurerSupport {

    @Value("${cache.redis.time-to-live}")
    private Long redisTtlSecond;

    @Inject
    private RedisProperties redisProperties;


    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        //sentinel 구성
        RedisSentinelConfiguration sentinelConfig = new RedisSentinelConfiguration()
                .master(redisProperties.getSentinel().getMaster());
        redisProperties.getSentinel().getNodes().forEach(s -> sentinelConfig.sentinel(s, Integer.valueOf(redisProperties.getPort())));
        sentinelConfig.setPassword(RedisPassword.of(redisProperties.getPassword()));

        //sentinelConfig.sentinel("127.0.0.1",  Integer.valueOf(26379));
        //sentinelConfig.setPassword(RedisPassword.of("password"));

        return new LettuceConnectionFactory(sentinelConfig);
    }

    @Bean
    MessageListenerAdapter messageListenerAdapter() {
        return new MessageListenerAdapter(new RedisService());
    }


    @Bean
    RedisMessageListenerContainer redisContainer() {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory());
        container.addMessageListener(messageListenerAdapter(), topic());
        return container;
    }

    @Bean
    RedisMessageListenerContainer keyExpirationListenerContainer(ExpirationListener expirationListener) {
        RedisMessageListenerContainer listenerContainer = new RedisMessageListenerContainer();
        listenerContainer.setConnectionFactory(redisConnectionFactory());
        listenerContainer.addMessageListener(expirationListener, new PatternTopic("__keyevent@*__:expired"));
        listenerContainer.setErrorHandler(e -> log.error("There was an error in redis key expiration listener container", e));
        return listenerContainer;
    }

    @Bean
    ChannelTopic topic() {
        return new ChannelTopic("customerIdCache");
    }


    @Bean
    public CacheManager redisCacheManager() {

        RedisCacheManager.RedisCacheManagerBuilder builder = RedisCacheManager.RedisCacheManagerBuilder.fromConnectionFactory(redisConnectionFactory());

        RedisCacheConfiguration redisCacheConfiguration =
                RedisCacheConfiguration.defaultCacheConfig()
                        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                        .entryTtl(Duration.ofSeconds(redisTtlSecond));
        builder.cacheDefaults(redisCacheConfiguration);

        return new LoggingCacheManager(builder.build(), "Global-Redis-Cache");

    }

    @Bean
    public CacheManager caffeineCacheManager() {
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
