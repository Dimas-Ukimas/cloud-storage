package com.dimasukimas.cloudstorage.exception.handler;

import io.swagger.v3.oas.annotations.media.Schema;

public record ErrorResponse(

        @Schema(description = "Error message", example = "Resource does not exist")
        String message
) {
}
