package com.dimasukimas.cloudstorage.integration.operations;

import com.dimasukimas.cloudstorage.annotation.IntegrationTest;
import com.dimasukimas.cloudstorage.config.container.MinioContainerInitializer;
import com.dimasukimas.cloudstorage.config.container.PostgresContainerInitializer;
import com.dimasukimas.cloudstorage.config.container.RedisContainerInitializer;
import com.dimasukimas.cloudstorage.config.security.SecurityTestConfig;
import com.dimasukimas.cloudstorage.dto.ResourceInfoDto;
import com.dimasukimas.cloudstorage.exception.handler.ErrorResponse;
import com.dimasukimas.cloudstorage.helper.MinioTestHelper;
import com.dimasukimas.cloudstorage.helper.RequestTestHelper;
import com.dimasukimas.cloudstorage.helper.UserTestDataHelper;
import com.dimasukimas.cloudstorage.util.assertion.HttpAssert;
import com.dimasukimas.cloudstorage.util.assertion.MinioAssert;
import org.junit.jupiter.api.*;
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
    private String userRootDirectory;

    @BeforeEach
    void setUp() {
        userTestDataHelper.clearRepository();
        minioHelper.clearBucket();
        long userId = userTestDataHelper.createUser(USERNAME, PASSWORD);
        userRootDirectory = minioHelper.createUserRootDirectory(userId);
    }

    @Nested
    @DisplayName("POST /directory")
    class CreateDirectory {

        @Test
        @DisplayName("201 when create a new directory with valid path")
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
        @DisplayName("409 when creating an already existent directory")
        void givenValidPath_whenCreateAlreadyExistentDirectory_thenReturnConflict() {
            testRestTemplate.postForEntity(
                    "/directory?path=folder1/",
                    null,
                    ResourceInfoDto.class
            );

            ResponseEntity<ErrorResponse> response = testRestTemplate.postForEntity(
                    "/directory?path=folder1/",
                    null,
                    ErrorResponse.class
            );

            HttpAssert.create(response)
                    .assertJsonContentType()
                    .assertStatus(HttpStatus.CONFLICT)
                    .assertBodyContainsMessage("Resource is already exists");

            MinioAssert.create(minioHelper)
                    .assertDirectoryNotExists(userRootDirectory + "folder1/");
        }

        @Test
        @DisplayName("404 when parent directory does not exists")
        void givenNotExistentPath_whenCreateDirectory_thenReturnNotFound() {
            ResponseEntity<ErrorResponse> response = testRestTemplate.postForEntity(
                    "/directory?path=folder404/folder1/",
                    null,
                    ErrorResponse.class
            );

            HttpAssert.create(response)
                    .assertJsonContentType()
                    .assertStatus(HttpStatus.NOT_FOUND)
                    .assertBodyContainsMessage("Parent directory does not exists");

            MinioAssert.create(minioHelper)
                    .assertDirectoryNotExists(userRootDirectory + "folder404/folder1/");
        }


    }

    @Nested
    @DisplayName("GET /directory")
    class ListDirectory {

        @BeforeEach
        void setUp() {
            minioHelper.clearBucket();
            minioHelper.createDirectory(userRootDirectory + "folder1/");
        }

        @Test
        @DisplayName("200 with empty directory content info when get empty directory")
        void givenExistentPath_whenGetEmptyDirectoryContentInfo_thenReturnInfo() {
            ResponseEntity<List<ResourceInfoDto>> response = testRestTemplate.exchange(
                    "/directory?path=folder1/",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {
                    }
            );

            HttpAssert.create(response)
                    .assertJsonContentType()
                    .assertStatus(HttpStatus.OK)
                    .assertEmptyBody();
        }

        @Test
        @DisplayName("404 when get non-existent directory")
        void givenNotExistentPath_whenGetDirectoryContentInfo_thenNotFound() {
            ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
                    "/directory?path=folder404/",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {
                    }
            );

            HttpAssert.create(response)
                    .assertJsonContentType()
                    .assertStatus(HttpStatus.NOT_FOUND)
                    .assertBodyContainsMessage("Resource does not exists");
        }
    }

    @Nested
    @DisplayName("GET /resource")
    class GetResource {

        @BeforeEach
        void setUp() {
            minioHelper.clearBucket();
            minioHelper.createDirectory(userRootDirectory + "folder1/");
        }

        @Test
        @DisplayName("200 with resource info when get existent resource")
        void givenExistentResourcePath_whenGetResourceInfo_thenReturnOk() {
            ResponseEntity<ResourceInfoDto> response = testRestTemplate.getForEntity(
                    "/resource?path=folder1/",
                    ResourceInfoDto.class
            );

            HttpAssert.create(response)
                    .assertJsonContentType()
                    .assertStatus(HttpStatus.OK)
                    .assertBodyContainsResourceName("folder1/");
        }


        @Test
        @DisplayName("404 when get non-existent resource")
        void givenNotExistentResourcePath_whenGetResourceInfo_thenReturnNotFound() {
            ResponseEntity<ErrorResponse> response = testRestTemplate.getForEntity(
                    "/resource?path=folder404/",
                    ErrorResponse.class
            );

            HttpAssert.create(response)
                    .assertStatus(HttpStatus.NOT_FOUND)
                    .assertJsonContentType()
                    .assertBodyContainsMessage("Resource does not exists");
        }
    }
}
