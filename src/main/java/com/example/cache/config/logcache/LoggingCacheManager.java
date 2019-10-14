package com.example.cache.config.logcache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class LoggingCacheManager implements CacheManager {

    private final Map<String, Cache> cacheMap = new ConcurrentHashMap<>();
    private final CacheManager delegate;
    private final String managerName;

    public LoggingCacheManager(CacheManager delegate, String managerName) {
        this.delegate = delegate;
        this.managerName = managerName;
        log.info("delegate: {}", delegate);
        log.info("managerName: {}", managerName);
    }

    @Override
    public Cache getCache(String name) {
        return cacheMap.computeIfAbsent(name, key -> new LoggingCache(delegate.getCache(key), managerName));
    }

    @Override
    public Collection<String> getCacheNames() {
        return delegate.getCacheNames();
    }
}
