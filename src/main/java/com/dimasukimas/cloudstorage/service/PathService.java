package com.dimasukimas.cloudstorage.service;

import com.dimasukimas.cloudstorage.config.minio.MinioProperties;
import com.dimasukimas.cloudstorage.util.PathUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PathService {

    private final MinioProperties minioProperties;

    public String extractResourceName(String path) {
        return PathUtils.extractResourceName(path, minioProperties.getDirectorySplitter());
    }

    public String extractPathToResourceWithoutRoot(String path) {
        return PathUtils.extractPathToResourceWithoutRoot(path, minioProperties.getUserRootDirectoryRegex(), minioProperties.getDirectorySplitter());
    }

    public String extractFullPathToResource(String path) {
        return PathUtils.extractPathToResource(path, minioProperties.getDirectorySplitter());
    }

    public List<String> extractSubdirectoriesFromPath(String path) {
        return PathUtils.extractSubdirectoriesFromPath(path, minioProperties.getDirectorySplitter());
    }

    public String createUserRootDirectoryName(Long userId) {
        return PathUtils.createUserRootDirectoryName(userId, minioProperties.getUserRootDirectoryPattern());
    }

}
