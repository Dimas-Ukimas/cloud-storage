package com.dimasukimas.cloud_storage.config.minio;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MinioInitializer implements ApplicationRunner {

    private final MinioClient minioClient;

    @Value("spring.minio.bucket-name")
    private final String bucketName;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        boolean found = minioClient.bucketExists(BucketExistsArgs
                .builder()
                .bucket(bucketName)
                .build());

        if (!found) {
            minioClient.makeBucket(MakeBucketArgs
                    .builder()
                    .bucket(bucketName)
                    .build());
        }
    }
}
