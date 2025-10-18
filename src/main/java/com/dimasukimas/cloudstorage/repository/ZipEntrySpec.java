package com.dimasukimas.cloudstorage.repository;

import com.dimasukimas.cloudstorage.service.ResourceType;

import java.io.InputStream;
import java.util.function.Supplier;

public record ZipEntrySpec(

        String relativePath,

        Long size,

        ResourceType type,

        Supplier<InputStream> content
) {
}
