package com.dimasukimas.cloudstorage.config.container;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.concurrent.atomic.AtomicBoolean;

public class RedisContainerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final AtomicBoolean STARTED = new AtomicBoolean(false);
    private static final GenericContainer<?> redis =
            new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
                    .withExposedPorts(6379);

    static {
        if (STARTED.compareAndSet(false, true)) {
            redis.start();
        }
    }

    @Override
    public void initialize(ConfigurableApplicationContext context) {

            TestPropertyValues.of(
                    "spring.data.redis.host=" + redis.getHost(),
                    "spring.data.redis.port=" + redis.getMappedPort(6379)
            ).applyTo(context);
    }
}
