package com.zhangheng.enhance.springcacheenhance.redis;

import com.zhangheng.enhance.springcacheenhance.CacheEnhanceInterceptor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.cache.RedisCacheWriter;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.Duration;

@Log4j2
public class RedisCacheWriterIH implements InvocationHandler {

    private final RedisCacheWriter target;


    public RedisCacheWriterIH(RedisCacheWriter target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ((!"put".equals(method.getName()) && !"putIfAbsent".equals(method.getName())) || args.length < 4) {
            return method.invoke(target, args);
        }

        // get duration
        Duration duration = (Duration) args[3];
        if (duration != null) {
            if (null != CacheEnhanceInterceptor.duration.get()) {
                duration = CacheEnhanceInterceptor.duration.get();
                args[3] = changeDuration(duration);
            }
        }

        return method.invoke(target, args);
    }

    public Duration changeDuration(Duration duration) {
        long s = duration.getSeconds();
        if (s <= 0)
            return duration;

        // min and max  + - 10% random
        long min = (long) (s * 0.9);
        long max = (long) (s * 1.1);

        long random = (long) (Math.random() * (max - min) + min);

        return Duration.ofSeconds(random);
    }

    public static RedisCacheWriter getProxy(RedisCacheWriter target) {
        return (RedisCacheWriter) Proxy.newProxyInstance(target.getClass().getClassLoader(), target.getClass().getInterfaces(), new RedisCacheWriterIH(target));
    }

}
