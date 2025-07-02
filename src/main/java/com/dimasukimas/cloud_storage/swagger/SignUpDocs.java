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
@Operation(summary = "Sign Up user and create session")
@io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "User registration data",
        required = true,
        content = @Content(schema = @Schema(implementation = AuthRequestDto.class))
)
@ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User successfully registered",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsernameDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "Registration failed due to validation error",
                content = @Content(mediaType = "application/json", contentSchema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(responseCode = "409", description = "Username is already exists",
                content = @Content(mediaType = "application/json", contentSchema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(responseCode = "500", description = "Internal server error",
                content = @Content(mediaType = "application/json", contentSchema = @Schema(implementation = ErrorResponse.class))
        )
})
public @interface SignUpDocs {
}
