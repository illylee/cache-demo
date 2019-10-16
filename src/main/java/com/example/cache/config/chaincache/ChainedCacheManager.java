package com.example.cache.config.chaincache;

import lombok.NonNull;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/*
 * spring에서는 CacheManager 인터페이스를 제공하여 캐시를 구현하도록 하고 있음.
 * Spring Boot에서는 spring-boot-starter-cache Artifact를 추가하여 CacheManager를 구성할 수 있다.
 * 기본적으로 별도의 추가적인 서드파티 모듈이 없는 경우에는 Local Memory에 저장이 가능한 ConcurrentMap기반인 ConcurrentMapCacheManager가 Bean으로 자동 생성 된다.
 * 이외에도 EHCache, Redis등의 서드파티 모듈을 추가 하게 되면 EHCacheCacheManager, RedisCacheManager를 Bean으로 등록 하여 사용할 수 있다.
 * 이렇게 되면 별도로 다른 설정 없이도 단순 Memory Cache가 아닌 Cache Server를 대상으로 캐시를 저장 할 수 있도록 지원하고 있다.
 */
public class ChainedCacheManager implements CacheManager {
    private final List<CacheManager> cacheManagers;
    private final Map<String, Cache> cacheMap = new ConcurrentHashMap<>();

    public ChainedCacheManager(@NonNull CacheManager... cacheManagers) {
        if (cacheManagers.length < 1) {
            throw new IllegalArgumentException();
        }
        this.cacheManagers = Collections.unmodifiableList(Arrays.asList(cacheManagers));
    }

    @Override
    public Cache getCache(String name) {
        //name 으로된 캐시가 없으면 chanedCache 만듬
        return cacheMap.computeIfAbsent(name, key -> new ChainedCache(getCaches(key)));
    }

    private List<Cache> getCaches(String name) {
        //cachemanager에 등록된 순서대로 cache 조회
        return cacheManagers.stream().map(manager -> manager.getCache(name)).collect(Collectors.toList());
    }

    @Override
    public Collection<String> getCacheNames() {
        return cacheManagers.stream()
                            .flatMap(manager -> manager.getCacheNames().stream())
                            .collect(Collectors.toSet());
    }
}
