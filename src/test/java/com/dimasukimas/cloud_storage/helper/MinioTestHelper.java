package com.dimasukimas.cloud_storage.helper;

import com.dimasukimas.cloud_storage.config.minio.MinioProperties;
import io.minio.*;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MinioTestHelper {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    public Optional<StatObjectResponse> findObject(String objectName) {
        StatObjectResponse objectStat;
        try {
            objectStat = minioClient.statObject(StatObjectArgs
                    .builder()
                    .bucket(minioProperties.getBucketName())
                    .object(objectName)
                    .build());
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

    private void createBucketIfNotExist() {
        boolean found = false;

        try {
            found = minioClient.bucketExists(BucketExistsArgs
                    .builder()
                    .bucket(minioProperties.getBucketName())
                    .build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs
                        .builder()
                        .bucket(minioProperties.getBucketName())
                        .build());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);

        }
    }
    
    
}
