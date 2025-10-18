package com.dimasukimas.cloudstorage.helper;

import com.dimasukimas.cloudstorage.config.minio.MinioProperties;
import com.dimasukimas.cloudstorage.exception.MinioOperationException;
import com.dimasukimas.cloudstorage.service.PathService;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MinioTestHelper {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;
    private final PathService pathService;

    public Optional<StatObjectResponse> findObject(String objectName) {
        StatObjectResponse objectStat;
        try {
            objectStat = minioClient.statObject(StatObjectArgs
                    .builder()
                    .bucket(minioProperties.getBucketName())
                    .object(objectName)
                    .build());

        } catch (ErrorResponseException e) {
            var code = e.errorResponse().code();
            if ("NoSuchKey".equals(code)) {
                return Optional.empty();
            }
            throw new RuntimeException("Failed to find object", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to find object", e);
        }
        return Optional.ofNullable(objectStat);
    }

    public void clearBucket() {
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs
                    .builder()
                    .bucket(minioProperties.getBucketName())
                    .recursive(true)
                    .build());

            for (Result<Item> result : results) {
                Item item = result.get();
                minioClient.removeObject(RemoveObjectArgs
                        .builder()
                        .bucket(minioProperties.getBucketName())
                        .object(item.objectName())
                        .build());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to clear bucket: " + minioProperties.getBucketName(), e);
        }
    }

    public String createUserRootDirectory(long userId) {
        String rootDirectory = pathService.getUserRootDirectoryName(userId);

        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(rootDirectory)
                    .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return rootDirectory;
    }

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

    public List<String> listAll(String prefix) {
        List<String> files = new ArrayList<>();

        Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs
                .builder()
                .bucket(minioProperties.getBucketName())
                .prefix(prefix)
                .recursive(true)
                .build());

        if (results.iterator().hasNext()) {
            String name;
            try {
                name = results.iterator().next().get().objectName();
            } catch (Exception e) {
                throw new RuntimeException("Failed to get test item");
            }

            files.add(name);
        }
        return files;
    }


}
