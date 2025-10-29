package com.dimasukimas.cloudstorage.integration.operations;

import com.dimasukimas.cloudstorage.exception.handler.ErrorResponse;
import com.dimasukimas.cloudstorage.util.TestUtils;
import com.dimasukimas.cloudstorage.util.assertion.HttpAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GET resource/download")
class DownloadResourceIT extends BaseResourceOperationsIT {

    @Test
    @DisplayName("200 when download file")
    void givenExistentFilePath_whenDownload_thenReturnOk() throws Exception {
        HttpEntity<MultiValueMap<String, Object>> request = TestUtils.createRequestWithTestFile(FILE_NAME, "content");
        testRestTemplate.exchange("/resource?path=folder1/folder2/", HttpMethod.POST, request, new ParameterizedTypeReference<>() {
        });

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

    @Test
    @DisplayName("200 when download directory with proper zip archive structure")
    void givenExistentDirectoryPath_whenDownload_thenReturnOk() throws Exception {
        HttpEntity<MultiValueMap<String, Object>> request = TestUtils.createRequestWithTestFile(FILE_NAME, "content");
        byte[] expectedFileBytes = "content".getBytes();
        testRestTemplate.exchange("/resource?path=folder1/folder2/", HttpMethod.POST, request, new ParameterizedTypeReference<>() {
        });

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

    @Test
    @DisplayName("404 when download non-existent file")
    void givenNonExistentFilePath_whenDownload_thenReturnNotFound() throws Exception {
        ResponseEntity<ErrorResponse> response = testRestTemplate.getForEntity("/resource/download?path=folder42/", ErrorResponse.class);

        HttpAssert.create(response)
                .assertStatus(HttpStatus.NOT_FOUND)
                .assertBodyContainsMessage("Resource does not exists");
    }
}
