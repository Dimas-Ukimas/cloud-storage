package com.dimasukimas.cloudstorage.unit.validation;

import com.dimasukimas.cloudstorage.config.StorageProperties;
import com.dimasukimas.cloudstorage.validation.PathValidator;
import com.dimasukimas.cloudstorage.validation.SafePath;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class PathValidatorTest {

    private PathValidator validator;

    @BeforeEach
    void setUp() {
        StorageProperties props = new StorageProperties();
        props.setMaxPathLength(100);
        props.setDirectorySplitter("/");

        validator = new PathValidator(props);
        validator.initialize(mock(SafePath.class));
    }

    static Stream<Arguments> invalidPaths() {
        return Stream.of(
                Arguments.of("/file.txt"),
                Arguments.of("/folder/sub/file.txt"),
                Arguments.of("folder//file.txt"),
                Arguments.of("/"),
                Arguments.of("."),
                Arguments.of(".."),
                Arguments.of("folder/.."),
                Arguments.of("./file.txt"),
                Arguments.of("file|name.txt"),
                Arguments.of("file:name.txt"),
                Arguments.of("file?name.txt"),
                Arguments.of("file*name.txt"),
                Arguments.of("file\"name.txt"),
                Arguments.of("folder\\file.txt"),
                Arguments.of(" file.txt"),
                Arguments.of("file.txt "),
                Arguments.of("folder/ bad.txt"),
                Arguments.of("folder//sub/file.txt"),
                Arguments.of("a".repeat(200))
        );
    }


    static Stream<Arguments> validPaths() {
        return Stream.of(
                Arguments.of((Object) null),
                Arguments.of(""),
                Arguments.of("file.txt"),
                Arguments.of("my_file-01.txt"),
                Arguments.of("Отчёт 2025.pdf"),
                Arguments.of("данные.csv"),
                Arguments.of("image 1.png"),
                Arguments.of("файл_с_пробелом.txt"),
                Arguments.of("docs/report.txt"),
                Arguments.of("папка/вложенная/файл.doc"),
                Arguments.of("data/2025/январь.xlsx"),
                Arguments.of("ver1.0/readme.txt"),
                Arguments.of("images/vacation-2024/photo 01.jpg")
        );
    }


    @ParameterizedTest
    @MethodSource("validPaths")
    void validPaths_shouldGoNext(String path) {
        assertThat(validator.isValid(path, mock(ConstraintValidatorContext.class))).isTrue();
    }

    @ParameterizedTest
    @MethodSource("invalidPaths")
    void shouldRejectInvalidPaths(String path) {
        assertThat(validator.isValid(path, mock(ConstraintValidatorContext.class))).isFalse();
    }


}
