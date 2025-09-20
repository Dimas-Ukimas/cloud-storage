package com.dimasukimas.cloudstorage.dto;

import com.dimasukimas.cloudstorage.service.ResourceType;
import com.fasterxml.jackson.annotation.JsonInclude;

public record ResourceInfoDto(
        String path,

        String name,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        Long size,

        ResourceType type) {
}
