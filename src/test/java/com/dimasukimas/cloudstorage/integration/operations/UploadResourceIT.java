package com.dimasukimas.cloudstorage.integration.operations;

import com.dimasukimas.cloudstorage.dto.ResourceInfoResponseDto;
import com.dimasukimas.cloudstorage.exception.handler.ErrorResponse;
import com.dimasukimas.cloudstorage.service.ResourceType;
import com.dimasukimas.cloudstorage.util.TestUtils;
import com.dimasukimas.cloudstorage.util.assertion.HttpAssert;
import com.dimasukimas.cloudstorage.util.assertion.MinioAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

import java.util.List;

@Nested
@DisplayName("POST /resource")
class UploadResourceIT extends BaseResourceOperationsIT {

    @Test
    @DisplayName("201 when upload file")
    void givenValidFile_whenUpload_thenReturnCreated() throws Exception {
        HttpEntity<MultiValueMap<String, Object>> request = TestUtils.createRequestWithTestFile(FILE_NAME, "test");

        ResponseEntity<List<ResourceInfoResponseDto>> response = testRestTemplate.exchange(
                "/resource?path=folder1/",
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<>() {
                }
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
        testRestTemplate.exchange(
                "/resource?path=folder1/folder2/folder3/",
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<>() {
                }
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
        testRestTemplate.exchange("/resource?path=folder1/", HttpMethod.POST, request, new ParameterizedTypeReference<>() {
        });

        ResponseEntity<ErrorResponse> response = testRestTemplate.postForEntity(
                "/resource?path=folder1/",
                request,
                ErrorResponse.class
        );

        HttpAssert.create(response)
                .assertJsonContentType()
                .assertStatus(HttpStatus.CONFLICT)
                .assertBodyContainsMessage("Resources are already exist: [test.txt]");

        MinioAssert.create(minioHelper)
                .assertNoDuplicates(userRootDirectory + "folder1/" + FILE_NAME);
    }

}
