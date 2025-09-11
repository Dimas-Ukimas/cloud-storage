package com.dimasukimas.cloudstorage.config.container;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.concurrent.atomic.AtomicBoolean;

public class MinioContainerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final AtomicBoolean STARTED = new AtomicBoolean(false);
    private static final GenericContainer<?> minio =
            new GenericContainer<>(DockerImageName.parse("minio/minio:RELEASE.2025-07-23T15-54-02Z-cpuv1"))
                    .withEnv("MINIO_ROOT_USER", "minio")
                    .withEnv("MINIO_ROOT_PASSWORD", "minio123")
                    .withCommand("server", "/data")
                    .withExposedPorts(9000);

    static {
        if (STARTED.compareAndSet(false, true)) {
            minio.start();
        }
    }

    @Override
    public void initialize(ConfigurableApplicationContext context) {

            TestPropertyValues.of(
                    "spring.minio.url=http://" + minio.getHost() + ":" + minio.getMappedPort(9000),
                    "spring.minio.access-key=minio",
                    "spring.minio.secret-key=minio123"
            ).applyTo(context);
    }
}
