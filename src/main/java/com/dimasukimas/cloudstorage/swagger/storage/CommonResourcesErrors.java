package com.dimasukimas.cloudstorage.swagger.storage;

import com.dimasukimas.cloudstorage.exception.handler.ErrorResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.*;

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "Path validation failed",
                content = @Content(mediaType = "application/json", contentSchema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unable to perform operation for unauthorized user",
                content = @Content(mediaType = "application/json", contentSchema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(responseCode = "500", description = "Internal server error",
                content = @Content(mediaType = "application/json", contentSchema = @Schema(implementation = ErrorResponse.class))
        )
}
)
public @interface CommonResourcesErrors {
}
