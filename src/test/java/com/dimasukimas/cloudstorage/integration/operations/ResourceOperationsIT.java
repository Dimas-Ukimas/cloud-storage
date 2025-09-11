package com.dimasukimas.cloudstorage.integration.operations;

import com.dimasukimas.cloudstorage.annotation.IntegrationTest;
import com.dimasukimas.cloudstorage.config.container.MinioContainerInitializer;
import com.dimasukimas.cloudstorage.config.container.PostgresContainerInitializer;
import com.dimasukimas.cloudstorage.config.container.RedisContainerInitializer;
import com.dimasukimas.cloudstorage.config.security.SecurityTestConfig;
import com.dimasukimas.cloudstorage.dto.ResourceInfoDto;
import com.dimasukimas.cloudstorage.helper.MinioTestHelper;
import com.dimasukimas.cloudstorage.helper.RequestTestHelper;
import com.dimasukimas.cloudstorage.helper.UserTestDataHelper;
import com.dimasukimas.cloudstorage.util.assertion.HttpAssert;
import com.dimasukimas.cloudstorage.util.assertion.MinioAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

@IntegrationTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "app.security.enabled=false")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import(SecurityTestConfig.class)
@ContextConfiguration(initializers = {
        PostgresContainerInitializer.class,
        RedisContainerInitializer.class,
        MinioContainerInitializer.class
})
public class ResourceOperationsIT {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private RequestTestHelper requestHelper;

    @Autowired
    private MinioTestHelper minioHelper;

    @Autowired
    private UserTestDataHelper userTestDataHelper;

    private static final String USERNAME = "testUser";
    private static final String PASSWORD = "secret";
    private static String userRootDirectory;

    @BeforeEach
    void setUp() {
        userTestDataHelper.clearRepository();
        minioHelper.clearBucket();
        long userId = userTestDataHelper.createUser(USERNAME, PASSWORD);
        userRootDirectory = minioHelper.createUserRootDirectory(userId);
    }

    @Test
    void givenValidPath_whenCreateDirectory_thenCreationSuccessful() {
        ResponseEntity<ResourceInfoDto> response = testRestTemplate.postForEntity(
                "/directory?path=folder1/",
                null,
                ResourceInfoDto.class
        );

        HttpAssert.create(response)
                .assertJsonContentType()
                .assertStatus(HttpStatus.CREATED)
                .assertBodyContainsResourceName("folder1/");

        MinioAssert.create(minioHelper)
                .assertDirectoryExist(userRootDirectory + "folder1/");
    }

    @Test
    void givenValidPath_whenGetEmptyDirectoryContentInfo_thenReturnInfo() {
        testRestTemplate.postForEntity(
                "/directory?path=folder1/",
                null,
                ResourceInfoDto.class
        );

        ResponseEntity<List<ResourceInfoDto>> response = testRestTemplate.exchange(
                "/directory?path=folder1/",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<ResourceInfoDto>>() {
                }
        );

        HttpAssert.create(response)
                .assertJsonContentType()
                .assertStatus(HttpStatus.OK)
                .assertEmptyBody();
    }


}
