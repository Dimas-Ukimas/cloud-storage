package com.dimasukimas.cloudstorage.repository;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public interface StorageRepository {
    StorageObjectInfo putEmptyObject(String objectKey);

    boolean isObjectExists(String objectKey);

    List<StorageObjectInfo> listChildren(String prefix);

    Optional<StorageObjectInfo> findObject(String objectKey);

    List<StorageObjectInfo> listRecursive(String prefix);

    StorageObjectInfo putObject(String objectKey, InputStream inputStream, long size);

    void removeObjects(String prefix);

    void removeObject(String objectKey);

    Supplier<InputStream> getObjectStream(String objectKey);

    StorageObjectInfo copyObject(String sourceKey, String targetKey);

}
