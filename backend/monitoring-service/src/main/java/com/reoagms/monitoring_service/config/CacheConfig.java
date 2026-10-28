package com.reoagms.monitoring_service.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Configuration;

/** Enables caching and degrades to direct database reads when Redis is unavailable. */
@Slf4j
@Configuration
@EnableCaching
public class CacheConfig implements CachingConfigurer {
    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override public void handleCacheGetError(RuntimeException e, Cache cache, Object key) { log.warn("Cache get failed: {}", e.getMessage()); }
            @Override public void handleCachePutError(RuntimeException e, Cache cache, Object key, Object value) { log.warn("Cache put failed: {}", e.getMessage()); }
            @Override public void handleCacheEvictError(RuntimeException e, Cache cache, Object key) { log.warn("Cache evict failed: {}", e.getMessage()); }
            @Override public void handleCacheClearError(RuntimeException e, Cache cache) { log.warn("Cache clear failed: {}", e.getMessage()); }
        };
    }
}
