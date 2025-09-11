package com.dimasukimas.cloudstorage.repository;

import com.dimasukimas.cloudstorage.config.minio.MinioProperties;
import com.dimasukimas.cloudstorage.dto.ObjectInfo;
import com.dimasukimas.cloudstorage.exception.MinioOperationException;
import com.dimasukimas.cloudstorage.mapper.ObjectInfoMapper;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
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

    public List<ObjectInfo> getDirectoryContentInfo(String path) {
        Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs
                .builder()
                .bucket(minioProperties.getBucketName())
                .prefix(path)
                .recursive(false)
                .build());

        List<ObjectInfo> contentInfo = new ArrayList<>();
        for (Result<Item> result : results) {
            try {
                Item item = result.get();
                boolean isMarkerDirectory = !item.isDir() && item.objectName().equals(path);

                if (isMarkerDirectory) {
                    continue;
                }

                contentInfo.add(mapper.toObjectInfo(item));
            } catch (Exception e) {
                throw new MinioOperationException("Something went wrong, please, try again later", e);
            }
        }

        return contentInfo;
    }


    public boolean isObjectExists(String path) {

        return findObject(path).isPresent();
    }

    public Optional<ObjectInfo> findObject(String path) {
        StatObjectResponse object;
        try {
            object = minioClient.statObject(StatObjectArgs
                    .builder()
                    .bucket(minioProperties.getBucketName())
                    .object(path)
                    .build());

        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equals("NoSuchKey")) {
                return Optional.empty();
            }
            throw new MinioOperationException("Something went wrong, please, try again later", e);
        } catch (Exception e) {
            throw new MinioOperationException("Something went wrong, please, try again later", e);
        }

        return Optional.ofNullable(mapper.toObjectInfo(object));
    }


}
