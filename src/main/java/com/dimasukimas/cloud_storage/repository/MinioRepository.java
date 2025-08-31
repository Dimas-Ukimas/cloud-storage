package com.dimasukimas.cloud_storage.repository;

import com.dimasukimas.cloud_storage.config.minio.MinioProperties;
import com.dimasukimas.cloud_storage.dto.ObjectInfo;
import com.dimasukimas.cloud_storage.exception.MinioOperationException;
import com.dimasukimas.cloud_storage.mapper.ObjectInfoMapper;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.io.ByteArrayInputStream;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MinioRepository implements StorageRepository {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;
    private final ObjectInfoMapper mapper;

    public void createDirectory(String path) {
        try {
            minioClient.putObject(PutObjectArgs
                    .builder()
                    .bucket(minioProperties.getBucketName())
                    .object(path)
                    .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                    .build());
        } catch (Exception e) {
            throw new MinioOperationException("Something went wrong, please, try again later", e);
        }
    }

    public Optional<ObjectInfo> findObject(String path) {
        Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs
                .builder()
                .bucket(minioProperties.getBucketName())
                .prefix(path)
                .maxKeys(1)
                .build());

        Item item = null;
        for (Result<Item> result : results) {
            try {
                item = result.get();
            } catch (Exception e) {
                throw new MinioOperationException("Something went wrong, please, try again later", e);
            }
        }

        return Optional.ofNullable(mapper.toObjectInfo(item));
    }

    public boolean isObjectExists(String path) {

        return findObject(path).isPresent();
    }


}
