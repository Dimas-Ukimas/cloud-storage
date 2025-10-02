package com.dimasukimas.cloudstorage.repository;

import com.dimasukimas.cloudstorage.dto.ObjectInfo;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

public interface StorageRepository {
    ObjectInfo createDirectory(String path);

    boolean isObjectExists(String path);

    List<ObjectInfo> getDirectoryContentInfo(String path);

    Optional<ObjectInfo> findObject(String path);

    ObjectInfo upload(String path, InputStream inputStream, long size);

    void delete(String path);
}
