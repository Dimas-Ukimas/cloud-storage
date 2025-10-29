package com.dimasukimas.cloudstorage.swagger.storage;

import com.dimasukimas.cloudstorage.dto.ResourceInfoResponseDto;
import com.dimasukimas.cloudstorage.exception.handler.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
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
@WithPathParameter
@CommonResourcesErrors
@Operation(
        summary = "Create empty directory",
        description = "Creates an empty directory at the specified path. " +
                "Parent directory must exist. Directory paths must end with '/'."
)
@ApiResponses({
        @ApiResponse(responseCode = "201", description = "Created",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ResourceInfoResponseDto.class))),
        @ApiResponse(responseCode = "404", description = "Parent directory not found",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Directory already exists",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class)))
})
public @interface CreateDirectoryDocs {
}
