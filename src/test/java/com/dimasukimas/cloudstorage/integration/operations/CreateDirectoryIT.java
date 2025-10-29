package com.dimasukimas.cloudstorage.integration.operations;

import com.dimasukimas.cloudstorage.dto.ResourceInfoResponseDto;
import com.dimasukimas.cloudstorage.exception.handler.ErrorResponse;
import com.dimasukimas.cloudstorage.util.assertion.HttpAssert;
import com.dimasukimas.cloudstorage.util.assertion.MinioAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@DisplayName("POST /directory")
public class CreateDirectoryIT extends BaseResourceOperationsIT {

    @Test
    @DisplayName("201 when create a new directory with valid path")
    void givenValidPath_whenCreateDirectory_thenCreationSuccessful() {
        ResponseEntity<ResourceInfoResponseDto> response = testRestTemplate.postForEntity(
                "/directory?path=folder1/",
                null,
                ResourceInfoResponseDto.class
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
                ResourceInfoResponseDto.class
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
