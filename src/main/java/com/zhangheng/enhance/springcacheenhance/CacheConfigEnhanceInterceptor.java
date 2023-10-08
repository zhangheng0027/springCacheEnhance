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

public class CacheConfigEnhanceInterceptor implements MethodInterceptor {


    @Override
    public Object invoke(MethodInvocation joinPoint) throws Throwable {

        Method method = joinPoint.getMethod();
        BaseCacheConfig mergedAnnotation = AnnotatedElementUtils.findMergedAnnotation(method, BaseCacheConfig.class);
        CacheEnhanceInterceptor.handleConfig(mergedAnnotation);

        Object result = joinPoint.proceed();

        CacheEnhanceInterceptor.clear();
        return result;
    }


}
