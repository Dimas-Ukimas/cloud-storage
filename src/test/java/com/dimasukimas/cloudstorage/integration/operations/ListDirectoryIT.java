package com.dimasukimas.cloudstorage.integration.operations;

import com.dimasukimas.cloudstorage.dto.ResourceInfoResponseDto;
import com.dimasukimas.cloudstorage.exception.handler.ErrorResponse;
import com.dimasukimas.cloudstorage.util.assertion.HttpAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

@DisplayName("GET /directory")
public class ListDirectoryIT extends BaseResourceOperationsIT {

    @BeforeEach
    void setUp() {
        minioHelper.createDirectory(userRootDirectory + "folder42/");
    }

    @Test
    @DisplayName("200 with empty directory content info when get empty directory")
    void givenExistentPath_whenGetEmptyDirectoryContentInfo_thenReturnInfo() {
        ResponseEntity<List<ResourceInfoResponseDto>> response = testRestTemplate
                .exchange(
                        "/directory?path=folder42/",
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<>() {
                        }
                );

        System.out.println("BODY: " + response.getBody());


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
