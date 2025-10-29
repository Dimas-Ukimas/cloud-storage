package com.dimasukimas.cloudstorage.swagger.storage;

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
        summary = "Download resource",
        description = "Downloads the resource by path. Files are streamed as application/octet-stream. " +
                "Directories are streamed as a ZIP archive of their contents."
)
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK",
                content = @Content(mediaType = "application/octet-stream",
                        schema = @Schema(type = "string", format = "binary"))),
        @ApiResponse(responseCode = "404", description = "Resource not found",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class)))
})
public @interface DownloadResourceDocs {
}
