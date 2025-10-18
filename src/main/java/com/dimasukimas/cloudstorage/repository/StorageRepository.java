package com.dimasukimas.cloudstorage.repository;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public interface StorageRepository {
    ResourceMetadata createDirectory(String path);

    boolean isResourceExists(String path);

    List<ResourceMetadata> getDirectoryContentInfo(String path);

    Optional<ResourceMetadata> findResource(String path);

    List<ResourceMetadata> listAll(String path);

    ResourceMetadata upload(String path, InputStream inputStream, long size);

    void deleteDirectory(String path);

    void deleteFile(String path);

    Supplier<InputStream> download(String path);

}
