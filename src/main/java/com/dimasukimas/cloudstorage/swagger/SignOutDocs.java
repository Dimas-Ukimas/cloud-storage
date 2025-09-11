package com.dimasukimas.cloudstorage.swagger;

import com.dimasukimas.cloudstorage.exception.handler.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Operation(summary = "Sign Out user and delete session")
@ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "User successfully signed out")
        ,
        @ApiResponse(responseCode = "401", description = "Unable to sign-out unauthorized user",
                content = @Content(mediaType = "application/json", contentSchema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(responseCode = "500", description = "Internal server error",
                content = @Content(mediaType = "application/json", contentSchema = @Schema(implementation = ErrorResponse.class))
        )
})
public @interface SignOutDocs {
}
