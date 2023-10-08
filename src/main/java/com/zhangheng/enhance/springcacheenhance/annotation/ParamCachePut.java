package com.zhangheng.enhance.springcacheenhance.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;


/**
 * 从参数中获取key 和 value 进行缓存
 * 推荐用于 insert 时更新缓存的最新编号
 * 必须配合 {@link BaseCacheConfig} 使用
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ParamCachePut.list.class)
@Inherited
@Documented
//@BaseCacheEnhance
public @interface ParamCachePut {


    /**
     * 支持 SpEL 表达式 只支持从参数取值
     * 不支持 #root
     * @return 缓存名称
     */
    String cacheNames() default "";
    /**
     * 支持 SpEL 表达式
     * 不支持 #root
     */
    String key() default "";

    String keyGenerator() default "";

    /**
     * 配合 order 使用，如果想替换掉父类的缓存，将其设置成相同的值，然后 order 设置成比父类大的
     * 如果想实现多个缓存，将其设置成不同的值
     */
    int index() default 0;

    /**
     * 配合 index 使用，如果想替换掉父类的缓存，将其设置成比父类大的值
     */
    int order() default 0;

    /**
     * 支持 SpEL 表达式
     * 不支持 #root
     */
    String result() default "";

//    @AliasFor(annotation= BaseCacheEnhance.class)
    String ttl() default "";

    /**
     * 缓存类型, 在一些情况下需要指定
     */
//    @AliasFor(annotation = BaseCacheEnhance.class)
    Class<?> cacheType() default Object.class;

    /**
     * 是否是驱逐操作
     * 支持 SpEL 表达式
     * #cache 表示已缓存值
     * #result 为 {@link ParamCachePut#result()} 的结果
     * eg. #cache == #result、true、false
     * 注意，如果使用 #cache 并且该方法的返回值类型与 cache 的类型不同，则需要配合 {@link ParamCachePut#cacheType()} 一起使用
     */
    String evict() default "";

    /**
     * 如果存在缓存是否跳过执行方法
     * @return true 等效于 {@link org.springframework.cache.annotation.Cacheable}
     */
    boolean skipMethod() default false;

    @Documented
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Inherited
    @interface list {
        ParamCachePut[] value();
    }
}
