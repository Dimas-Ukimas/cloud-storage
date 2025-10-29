package com.dimasukimas.cloudstorage.integration.operations;

import com.dimasukimas.cloudstorage.dto.ResourceInfoResponseDto;
import com.dimasukimas.cloudstorage.exception.handler.ErrorResponse;
import com.dimasukimas.cloudstorage.util.assertion.HttpAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@DisplayName("GET /resource")
class GetResourceIT extends BaseResourceOperationsIT {

    @BeforeEach
    void setUp() {
        minioHelper.createDirectory(userRootDirectory + "folder1/");
    }

    @Test
    @DisplayName("200 with resource info when get existent resource")
    void givenExistentResourcePath_whenGetResourceInfo_thenReturnOk() {
        ResponseEntity<ResourceInfoResponseDto> response = testRestTemplate.getForEntity(
                "/resource?path=folder1/",
                ResourceInfoResponseDto.class
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
