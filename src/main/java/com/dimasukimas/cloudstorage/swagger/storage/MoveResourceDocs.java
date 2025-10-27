package com.dimasukimas.cloudstorage.swagger.storage;

import com.dimasukimas.cloudstorage.dto.ResourceInfoResponseDto;
import com.dimasukimas.cloudstorage.exception.handler.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(METHOD)
@Retention(RUNTIME)
@Documented
@CommonResourcesErrors
@Operation(
        summary = "Move or rename resource",
        description = "Renames a resource when only the name changes; moves a resource when only the path changes. " +
                "Both parameters are full, URL-encoded paths."
)
@Parameters({
        @Parameter(
                name = "from", in = ParameterIn.QUERY, required = true,
                description = "Original full path to the resource, URL-encoded. " +
                        "Directory paths must end with '/'.",
                example = "folder1/old-name.txt",
                schema = @Schema(type = "string", pattern = "^[\\p{L}\\p{N}._/ -]*$")
        ),
        @Parameter(
                name = "to", in = ParameterIn.QUERY, required = true,
                description = "New full path for the resource, URL-encoded. " +
                        "Directory paths must end with '/'.",
                example = "folder1/new-name.txt",
                schema = @Schema(type = "string", pattern = "^[\\p{L}\\p{N}._/ -]*$")
        )
})
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ResourceInfoResponseDto.class))),
        @ApiResponse(responseCode = "404", description = "Resource not found",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Target resource already exists at 'to' path",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class)))
})
public @interface MoveResourceDocs {
}
