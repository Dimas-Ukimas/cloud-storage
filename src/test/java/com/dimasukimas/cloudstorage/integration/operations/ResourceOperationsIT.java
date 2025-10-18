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
import com.dimasukimas.cloudstorage.service.ResourceType;
import com.dimasukimas.cloudstorage.util.TestUtils;
import com.dimasukimas.cloudstorage.util.assertion.HttpAssert;
import com.dimasukimas.cloudstorage.util.assertion.MinioAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.MultiValueMap;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;

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

    private static final String FILE_NAME = "test.txt";

    private String userRootDirectory;

    @BeforeEach
    void setUp() {
        userTestDataHelper.clearRepository();
        minioHelper.clearBucket();
        long userId = userTestDataHelper.createUser("testUser", "secret");
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
                    .assertResourceExist(userRootDirectory + "folder1/");
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
                    .assertNoDuplicates(userRootDirectory + "folder1/");
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
                    .assertResourceNotExists(userRootDirectory + "folder404/folder1/");
        }
    }

    @Nested
    @DisplayName("GET /directory")
    class ListDirectory {

        @BeforeEach
        void setUp() {
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
                    .assertEmptyCollectionBody();
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

    @Nested
    @DisplayName("POST /resource")
    class UploadResource {

        @Test
        @DisplayName("201 when upload file")
        void givenValidFile_whenUpload_thenReturnCreated() throws Exception {
            HttpEntity<MultiValueMap<String, Object>> request = TestUtils.createRequestWithTestFile(FILE_NAME, "test");

            ResponseEntity<ResourceInfoDto> response = testRestTemplate.postForEntity(
                    "/resource?path=folder1/" + FILE_NAME,
                    request,
                    ResourceInfoDto.class
            );

            HttpAssert.create(response)
                    .assertJsonContentType()
                    .assertStatus(HttpStatus.CREATED)
                    .assertBodyContainsResourceName(FILE_NAME)
                    .assertResourceType(ResourceType.FILE);

            MinioAssert.create(minioHelper)
                    .assertResourceExist(userRootDirectory + "folder1/" + FILE_NAME);
        }

        @Test
        @DisplayName("all subdirectories created when upload file to non-existent path")
        void givenValidFilePath_whenUpload_thenCreateSubdirectories() throws Exception {
            HttpEntity<MultiValueMap<String, Object>> request = TestUtils.createRequestWithTestFile(FILE_NAME, "test");

            ResponseEntity<ResourceInfoDto> response = testRestTemplate.postForEntity(
                    "/resource?path=folder1/folder2/folder3/",
                    request,
                    ResourceInfoDto.class
            );

            MinioAssert.create(minioHelper)
                    .assertResourceExist(userRootDirectory + "folder1/")
                    .assertResourceExist(userRootDirectory + "folder1/folder2/")
                    .assertResourceExist(userRootDirectory + "folder1/folder2/folder3/");
        }

        @Test
        @DisplayName("409 when upload already existent file")
        void givenExistentFilePath_whenUpload_thenReturnConflict() throws Exception {
            HttpEntity<MultiValueMap<String, Object>> request = TestUtils.createRequestWithTestFile(FILE_NAME, "test");
            testRestTemplate.postForEntity("/resource?path=folder1/", request, ResourceInfoDto.class);

            ResponseEntity<ErrorResponse> response = testRestTemplate.postForEntity(
                    "/resource?path=folder1/",
                    request,
                    ErrorResponse.class
            );

            HttpAssert.create(response)
                    .assertJsonContentType()
                    .assertStatus(HttpStatus.CONFLICT)
                    .assertBodyContainsMessage("Resource is already exists");

            MinioAssert.create(minioHelper)
                    .assertNoDuplicates(userRootDirectory + "folder1/" + FILE_NAME);
        }

        //TODO написать тест
        @Test
        @DisplayName("400 when upload file with invalid body")
        void givenInvalidBody_whenUpload_thenReturnBadRequest() throws Exception {

        }


        @Nested
        @DisplayName("DELETE /resource")
        class DeleteResource {

            @Test
            @DisplayName("204 when delete file")
            void givenExistentFilePath_whenDeleteResource_thenReturnNoContent() throws Exception {
                HttpEntity<MultiValueMap<String, Object>> request = TestUtils.createRequestWithTestFile(FILE_NAME, "content");
                testRestTemplate.postForEntity("/resource?path=folder1/", request, ResourceInfoDto.class);

                ResponseEntity<Void> response = testRestTemplate.exchange("/resource?path=folder1/" + FILE_NAME,
                        HttpMethod.DELETE,
                        null,
                        Void.class
                );

                HttpAssert.create(response)
                        .assertStatus(HttpStatus.NO_CONTENT)
                        .assertBodyIsNull();

                MinioAssert.create(minioHelper)
                        .assertResourceNotExists(userRootDirectory + "folder1/" + FILE_NAME);
            }

            @Test
            @DisplayName("204 with recursive delete when delete directory")
            void givenExistentDirectoryPath_whenDelete_thenReturnNoContentAndDeleteAllContent() throws Exception {
                HttpEntity<MultiValueMap<String, Object>> request = TestUtils.createRequestWithTestFile(FILE_NAME, "content");
                testRestTemplate.postForEntity("/resource?path=folder1/", request, ResourceInfoDto.class);

                ResponseEntity<Void> response = testRestTemplate.exchange("/resource?path=folder1/",
                        HttpMethod.DELETE,
                        null,
                        Void.class
                );

                HttpAssert.create(response)
                        .assertStatus(HttpStatus.NO_CONTENT)
                        .assertBodyIsNull();

                MinioAssert.create(minioHelper)
                        .assertResourceNotExists(userRootDirectory + "folder1/")
                        .assertResourceNotExists(userRootDirectory + "folder1/" + FILE_NAME);
            }


            @Test
            @DisplayName("404 when delete non-existent resource")
            void givenNotExistentResourcePath_whenDelete_thenReturnNotFound() throws Exception {
                ResponseEntity<ErrorResponse> response = testRestTemplate.exchange("/resource?path=folder1/",
                        HttpMethod.DELETE,
                        null,
                        ErrorResponse.class
                );

                HttpAssert.create(response)
                        .assertStatus(HttpStatus.NOT_FOUND)
                        .assertBodyContainsMessage("Resource does not exists");
            }
        }

        @Nested
        @DisplayName("GET resource/download")
        class DownloadResource {

            @Test
            @DisplayName("200 when download file")
            void givenExistentFilePath_whenDownload_thenReturnOk() throws Exception {
                HttpEntity<MultiValueMap<String, Object>> request = TestUtils.createRequestWithTestFile(FILE_NAME, "content");
                testRestTemplate.postForEntity("/resource?path=folder1/folder2/", request, ResourceInfoDto.class);

                ResponseEntity<byte[]> response = testRestTemplate.exchange("/resource/download?path=folder1/folder2/" + FILE_NAME,
                        HttpMethod.GET,
                        null,
                        byte[].class
                );

                HttpAssert.create(response)
                        .assertStatus(HttpStatus.OK)
                        .assertHeaderExist(HttpHeaders.CONTENT_DISPOSITION)
                        .assertHeaderExist(HttpHeaders.CONTENT_LENGTH);

                assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                        .contains("filename=\"" + FILE_NAME + "\"");
                assertThat(response.getBody()).isEqualTo("content".getBytes());
            }

            //TODO: проверить почему относительный путь формируется коряво
            @Test
            @DisplayName("200 when download directory with proper zip archive structure")
            void givenExistentDirectoryPath_whenDownload_thenReturnOk() throws Exception {
                HttpEntity<MultiValueMap<String, Object>> request = TestUtils.createRequestWithTestFile(FILE_NAME, "content");
                byte[] expectedFileBytes = "content".getBytes();
                testRestTemplate.postForEntity("/resource?path=folder1/folder2/", request, ResourceInfoDto.class);

                ResponseEntity<byte[]> response = testRestTemplate.exchange("/resource/download?path=folder1/",
                        HttpMethod.GET,
                        null,
                        byte[].class
                );

                HttpAssert.create(response)
                        .assertStatus(HttpStatus.OK)
                        .assertHeaderExist(HttpHeaders.CONTENT_DISPOSITION)
                        .assertHeaderNotExist(HttpHeaders.CONTENT_LENGTH);

                assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                        .contains("filename=\"folder1.zip\"");

                byte[] zipBytes = response.getBody();
                assertThat(zipBytes).isNotEmpty();

                List<String> names = new ArrayList<>();
                Map<String, byte[]> files = new HashMap<>();

                try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
                    ZipEntry zipEntry;

                    while ((zipEntry = zip.getNextEntry()) != null) {
                        String name = zipEntry.getName();
                        names.add(name);

                        assertThat(name).doesNotStartWith("/");

                        if (!zipEntry.isDirectory()) {
                            files.put(name, zip.readAllBytes());
                        }
                        zip.closeEntry();
                    }
                }
                assertThat(names).contains("folder1/");
                assertThat(names).contains("folder1/folder2/");
                assertThat(names).contains("folder1/folder2/" + FILE_NAME);
                assertThat(files.get("folder1/folder2/" + FILE_NAME)).isEqualTo(expectedFileBytes);
            }
        }
    }


}

