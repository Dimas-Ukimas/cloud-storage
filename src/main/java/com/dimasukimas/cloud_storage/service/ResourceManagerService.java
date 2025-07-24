package com.dimasukimas.cloud_storage.service;

import com.dimasukimas.cloud_storage.dto.DirectoryInfoDto;
import com.dimasukimas.cloud_storage.mapper.ResourceInfoMapper;
import com.dimasukimas.cloud_storage.util.PathExtractor;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResourceManagerService {

    private final MinioClient minioClient;
    private final PathCheckerService pathCheckerService;
    private final ResourceInfoMapper mapper;

    @Value("spring.minio.bucket-name")
    private final String bucketName;

    public DirectoryInfoDto createDirectory(Long userId, String directoryPath) {
        String userRootDirectoryPath = createUserRootDirectoryIfNotExists(userId);
        String fullPath = userRootDirectoryPath + directoryPath;

        pathCheckerService.checkDirectories(fullPath);

        try {
            minioClient.putObject(PutObjectArgs
                    .builder()
                    .bucket(bucketName)
                    .object(fullPath)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Something went wrong, please, try again later");
        }

        Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs
                .builder()
                .bucket(bucketName)
                .prefix(fullPath)
                .maxKeys(1)
                .build());

        String name = "";
        for (Result<Item> result : results) {
            try {
                name = PathExtractor.extractResourceName(result.get().objectName());

                return mapper.toDirDto(directoryPath, name);
            } catch (Exception e) {
                throw new RuntimeException("Something went wrong, please, try again later");
            }
        }
        return mapper.toDirDto(directoryPath, name);
    }

    private String createUserRootDirectoryIfNotExists(Long userId) {
        String userDirectoryPath = String.format("user-%d-files/", userId);

        Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs
                .builder()
                .bucket(bucketName)
                .prefix(userDirectoryPath)
                .maxKeys(1)
                .build());

        for (Result<Item> result : results) {
            try {
                result.get();
                return userDirectoryPath;
            } catch (Exception e) {
                throw new RuntimeException("Something went wrong, please, try again later");
            }
        }

        try {
            minioClient.putObject(PutObjectArgs
                    .builder()
                    .bucket(bucketName)
                    .object(userDirectoryPath)
                    .build());

            return userDirectoryPath;
        } catch (Exception e) {
            throw new RuntimeException("Something went wrong, please, try again later");
        }
    }
}
