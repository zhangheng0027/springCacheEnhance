package com.zhangheng.enhance.springcacheenhance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhangheng.enhance.springcacheenhance.redis.RedisCacheEnhance;
import jakarta.annotation.Resource;
import lombok.Getter;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.SimpleCacheResolver;
import org.springframework.cache.support.NoOpCache;
import org.springframework.cache.transaction.AbstractTransactionSupportingCacheManager;
import org.springframework.cache.transaction.TransactionAwareCacheDecorator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Configuration
public class CacheConf {

    @Getter
    private ObjectMapper objectMapper;


    @Resource
    public void setObjectMapper(ObjectMapper om) {
        objectMapper = om.copy();
    }

    @Bean("customCacheResolver")
    public CacheResolver cacheResolver(CacheManager manager) {

        if (manager instanceof AbstractTransactionSupportingCacheManager) {
            // 开启事务
            ((AbstractTransactionSupportingCacheManager) manager).setTransactionAware(true);
        }

        return new SimpleCacheResolver(manager) {

            private final NoOpCache aDefault = new NoOpCache("___default");
            private  <T extends Cache> T getCache() {
                return (T) aDefault;
            }

            @Override
            public Collection<? extends Cache> resolveCaches(CacheOperationInvocationContext<?> context) {
                Collection<? extends Cache> caches = super.resolveCaches(context);
                if (caches.isEmpty()) {
                    caches.add(getCache());
                    return caches;
                }

                List<Cache> r = new ArrayList<>(caches.size() << 1);
                for (Cache cache : caches) {

                    if (cache instanceof TransactionAwareCacheDecorator ta) {
                        cache = ta.getTargetCache();
                    }

                    if (cache instanceof RedisCache rc) {
                        cache = new RedisCacheEnhance(rc, objectMapper);
                    }

                    if (manager instanceof AbstractTransactionSupportingCacheManager) {
                        if (((AbstractTransactionSupportingCacheManager) manager).isTransactionAware()) {
                            cache = new TransactionAwareCacheDecoratorIH(cache);
                        }
                    }

                    r.add(cache);
                }

                return r;
            }
        };
    }

}
