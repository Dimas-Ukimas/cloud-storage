package com.dimasukimas.cloud_storage.helper;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class RedisTestHelper {

    private final StringRedisTemplate redisTemplate;

    public Set<String> getKeys() {
        return redisTemplate.keys("spring:session:sessions:*");
    }

    public void clear(){
        redisTemplate.delete("spring:session:sessions:*");
    }

}
