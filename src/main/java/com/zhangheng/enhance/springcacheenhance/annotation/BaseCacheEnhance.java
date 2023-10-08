package com.zhangheng.enhance.springcacheenhance.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface BaseCacheEnhance {

    String ttl() default "";

    Class<?> cacheType() default Object.class;

}
