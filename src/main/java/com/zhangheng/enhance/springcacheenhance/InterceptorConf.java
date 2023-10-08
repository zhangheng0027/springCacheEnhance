package com.zhangheng.enhance.springcacheenhance;

import com.zhangheng.enhance.springcacheenhance.annotation.BaseCacheConfig;
import com.zhangheng.enhance.springcacheenhance.annotation.BaseCacheEnhance;
import com.zhangheng.enhance.springcacheenhance.annotation.ParamCachePut;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Method;
import java.util.Set;

@Configuration
public class InterceptorConf {


    @Bean
    public DefaultPointcutAdvisor cacheEnhanceAdvisor1() {
        CacheConfigEnhanceInterceptor methodInterceptor = new CacheConfigEnhanceInterceptor();
        DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor();

        Pointcut p = new Pointcut() {
            @Override
            public ClassFilter getClassFilter() {
                return (clazz) -> AnnotatedElementUtils.hasAnnotation(clazz, BaseCacheConfig.class);
            }

            @Override
            public MethodMatcher getMethodMatcher() {
                return new MethodMatcher() {
                    @Override
                    public boolean matches(Method method, Class<?> targetClass) {
                        return !AnnotatedElementUtils.hasAnnotation(method, BaseCacheEnhance.class)
                                && AnnotatedElementUtils.hasAnnotation(method, Cacheable.class)
                                && AnnotatedElementUtils.hasAnnotation(method, CachePut.class);
                    }

                    @Override
                    public boolean isRuntime() {
                        return false;
                    }

                    @Override
                    public boolean matches(Method method, Class<?> targetClass, Object... args) {
                        return matches(method, targetClass);
                    }
                };
            }
        };

        advisor.setPointcut(p);
        advisor.setAdvice(methodInterceptor);
        return advisor;
    }

    /**
     * 方法上的 BaseCacheEnhance 注解
     */
    @Bean
    public DefaultPointcutAdvisor cacheEnhanceAdvisor() {
        CacheEnhanceInterceptor methodInterceptor = new CacheEnhanceInterceptor();
        DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor();

        Pointcut p = new Pointcut() {
            @Override
            public ClassFilter getClassFilter() {

                return (clazz) -> AnnotatedElementUtils.hasAnnotation(clazz, CacheConfig.class);
            }

            @Override
            public MethodMatcher getMethodMatcher() {
                return new MethodMatcher() {
                    @Override
                    public boolean matches(Method method, Class<?> targetClass) {
                        return AnnotatedElementUtils.hasAnnotation(method, BaseCacheEnhance.class);
                    }

                    @Override
                    public boolean isRuntime() {
                        return false;
                    }

                    @Override
                    public boolean matches(Method method, Class<?> targetClass, Object... args) {
                        return matches(method, targetClass);
                    }
                };
            }
        };

        advisor.setPointcut(p);
        advisor.setAdvice(methodInterceptor);
        return advisor;
    }


    @Bean
    public DefaultPointcutAdvisor paramCacheAdvisor(CacheResolver cacheResolver, ApplicationContext applicationContext) {
        ParamCacheInterceptor methodInterceptor = new ParamCacheInterceptor(cacheResolver, applicationContext);
        DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor();

        Pointcut p = new Pointcut() {
            @Override
            public ClassFilter getClassFilter() {
                return (clazz) -> AnnotatedElementUtils.hasAnnotation(clazz, BaseCacheConfig.class);
            }

            @Override
            public MethodMatcher getMethodMatcher() {
                return new MethodMatcher() {
                    @Override
                    public boolean matches(Method method, Class<?> targetClass) {
                        Set<ParamCachePut> mergedRepeatableAnnotations = AnnotatedElementUtils.findMergedRepeatableAnnotations(method, ParamCachePut.class);
                        return !mergedRepeatableAnnotations.isEmpty();
                    }

                    @Override
                    public boolean isRuntime() {
                        return false;
                    }

                    @Override
                    public boolean matches(Method method, Class<?> targetClass, Object... args) {
                        return matches(method, targetClass);
                    }
                };
            }
        };

        advisor.setPointcut(p);
        advisor.setAdvice(methodInterceptor);
        return advisor;
    }

}
