package com.dimasukimas.cloudstorage.swagger.storage;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;

import java.lang.annotation.*;

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Parameters({
        @Parameter(
                name = "path",
                in = ParameterIn.QUERY,
                description = "Path is the full path to the resource in url-encoded format. " +
                        "The folder path must end in /. " +
                        "This is necessary to distinguish between a folder and a file with the same name that can co-exist in the same root directory." +
                        "For example: 'folder1/folder2/' — folder, 'folder1/file.txt ' — file." +
                        "If not specified, the root is used.",
                example = "projects/java/",
                schema = @Schema(type = "string", pattern = "^[\\p{L}\\p{N}._ -]*$")
        )}
)
public @interface WithPathParameter {
}
