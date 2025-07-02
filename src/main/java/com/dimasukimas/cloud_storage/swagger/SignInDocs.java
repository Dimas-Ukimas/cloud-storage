package com.dimasukimas.cloud_storage.swagger;

import com.dimasukimas.cloud_storage.dto.AuthRequestDto;
import com.dimasukimas.cloud_storage.dto.UsernameDto;
import com.dimasukimas.cloud_storage.exception.handler.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Operation(summary = "Sign In user and create session")
@io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "User authorization data",
        required = true,
        content = @Content(schema = @Schema(implementation = AuthRequestDto.class))
)
@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User successfully authorized",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsernameDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "Authorization failed due to validation error",
                content = @Content(mediaType = "application/json", contentSchema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Invalid data",
                content = @Content(mediaType = "application/json", contentSchema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(responseCode = "500", description = "Internal server error",
                content = @Content(mediaType = "application/json", contentSchema = @Schema(implementation = ErrorResponse.class))
        )
})
public @interface SignInDocs {
}
