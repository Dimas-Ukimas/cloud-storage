package com.dimasukimas.cloudstorage.integration.operations;

import com.dimasukimas.cloudstorage.exception.handler.ErrorResponse;
import com.dimasukimas.cloudstorage.util.TestUtils;
import com.dimasukimas.cloudstorage.util.assertion.HttpAssert;
import com.dimasukimas.cloudstorage.util.assertion.MinioAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

@DisplayName("DELETE /resource")
class DeleteResourceIT extends BaseResourceOperationsIT {

    @Test
    @DisplayName("204 when delete file")
    void givenExistentFilePath_whenDeleteResource_thenReturnNoContent() throws Exception {
        HttpEntity<MultiValueMap<String, Object>> request = TestUtils.createRequestWithTestFile(FILE_NAME, "content");
        testRestTemplate.exchange("/resource?path=folder1/", HttpMethod.POST, request, new ParameterizedTypeReference<>() {
        });

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
        testRestTemplate.exchange("/resource?path=folder1/", HttpMethod.POST, request, new ParameterizedTypeReference<>() {
        });

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
