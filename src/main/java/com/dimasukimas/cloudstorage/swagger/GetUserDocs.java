package com.dimasukimas.cloudstorage.swagger;

import com.dimasukimas.cloudstorage.dto.UsernameDto;
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
@Operation(summary = "Get username of authorized user")
@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Username successfully retrieved",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsernameDto.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unable to get unauthorized user",
                content = @Content(mediaType = "application/json", contentSchema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(responseCode = "500", description = "Internal server error",
                content = @Content(mediaType = "application/json", contentSchema = @Schema(implementation = ErrorResponse.class))
        )
})
public @interface GetUserDocs {
}
