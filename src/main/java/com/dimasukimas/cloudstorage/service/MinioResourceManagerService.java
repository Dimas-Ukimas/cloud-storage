package com.dimasukimas.cloudstorage.service;

import com.dimasukimas.cloudstorage.config.minio.MinioProperties;
import com.dimasukimas.cloudstorage.dto.ObjectInfo;
import com.dimasukimas.cloudstorage.dto.ResourceInfoDto;
import com.dimasukimas.cloudstorage.exception.*;
import com.dimasukimas.cloudstorage.mapper.ResourceInfoMapper;
import com.dimasukimas.cloudstorage.repository.StorageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MinioResourceManagerService implements ResourceManagerService {

    private final StorageRepository repository;
    private final ResourceInfoMapper mapper;
    private final MinioProperties minioProperties;
    private final PathService pathService;

    @Override
    public ResourceInfoDto createDirectory(Long userId, String path) {
        String fullPath = addRootDirBefore(userId, path);

        checkResourceNotExists(fullPath);
        checkParentDirectoriesExist(fullPath);

        return mapper.toResDto(repository.createDirectory(fullPath));
    }

    @Override
    public List<ResourceInfoDto> getDirectoryContentInfo(Long userId, String path) {
        String fullPath = addRootDirBefore(userId, path);
        checkResourceExists(fullPath);

        return repository.getDirectoryContentInfo(fullPath)
                .stream()
                .map(mapper::toResDto)
                .toList();
    }

    @Override
    public ResourceInfoDto getResourceInfo(Long userId, String path) {
        String fullPath = addRootDirBefore(userId, path);

        ObjectInfo object = repository.findObject(fullPath).orElseThrow(() -> new ResourceNotFoundException("Resource does not exists"));

        return mapper.toResDto(object);
    }

    @Override
    public ResourceInfoDto upload(Long userId, String path, MultipartFile file) {
        String parentPath = addRootDirBefore(userId, path);
        String fullPath = parentPath + file.getOriginalFilename();
        checkResourceNotExists(fullPath);

        pathService.extractSubdirectoriesFromPath(path)
                .stream()
                .map(subDir -> addRootDirBefore(userId, subDir))
                .forEach(this::createSubdirectory);

        try {
            if (file.getSize() <= minioProperties.getFileMaxSize().toMegabytes()) {
                ObjectInfo objectInfo = repository.upload(fullPath, file.getInputStream(), file.getSize());

                return mapper.toResDto(objectInfo);
            }
            throw new MaxSizeExceedingException("File cannot be uploaded due to max size exceeding");
        } catch (IOException e) {
            throw new FileProcessingException("Failed to read uploaded file");
        }
    }

    private void createSubdirectory(String path) {
        if (!isResourceExists(path)) {
            repository.createDirectory(path);
        }
    }

    public boolean isResourceExists(String path) {
        return repository.isObjectExists(path);
    }

    private void checkResourceExists(String path) {
        if (!repository.isObjectExists(path)) {
            throw new ResourceNotFoundException("Resource does not exists");
        }
    }

    private void checkResourceNotExists(String path) {
        if (repository.isObjectExists(path)) {
            throw new ResourceAlreadyExistsException("Resource is already exists");
        }
    }

    private void checkParentDirectoriesExist(String path) {
        String parentPath = pathService.extractFullPathToResource(path);

        if (parentPath.isEmpty()) {
            return;
        }

        if (!repository.isObjectExists(parentPath)) {
            throw new ParentDirectoryNotExistsException("Parent directory does not exists");
        }
    }

    private String addRootDirBefore(Long userId, String path) {
        return String.format(minioProperties.getUserRootDirectoryPattern(), userId) + path;
    }

}



