package com.zhangheng.enhance.springcacheenhance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class SpringCacheEnhanceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringCacheEnhanceApplication.class, args);
    }

}
