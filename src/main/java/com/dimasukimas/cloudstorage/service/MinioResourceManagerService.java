package com.dimasukimas.cloudstorage.service;

import com.dimasukimas.cloudstorage.config.minio.MinioProperties;
import com.dimasukimas.cloudstorage.dto.ObjectInfo;
import com.dimasukimas.cloudstorage.dto.ResourceInfoDto;
import com.dimasukimas.cloudstorage.exception.ResourceAlreadyExistsException;
import com.dimasukimas.cloudstorage.exception.ParentDirectoryNotExistsException;
import com.dimasukimas.cloudstorage.exception.ResourceNotFoundException;
import com.dimasukimas.cloudstorage.mapper.ResourceInfoMapper;
import com.dimasukimas.cloudstorage.repository.StorageRepository;
import com.dimasukimas.cloudstorage.util.PathUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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



