package com.dimasukimas.cloudstorage.dto;

import com.dimasukimas.cloudstorage.service.ResourceType;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

public record ResourceInfoResponseDto(

        @Schema(description = "Full path", example = "docs/report.pdf")
        String path,


        String name,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @Schema(description = "Size in bytes. For directories field is absent.", example = "10240", nullable = true)
        Long size,

        @Schema(description = "Resource type. Can be FILE or DIRECTORY.", example = "FILE")
        ResourceType type) {
}
