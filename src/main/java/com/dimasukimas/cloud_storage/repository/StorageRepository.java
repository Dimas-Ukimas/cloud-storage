package com.dimasukimas.cloud_storage.repository;

import com.dimasukimas.cloud_storage.dto.ObjectInfo;

import java.util.Optional;

public interface StorageRepository {
    public void createDirectory(String path);
    public boolean isObjectExists(String path);
    public Optional<ObjectInfo> findObject(String path);

}
