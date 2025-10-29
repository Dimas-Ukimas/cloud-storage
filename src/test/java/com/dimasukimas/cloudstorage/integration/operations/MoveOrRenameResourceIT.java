package com.dimasukimas.cloudstorage.integration.operations;

import com.dimasukimas.cloudstorage.dto.ResourceInfoResponseDto;
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

@DisplayName("GET resource/move")
class MoveOrRenameResourceIT extends BaseResourceOperationsIT {

    @Test
    @DisplayName("200 when move resource")
    void givenExistentFilePath_whenMove_thenReturnOk() throws Exception {
        HttpEntity<MultiValueMap<String, Object>> request = TestUtils.createRequestWithTestFile(FILE_NAME, "content");
        testRestTemplate.exchange("/resource?path=folder1/folder2/", HttpMethod.POST, request, new ParameterizedTypeReference<>() {
        });

        String oldPath = "folder1/folder2/" + FILE_NAME;
        String newPath = "folder1/" + FILE_NAME;

        ResponseEntity<ResourceInfoResponseDto> response = testRestTemplate.getForEntity(
                "/resource/move?from=" + oldPath + "&to=" + newPath,
                ResourceInfoResponseDto.class);

        HttpAssert.create(response)
                .assertJsonContentType()
                .assertStatus(HttpStatus.OK)
                .assertNewPathIsCorrect("folder1/")
                .assertBodyContainsResourceName(FILE_NAME);

        MinioAssert.create(minioHelper)
                .assertResourceExist(userRootDirectory + newPath)
                .assertResourceNotExists(userRootDirectory + oldPath);
    }

    @Test
    @DisplayName("200 when rename directory via target path without end splitter")
    void givenExistentDirectoryPathWithoutEndSplitter_whenRename_thenReturnOk() throws Exception {
        HttpEntity<MultiValueMap<String, Object>> request = TestUtils.createRequestWithTestFile(FILE_NAME, "content");
        testRestTemplate.exchange("/resource?path=folder1/folder2/", HttpMethod.POST, request, new ParameterizedTypeReference<>() {
        });

        String oldPath = "folder1/folder2/";
        String newPathWithoutEndSplitter = "folder1/folder3";
        String correctNewPath = "folder1/folder3/";

        ResponseEntity<ResourceInfoResponseDto> response = testRestTemplate.getForEntity(
                "/resource/move?from=" + oldPath + "&to=" + newPathWithoutEndSplitter,
                ResourceInfoResponseDto.class);

        HttpAssert.create(response)
                .assertJsonContentType()
                .assertStatus(HttpStatus.OK)
                .assertBodyContainsResourceName("folder3/");

        MinioAssert.create(minioHelper)
                .assertResourceExist(userRootDirectory + correctNewPath)
                .assertResourceNotExists(userRootDirectory + oldPath);
    }

    @Test
    @DisplayName("200 when rename file via target path with end splitter")
    void givenExistentFilePathWithEndSplitter_whenRename_thenReturnOk() throws Exception {
        HttpEntity<MultiValueMap<String, Object>> request = TestUtils.createRequestWithTestFile(FILE_NAME, "content");
        testRestTemplate.exchange("/resource?path=folder1/folder2/", HttpMethod.POST, request, new ParameterizedTypeReference<>() {
        });

        String oldPath = "folder1/folder2/" + FILE_NAME;
        String newPathWithEndSplitter = "folder1/folder2/newTest.txt/";
        String correctNewPath = "folder1/folder2/newTest.txt";

        ResponseEntity<ResourceInfoResponseDto> response = testRestTemplate.getForEntity(
                "/resource/move?from=" + oldPath + "&to=" + newPathWithEndSplitter,
                ResourceInfoResponseDto.class);

        HttpAssert.create(response)
                .assertJsonContentType()
                .assertStatus(HttpStatus.OK)
                .assertBodyContainsResourceName("newTest.txt");

        MinioAssert.create(minioHelper)
                .assertResourceExist(userRootDirectory + correctNewPath)
                .assertResourceNotExists(userRootDirectory + oldPath);
    }


    @Test
    @DisplayName("404 when move non-existing resource")
    void givenNonExistentFilePath_whenMove_thenReturnNotFound() throws Exception {
        ResponseEntity<ErrorResponse> response = testRestTemplate.getForEntity(
                "/resource/move?from=folder42/&to=folder43/",
                ErrorResponse.class);

        HttpAssert.create(response)
                .assertStatus(HttpStatus.NOT_FOUND)
                .assertBodyContainsMessage("Resource does not exists");
    }

    @Test
    @DisplayName("409 when resource exist in target directory")
    void givenExistentFilePathInTargetDirectory_whenMove_thenReturnConflict() throws Exception {
        HttpEntity<MultiValueMap<String, Object>> request = TestUtils.createRequestWithTestFile(FILE_NAME, "content");
        testRestTemplate.exchange("/resource?path=folder1/folder2/", HttpMethod.POST, request, new ParameterizedTypeReference<>() {
        });
        testRestTemplate.exchange("/resource?path=folder1/", HttpMethod.POST, request, new ParameterizedTypeReference<>() {
        });

        String oldPath = "folder1/folder2/" + FILE_NAME;
        String newPath = "folder1/" + FILE_NAME;

        ResponseEntity<ErrorResponse> response = testRestTemplate.getForEntity(
                "/resource/move?from=" + oldPath + "&to=" + newPath,
                ErrorResponse.class);

        HttpAssert.create(response)
                .assertJsonContentType()
                .assertStatus(HttpStatus.CONFLICT)
                .assertBodyContainsMessage("Resource is already exists");

        MinioAssert.create(minioHelper)
                .assertResourceExist(userRootDirectory + oldPath);
    }
}
