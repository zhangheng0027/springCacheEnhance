package com.zhangheng.enhance.springcacheenhance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhangheng.enhance.springcacheenhance.annotation.BaseCacheConfig;
import com.zhangheng.enhance.springcacheenhance.annotation.BaseCacheable;
import com.zhangheng.enhance.springcacheenhance.annotation.ParamCachePut;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest
class SpringCacheEnhanceApplicationTests {

    @Test
    void contextLoads() {
    }


    @Resource
    conf.TESTAA ta;

    @Test
    public void test01() {

        System.out.println(ta.abcd());
        System.out.println(ta.cut(10));
        System.out.println(ta.abcd());

    }

    @TestConfiguration
    public static class conf {
        @Bean
        ObjectMapper getObjectMapper() {
            return new ObjectMapper();
        }

        @TestComponent
        @BaseCacheConfig(cacheNames = "testAA", ttl = "20s")
        public static class TESTAA {

            AtomicInteger ai = new AtomicInteger(0);

            @BaseCacheable(key = "'abcd'")
            public int abcd() {
                return ai.getAndIncrement();
            }


            @ParamCachePut(key = "'abcd'", result = "#a",
                    skipMethod = true)
            public int cut(int a) {
                return ai.get();
            }

        }

    }


}
