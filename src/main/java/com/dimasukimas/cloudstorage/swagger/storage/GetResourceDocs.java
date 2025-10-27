package com.dimasukimas.cloudstorage.swagger.storage;

import com.dimasukimas.cloudstorage.dto.ResourceInfoResponseDto;
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
@Operation(summary = "Get resource metadata"
        , description = "Return resource metadata such as path, name, size and type."
)
@WithPathParameter
@CommonResourcesErrors
@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Ok",
                content = @Content(mediaType = "application/json", contentSchema = @Schema(implementation = ResourceInfoResponseDto.class))
        ),
        @ApiResponse(responseCode = "404", description = "Resource not found",
                content = @Content(mediaType = "application/json", contentSchema = @Schema(implementation = ErrorResponse.class))
        )
}
)
public @interface GetResourceDocs {

}
