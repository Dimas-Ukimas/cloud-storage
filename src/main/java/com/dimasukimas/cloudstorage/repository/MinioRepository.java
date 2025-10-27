package com.dimasukimas.cloudstorage.repository;

import com.dimasukimas.cloudstorage.config.minio.MinioProperties;
import com.dimasukimas.cloudstorage.exception.MinioOperationException;
import com.dimasukimas.cloudstorage.mapper.ObjectInfoMapper;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;

@Repository
@RequiredArgsConstructor
public class MinioRepository implements StorageRepository {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;
    private final ObjectInfoMapper mapper;

    @Override
    public StorageObjectInfo putEmptyObject(String objectKey) {

        ObjectWriteResponse object;
        try {
            object = minioClient.putObject(PutObjectArgs
                    .builder()
                    .bucket(minioProperties.getBucketName())
                    .object(objectKey)
                    .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                    .build());
        } catch (Exception e) {
            throw new MinioOperationException("Something went wrong, please, try again later", e);

        }
        return mapper.toMetadata(object);
    }

    @Override
    public List<StorageObjectInfo> listChildren(String prefix) {
        Iterable<Result<Item>> results = findDirectObjects(prefix);

        List<StorageObjectInfo> objects = new ArrayList<>();
        for (Result<Item> result : results) {
            try {
                Item item = result.get();

                boolean isParentObject = item.objectName().equals(prefix);

                if (isParentObject) {
                    continue;
                }
                objects.add(mapper.toMetadata(item));

            } catch (Exception e) {
                throw new MinioOperationException("Something went wrong, please, try again later", e);
            }
        }

        return objects;
    }

    @Override
    public boolean isObjectExists(String objectKey) {
        return findObject(objectKey).isPresent();
    }

    @Override
    public Optional<StorageObjectInfo> findObject(String path) {
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

        return Optional.ofNullable(mapper.toMetadata(object));
    }

    @Override
    public StorageObjectInfo putObject(String objectKey, InputStream inputStream, long size) {
        ObjectWriteResponse object;
        try {
            object = minioClient.putObject(PutObjectArgs
                    .builder()
                    .bucket(minioProperties.getBucketName())
                    .object(objectKey)
                    .stream(inputStream, size, -1)
                    .build());
        } catch (Exception e) {
            throw new MinioOperationException("Something went wrong, please, try again later", e);

        }
        return mapper.toMetadata(object, size);
    }

    @Override
    public Supplier<InputStream> getObjectStream(String objectKey) {
        return () -> {
            InputStream objectStream;
            try {
                objectStream = minioClient.getObject(GetObjectArgs
                        .builder()
                        .bucket(minioProperties.getBucketName())
                        .object(objectKey)
                        .build());
            } catch (Exception e) {
                throw new MinioOperationException("Something went wrong, please, try again later", e);
            }
            return objectStream;
        };
    }

    @Override
    public StorageObjectInfo copyObject(String sourceKey, String targetKey) {
        ObjectWriteResponse response;
        try {
            response = minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(targetKey)
                            .source(
                                    CopySource.builder()
                                            .bucket(minioProperties.getBucketName())
                                            .object(sourceKey)
                                            .build())
                            .build());
        } catch (Exception e) {
            throw new MinioOperationException("Something went wrong, please, try again later", e);
        }
        return mapper.toMetadata(response);
    }

    @Override
    public List<StorageObjectInfo> listRecursive(String prefix) {
        Iterable<Result<Item>> results = findAllObjects(prefix);

        List<StorageObjectInfo> resources = new ArrayList<>();
        for (Result<Item> result : results) {
            try {
                Item item = result.get();
                resources.add(mapper.toMetadata(item));
            } catch (Exception e) {
                throw new MinioOperationException("Something went wrong, please, try again later", e);
            }
        }
        return resources;
    }

    @Override
    public void removeObjects(String prefix) {
        Iterable<Result<Item>> objects = findAllObjects(prefix);
        List<DeleteObject> deleteObjects = convertToDeleteObjects(objects);

        try {
            Iterable<Result<DeleteError>> deleteResults = minioClient.removeObjects(RemoveObjectsArgs
                    .builder()
                    .bucket(minioProperties.getBucketName())
                    .objects(deleteObjects)
                    .build());

            StringBuilder errorMessage = new StringBuilder();
            for (Result<DeleteError> result : deleteResults) {
                DeleteError error = result.get();
                errorMessage.append(error.objectName()).append("; ").append(error.message()).append(" ");
            }
            if (!errorMessage.isEmpty()) {
                throw new MinioOperationException("Error in deleting objects: " + errorMessage);
            }
        } catch (
                Exception e) {
            throw new MinioOperationException("Something went wrong, please, try again later", e);
        }
    }

    @Override
    public void removeObject(String objectKey) {
        try {
            minioClient.removeObject(RemoveObjectArgs
                    .builder()
                    .bucket(minioProperties.getBucketName())
                    .object(objectKey)
                    .build());
        } catch (Exception e) {
            throw new MinioOperationException("Something went wrong, please, try again later", e);
        }
    }

    private Iterable<Result<Item>> findAllObjects(String prefix) {
        return minioClient.listObjects(ListObjectsArgs
                .builder()
                .bucket(minioProperties.getBucketName())
                .prefix(prefix)
                .recursive(true)
                .build());
    }

    private Iterable<Result<Item>> findDirectObjects(String prefix) {
        return minioClient.listObjects(ListObjectsArgs
                .builder()
                .bucket(minioProperties.getBucketName())
                .prefix(prefix)
                .recursive(false)
                .build());
    }

    private List<DeleteObject> convertToDeleteObjects(Iterable<Result<Item>> items) {
        Spliterator<Result<Item>> spliterator = items.spliterator();

        return StreamSupport.stream(spliterator, false)
                .map(item -> {
                    try {
                        return new DeleteObject(item.get().objectName());
                    } catch (Exception e) {
                        throw new MinioOperationException("Something went wrong, please, try again later", e);
                    }
                })
                .toList();
    }
}
