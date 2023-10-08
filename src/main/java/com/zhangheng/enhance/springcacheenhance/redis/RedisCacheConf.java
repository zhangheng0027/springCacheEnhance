package com.zhangheng.enhance.springcacheenhance.redis;

import com.zhangheng.enhance.springcacheenhance.annotation.BaseCacheConfig;
import com.zhangheng.enhance.springcacheenhance.CacheConf;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.Duration;

@Order
@Configuration
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
public class RedisCacheConf {

    @Resource
    RedisConnectionFactory redisConnectionFactory;


    @Bean
    public RedisCacheManager RedisCacheManager(CacheConf com) {

        RedisSerializationContext.SerializationPair objectSerializationPair =
                RedisSerializationContext.SerializationPair.<String>fromSerializer((RedisSerializer)new GenericJackson2JsonRedisSerializer(com.getObjectMapper()));


        RedisSerializationContext.SerializationPair<String> stringSerializationPair = RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer());

        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofDays(1)) // 设置默认缓存有效期一天
                .serializeValuesWith(objectSerializationPair)
                .serializeKeysWith(stringSerializationPair)
                ;

        RedisCacheManager.RedisCacheManagerBuilder builder = RedisCacheManager
                .builder(RedisCacheWriterIH.getProxy(RedisCacheWriter.nonLockingRedisCacheWriter(redisConnectionFactory)))
                .cacheDefaults(redisCacheConfiguration);


        // 扫描 com.kstopa 下的所有类，获取 BaseCacheConfig 注解，获取 cacheNames，设置到期时间
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            org.springframework.core.io.Resource[] resources = resolver.getResources("classpath*:com/kstopa/**/*.class");
            for (org.springframework.core.io.Resource resource : resources) {
                MergedAnnotations annotations = new SimpleMetadataReaderFactory().getMetadataReader(resource).getAnnotationMetadata().getAnnotations();
                if (!annotations.isPresent(BaseCacheConfig.class))
                    continue;

                MergedAnnotation<BaseCacheConfig> baseCacheConfigMergedAnnotation = annotations.get(BaseCacheConfig.class);

                for (String s : baseCacheConfigMergedAnnotation.getStringArray("cacheNames")) {
                    String ttl = baseCacheConfigMergedAnnotation.getString("ttl");
                    if (StringUtils.hasLength(ttl)) {
                        Duration d = DurationStyle.SIMPLE.parse(ttl);
                        builder.withCacheConfiguration(s, redisCacheConfiguration.entryTtl(d));
                    }
                }
            }
        } catch (IOException ignored) {
        }

        RedisCacheManager build = builder.build();

        build.setTransactionAware(true);
        return build;

    }

}
