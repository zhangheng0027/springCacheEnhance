package com.zhangheng.enhance.springcacheenhance.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhangheng.enhance.springcacheenhance.CacheEnhanceInterceptor;
import org.springframework.cache.Cache;
import org.springframework.cache.support.NullValue;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.lang.Nullable;

import java.time.Duration;

public class RedisCacheEnhance extends RedisCache {

    ObjectMapper mapper;

    public RedisCacheEnhance(RedisCache rc, ObjectMapper mapper) {
        super(rc.getName(), rc.getNativeCache(), rc.getCacheConfiguration());
        this.mapper = mapper;
    }

    @Override
    protected Object lookup(Object key) {
        Object lookup = super.lookup(key);
        if (null == lookup || lookup instanceof NullValue)
            return null;
        Class<?> returnType = CacheEnhanceInterceptor.returnType.get();
        if (null == returnType || returnType.isAssignableFrom(lookup.getClass()))
            return lookup;


        return mapper.convertValue(lookup, returnType);
    }

    @Override
    public Cache.ValueWrapper putIfAbsent(Object key, @Nullable Object value) {

        Object cacheValue = preProcessCacheValue(value);

        if (!isAllowNullValues() && cacheValue == null) {
            return get(key);
        }
        Duration ttl = getCacheConfiguration().getTtl();
        if (null != CacheEnhanceInterceptor.duration.get()) {
            ttl = CacheEnhanceInterceptor.duration.get();
        }

        byte[] result = getNativeCache().putIfAbsent(getName(), createAndConvertCacheKey(key), serializeCacheValue(cacheValue),
                ttl);

        if (result == null) {
            return null;
        }

        return new SimpleValueWrapper(fromStoreValue(deserializeCacheValue(result)));
    }

    @Override
    public void put(Object key, @Nullable Object value) {

        Object cacheValue = preProcessCacheValue(value);

        if (!isAllowNullValues() && cacheValue == null) {

            throw new IllegalArgumentException(String.format(
                    "Cache '%s' does not allow 'null' values. Avoid storing null via '@Cacheable(unless=\"#result == null\")' or configure RedisCache to allow 'null' via RedisCacheConfiguration.",
                    getName()));
        }
        Duration ttl = getCacheConfiguration().getTtl();
        if (null != CacheEnhanceInterceptor.duration.get()) {
            ttl = CacheEnhanceInterceptor.duration.get();
        }
        getNativeCache().put(getName(), createAndConvertCacheKey(key), serializeCacheValue(cacheValue), ttl);
    }

    private byte[] createAndConvertCacheKey(Object key) {
        return serializeCacheKey(createCacheKey(key));
    }
}
