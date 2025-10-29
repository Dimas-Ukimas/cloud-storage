package com.dimasukimas.cloudstorage.unit.service;

import com.dimasukimas.cloudstorage.annotation.IntegrationTest;
import com.dimasukimas.cloudstorage.config.StorageProperties;
import com.dimasukimas.cloudstorage.service.PathService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
@ContextConfiguration(classes = {PathService.class, StorageProperties.class})
@EnableConfigurationProperties(StorageProperties.class)
public class PathServiceTest {

    @Autowired
    private PathService pathService;

    @Autowired
    private StorageProperties storageProperties;

    static Stream<Arguments> cases() {
        return Stream.of(
                Arguments.of("folder1/folder2/folder3/test.txt",
                        List.of("folder1/", "folder1/folder2/", "folder1/folder2/folder3/")),
                Arguments.of("folder1/folder2/folder3",
                        List.of("folder1/", "folder1/folder2/")),
                Arguments.of("test.txt",
                        List.of()));
    }

    static Stream<Arguments> pathCases() {
        return Stream.of(
                Arguments.of("user-1-files/folder1/folder2/folder3/test.txt",
                        "folder1/folder2/folder3/"),
                Arguments.of("folder1/folder2/folder3/",
                        "folder1/folder2/"),
                Arguments.of("test.txt",
                        ""));
    }

    static Stream<Arguments> relativePathCases() {
        return Stream.of(
                Arguments.of("user-1-files/folder1/folder2/folder3/test.txt", "user-1-files/folder1/",
                        "folder2/folder3/test.txt"),
                Arguments.of("folder1/folder2/folder3/", "folder1/folder2/",
                        "folder3/"),
                Arguments.of("folder1/test.txt", "folder1/",
                        "test.txt"));
    }

    @ParameterizedTest
    @MethodSource("cases")
    void extractSubdirectoriesFromResourcePath_shouldReturnAllSubdirectories(String path, List<String> expected) throws Exception {
        List<String> subdirectories = pathService.extractSubdirectories(path);

        assertThat(subdirectories).containsExactlyElementsOf(expected);
    }

    @ParameterizedTest
    @MethodSource("pathCases")
    void extractPathToResource_shouldReturnCorrectPathWithoutRoot(String path, String expected) throws Exception {
        String pathToResource = pathService.extractRootlessPathToResource(path);

        assertThat(pathToResource).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("relativePathCases")
    void extractRelativePathToResource_shouldReturnCorrectPath(String path, String rootPath, String expected) throws Exception {
        String relativePath = pathService.extractRelativePath(path, rootPath);

        assertThat(relativePath).isEqualTo(expected);
    }



}
