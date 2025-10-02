package com.dimasukimas.cloudstorage.repository;

import com.dimasukimas.cloudstorage.config.minio.MinioProperties;
import com.dimasukimas.cloudstorage.dto.ObjectInfo;
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
import java.util.stream.StreamSupport;

@Repository
@RequiredArgsConstructor
public class MinioRepository implements StorageRepository {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;
    private final ObjectInfoMapper mapper;

    public ObjectInfo createDirectory(String path) {

        ObjectWriteResponse object;
        try {
            object = minioClient.putObject(PutObjectArgs
                    .builder()
                    .bucket(minioProperties.getBucketName())
                    .object(path)
                    .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                    .build());
        } catch (Exception e) {
            throw new MinioOperationException("Something went wrong, please, try again later", e);

        }
        return mapper.toObjectInfo(object);
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

                //TODO проверить почему рутовая папка не isDir, проверить, отображается ли она на фронте
                boolean isMarkerDirectory = item.isDir()
                        && item.objectName().equals(path)
                        && item.size() == 0;

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

    @Override
    public ObjectInfo upload(String path, InputStream inputStream, long size) {
        ObjectWriteResponse object;
        try {
            object = minioClient.putObject(PutObjectArgs
                    .builder()
                    .bucket(minioProperties.getBucketName())
                    .object(path)
                    .stream(inputStream, size, -1)
                    .build());
        } catch (Exception e) {
            throw new MinioOperationException("Something went wrong, please, try again later", e);

        }
        return mapper.toObjectInfo(object, size);
    }

    @Override
    public void delete(String path) {
        boolean isDir = path.endsWith(minioProperties.getDirectorySplitter());

        if (isDir) {
            List<DeleteObject> deleteObjects = convertDirectoryContentToDeleteObjects(path);
            removeObjects(deleteObjects);
        } else {
            removeObject(path);
        }
    }

    private List<DeleteObject> convertDirectoryContentToDeleteObjects(String path) {
        Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs
                .builder()
                .bucket(minioProperties.getBucketName())
                .prefix(path)
                .recursive(true)
                .build());

        Spliterator<Result<Item>> spliterator = results.spliterator();

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

    private void removeObject(String path) {
        try {
            minioClient.removeObject(RemoveObjectArgs
                    .builder()
                    .bucket(minioProperties.getBucketName())
                    .object(path)
                    .build());
        } catch (Exception e) {
            throw new MinioOperationException("Something went wrong, please, try again later", e);
        }
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
}
