package com.dimasukimas.cloud_storage.util.assertion;

import com.dimasukimas.cloud_storage.helper.RedisTestHelper;

import static org.assertj.core.api.Assertions.assertThat;

public class RedisAssert {

    private final RedisTestHelper redisHelper;

    private RedisAssert(RedisTestHelper redisHelper) {
        this.redisHelper = redisHelper;
    }

    public static RedisAssert create(RedisTestHelper redisHelper) {
        return new RedisAssert(redisHelper);
    }

    public RedisAssert assertRedisSessionCreated() {
        assertThat(redisHelper.getKeys()).isNotEmpty();
        return this;
    }

    public RedisAssert assertRedisSessionNotCreated() {
        assertThat(redisHelper.getKeys()).isEmpty();
        return this;
    }
}
