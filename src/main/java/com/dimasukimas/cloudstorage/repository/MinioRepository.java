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
    public ResourceMetadata createDirectory(String prefix) {

        ObjectWriteResponse object;
        try {
            object = minioClient.putObject(PutObjectArgs
                    .builder()
                    .bucket(minioProperties.getBucketName())
                    .object(prefix)
                    .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                    .build());
        } catch (Exception e) {
            throw new MinioOperationException("Something went wrong, please, try again later", e);

        }
        return mapper.toMetadata(object);
    }

    @Override
    public List<ResourceMetadata> getDirectoryContentInfo(String prefix) {
        Iterable<Result<Item>> results = findDirectObjects(prefix);

        List<ResourceMetadata> contentInfo = new ArrayList<>();
        for (Result<Item> result : results) {
            try {
                Item item = result.get();

                boolean isCurrentDirectory = item.objectName().equals(prefix);

                if (isCurrentDirectory) {
                    continue;
                }
                contentInfo.add(mapper.toMetadata(item));

            } catch (Exception e) {
                throw new MinioOperationException("Something went wrong, please, try again later", e);
            }
        }

        return contentInfo;
    }

    @Override
    public boolean isResourceExists(String prefix) {
        return findResource(prefix).isPresent();
    }

    @Override
    public Optional<ResourceMetadata> findResource(String path) {
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
    public ResourceMetadata upload(String objectName, InputStream inputStream, long size) {
        ObjectWriteResponse object;
        try {
            object = minioClient.putObject(PutObjectArgs
                    .builder()
                    .bucket(minioProperties.getBucketName())
                    .object(objectName)
                    .stream(inputStream, size, -1)
                    .build());
        } catch (Exception e) {
            throw new MinioOperationException("Something went wrong, please, try again later", e);

        }
        return mapper.toMetadata(object, size);
    }

    @Override
    public void deleteDirectory(String prefix) {
        Iterable<Result<Item>> objects = findAllObjects(prefix);
        List<DeleteObject> deleteObjects = convertToDeleteObjects(objects);
        removeObjects(deleteObjects);
    }

    @Override
    public void deleteFile(String prefix) {
        removeObject(prefix);
    }

    @Override
    public Supplier<InputStream> download(String prefix) {
        return () -> {
            InputStream content;
            try {
                content = minioClient.getObject(GetObjectArgs
                        .builder()
                        .bucket(minioProperties.getBucketName())
                        .object(prefix)
                        .build());
            } catch (Exception e) {
                throw new MinioOperationException("Something went wrong, please, try again later", e);
            }
            return content;
        };
    }

    @Override
    public List<ResourceMetadata> listAll(String prefix) {
        Iterable<Result<Item>> results = findAllObjects(prefix);

        List<ResourceMetadata> resources = new ArrayList<>();
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

    private void removeObjects(List<DeleteObject> deleteObjects) {
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

    private void removeObject(String prefix) {
        try {
            minioClient.removeObject(RemoveObjectArgs
                    .builder()
                    .bucket(minioProperties.getBucketName())
                    .object(prefix)
                    .build());
        } catch (Exception e) {
            throw new MinioOperationException("Something went wrong, please, try again later", e);
        }
    }
}
