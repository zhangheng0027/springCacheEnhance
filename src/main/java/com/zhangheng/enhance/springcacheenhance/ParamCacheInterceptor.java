package com.zhangheng.enhance.springcacheenhance;

import com.zhangheng.enhance.springcacheenhance.annotation.ParamCachePut;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.interceptor.BasicOperation;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ParamCacheInterceptor implements MethodInterceptor {

    DefaultParameterNameDiscoverer defaultParameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    SpelExpressionParser spelExpressionParser = new SpelExpressionParser();

    ApplicationContext applicationContext;

    private static final Map<String, Expression> expressionMap = new ConcurrentHashMap<>();

    public Expression getExpression(String key) {
        return expressionMap.computeIfAbsent(key, k -> spelExpressionParser.parseExpression(k));
    }

    CacheResolver cacheResolver;

    public ParamCacheInterceptor(CacheResolver cacheResolver, ApplicationContext applicationContext) {
        this.cacheResolver = cacheResolver;
        this.applicationContext = applicationContext;
    }

    @Nullable
    @Override
    public Object invoke(@Nonnull MethodInvocation joinPoint) throws Throwable {

        Method method = joinPoint.getMethod();
        Set<ParamCachePut> paramCachePuts = AnnotatedElementUtils.findMergedRepeatableAnnotations(method, ParamCachePut.class);


        Collection<ParamCachePut> values = paramCachePuts.stream().collect(Collectors.toMap(ParamCachePut::index, v -> v, (v1, v2) -> {
            if (v1.order() > v2.order())
                return v1;
            return v2;
        })).values();

        ParamCachePut paramCachePut = values.stream().filter(ParamCachePut::skipMethod).findFirst().orElseGet(() -> null);

        CacheConfig cacheConfig = AnnotatedElementUtils.findMergedAnnotation(joinPoint.getThis().getClass(), CacheConfig.class);

        MethodBasedEvaluationContext context = new MethodBasedEvaluationContext(null,
                method,
                joinPoint.getArguments(),
                defaultParameterNameDiscoverer
        );

        Object proceed = null;
        if (null != paramCachePut) {
            String key1 = getKey(joinPoint, method, paramCachePut, context);

            // 从缓存获取
            Collection<? extends Cache> caches = cacheResolver.resolveCaches(new CacheOperationInvocationContextA(cacheConfig.cacheNames()));
            for (Cache cache : caches) {
                Cache.ValueWrapper valueWrapper = cache.get(key1);
                if (null != valueWrapper) {
                    proceed = valueWrapper.get();
                    break;
                }
            }
        }

        if (null == proceed) {
            proceed = joinPoint.proceed();
        }

        if (null == joinPoint.getThis())
            return proceed;


        for (ParamCachePut mergedAnnotation : values) {

            String[] cacheNames;

            if (StringUtils.isEmpty(mergedAnnotation.cacheNames())) {
                cacheNames = cacheConfig.cacheNames();
            } else {
                cacheNames = new String[]{
                        String.valueOf(getExpression(mergedAnnotation.cacheNames()).getValue(context))
                };
            }

            String key1 = getKey(joinPoint, method, mergedAnnotation, context);

            if (!mergedAnnotation.evict().isEmpty()) {

                Object r = null;
                if (!mergedAnnotation.result().isEmpty())
                    r = getExpression(mergedAnnotation.result()).getValue(context);

                context.setVariable("result", r);

                if (mergedAnnotation.evict().contains("#cache")) {
                    Object cache = null;
                    Collection<? extends Cache> caches = cacheResolver.resolveCaches(new CacheOperationInvocationContextA(cacheNames));
                    for (Cache c : caches) {
                        Cache.ValueWrapper valueWrapper = c.get(key1);
                        if (null != valueWrapper)
                            cache = valueWrapper.get();
                        break;
                    }
                    context.setVariable("cache", cache);
                }

                Boolean value1 = getExpression(mergedAnnotation.evict()).getValue(context, Boolean.class);
                if (Boolean.TRUE.equals(value1))
                    resolveCache(mergedAnnotation, cacheResolver ->
                            cacheResolver.resolveCaches(new CacheOperationInvocationContextA(cacheNames))
                                    .forEach(cache -> cache.evict(key1)));
                continue;
            }


            String value = mergedAnnotation.result();
            Object result;
            if (StringUtils.isEmpty(value)) {
                result = proceed;
            } else
                result = getExpression(value).getValue(context);

            if (null == result)
                continue;

            if (StringUtils.isEmpty(key1))
                continue;

            resolveCache(mergedAnnotation, cacheResolver ->
                    cacheResolver.resolveCaches(new CacheOperationInvocationContextA(cacheNames))
                            .forEach(cache -> cache.put(key1, result)));
        }

        return proceed;
    }

    public void resolveCache(ParamCachePut mergedAnnotation, Consumer<CacheResolver> resolver) {

        CacheEnhanceInterceptor.handleParam(mergedAnnotation);
        resolver.accept(cacheResolver);
        CacheEnhanceInterceptor.clear();

    }

    private String getKey(MethodInvocation joinPoint, Method method, ParamCachePut paramCachePut, MethodBasedEvaluationContext context) {
        String key = paramCachePut.key();
        String key1;
        if (!StringUtils.isEmpty(key)) {
            key1 = String.valueOf(getExpression(key).getValue(context));
        } else {
            String s = paramCachePut.keyGenerator();

            key1 = String.valueOf(applicationContext.getBean(s, KeyGenerator.class).generate(joinPoint.getThis(), method, joinPoint.getArguments()));
        }
        return key1;
    }

    class CacheOperationInvocationContextA implements CacheOperationInvocationContext {

        Set<String> cacheNames;
        CacheOperationInvocationContextA(String[] cacheNames) {
            this.cacheNames = new HashSet<>(Arrays.asList(cacheNames));
        }

        @Override
        public BasicOperation getOperation() {
            return () -> cacheNames;
        }

        @Override
        public Object getTarget() {
            return null;
        }

        @Override
        public Method getMethod() {
            return null;
        }

        @Override
        public Object[] getArgs() {
            return new Object[0];
        }
    }
}
