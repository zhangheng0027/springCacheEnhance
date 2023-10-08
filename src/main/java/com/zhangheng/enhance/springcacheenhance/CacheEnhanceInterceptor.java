package com.zhangheng.enhance.springcacheenhance;

import com.zhangheng.enhance.springcacheenhance.annotation.BaseCacheConfig;
import com.zhangheng.enhance.springcacheenhance.annotation.BaseCacheEnhance;
import com.zhangheng.enhance.springcacheenhance.annotation.ParamCachePut;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Method;
import java.time.Duration;

public class CacheEnhanceInterceptor implements MethodInterceptor {

    public static final ThreadLocal<Duration> duration = new ThreadLocal<>();

    public static final ThreadLocal<Class<?>> returnType = new ThreadLocal<>();

    @Override
    public Object invoke(MethodInvocation joinPoint) throws Throwable {

        Method method = joinPoint.getMethod();
        BaseCacheEnhance mergedAnnotation = AnnotatedElementUtils.findMergedAnnotation(method, BaseCacheEnhance.class);

        String ttl = mergedAnnotation.ttl();
        if (!ttl.isEmpty()) {
            duration.set(DurationStyle.SIMPLE.parse(ttl));
        }

        Class<?> aClass = mergedAnnotation.cacheType();
        if (Object.class != aClass)
            returnType.set(aClass);

        Object result = joinPoint.proceed();

        clear();

        return result;
    }

    public static void clear() {
        returnType.remove();
        duration.remove();
    }

    static void handleParam(ParamCachePut paramCachePut) {
        if (!paramCachePut.ttl().isEmpty()) {
            duration.set(DurationStyle.SIMPLE.parse(paramCachePut.ttl()));
        }
        if (Object.class!=paramCachePut.cacheType()) {
            returnType.set(paramCachePut.cacheType());
        }
    }

    static void handleConfig(BaseCacheConfig conf) {
        if (!conf.ttl().isEmpty()) {
            duration.set(DurationStyle.SIMPLE.parse(conf.ttl()));
        }
    }
}
