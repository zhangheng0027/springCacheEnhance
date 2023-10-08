package com.zhangheng.enhance.springcacheenhance.annotation;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@CacheConfig
@BaseCacheEnhance
@Inherited
public @interface BaseCacheConfig {

    @AliasFor(annotation = BaseCacheEnhance.class)
    String ttl() default "";


    /**
     * Names of the default caches to consider for caching operations defined
     * in the annotated class.
     * <p>If none is set at the operation level, these are used instead of the default.
     * <p>May be used to determine the target cache (or caches), matching the
     * qualifier value or the bean names of a specific bean definition.
     */
    @AliasFor(annotation = CacheConfig.class)
    String[] cacheNames() default {};

    /**
     * The bean name of the default {@link org.springframework.cache.interceptor.KeyGenerator} to
     * use for the class.
     * <p>If none is set at the operation level, this one is used instead of the default.
     * <p>The key generator is mutually exclusive with the use of a custom key. When such key is
     * defined for the operation, the value of this key generator is ignored.
     */
    @AliasFor(annotation = CacheConfig.class)
    String keyGenerator() default "";

    /**
     * The bean name of the custom {@link org.springframework.cache.CacheManager} to use to
     * create a default {@link org.springframework.cache.interceptor.CacheResolver} if none
     * is set already.
     * <p>If no resolver and no cache manager are set at the operation level, and no cache
     * resolver is set via {@link #cacheResolver}, this one is used instead of the default.
     * @see org.springframework.cache.interceptor.SimpleCacheResolver
     */
    @AliasFor(annotation = CacheConfig.class)
    String cacheManager() default "";

    /**
     * The bean name of the custom {@link org.springframework.cache.interceptor.CacheResolver} to use.
     * <p>If no resolver and no cache manager are set at the operation level, this one is used
     * instead of the default.
     */
    @AliasFor(annotation = CacheConfig.class)
    String cacheResolver() default "customCacheResolver";

}
