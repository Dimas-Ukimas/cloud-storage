package com.dimasukimas.cloudstorage.model.storage;

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
