package com.dimasukimas.cloudstorage.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

public record ResourceInfoDto(
        String path,

        String name,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        Long size,

        String type) {
}
