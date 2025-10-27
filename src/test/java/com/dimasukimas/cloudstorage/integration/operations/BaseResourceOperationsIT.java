package com.dimasukimas.cloudstorage.integration.operations;

import com.dimasukimas.cloudstorage.annotation.IntegrationTest;
import com.dimasukimas.cloudstorage.config.container.MinioContainerInitializer;
import com.dimasukimas.cloudstorage.config.container.PostgresContainerInitializer;
import com.dimasukimas.cloudstorage.config.container.RedisContainerInitializer;
import com.dimasukimas.cloudstorage.config.security.SecurityTestConfig;
import com.dimasukimas.cloudstorage.helper.MinioTestHelper;
import com.dimasukimas.cloudstorage.helper.UserTestDataHelper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

@IntegrationTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "app.security.enabled=false")
@Import(SecurityTestConfig.class)
@ContextConfiguration(initializers = {
        PostgresContainerInitializer.class,
        RedisContainerInitializer.class,
        MinioContainerInitializer.class
})
public abstract class BaseResourceOperationsIT {

    @Autowired
    protected TestRestTemplate testRestTemplate;

    @Autowired
    protected MinioTestHelper minioHelper;

    @Autowired
    protected UserTestDataHelper userTestDataHelper;

    protected static final String FILE_NAME = "test.txt";

    protected String userRootDirectory;

    @BeforeEach
    void setUp() {
        userTestDataHelper.clearRepository();
        minioHelper.clearBucket();
        long userId = userTestDataHelper.createUser("testUser", "secret");
        userRootDirectory = minioHelper.createUserRootDirectory(userId);
    }
}



