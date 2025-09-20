package com.dimasukimas.cloudstorage.service;

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

    private static final String USER_ROOT_DIRECTORY_PATTERN = "user-%d-files/";

    public ResourceInfoDto createDirectory(Long userId, String path) {
        String fullPath = getFullPath(userId, path);

        checkResourceNotExists(fullPath);
        checkParentDirectoriesExist(fullPath);

        return mapper.toResDto(repository.createDirectory(fullPath));
    }

    public List<ResourceInfoDto> getDirectoryContentInfo(Long userId, String path) {
        String fullPath = getFullPath(userId, path);
        checkResourceExists(fullPath);

        return repository.getDirectoryContentInfo(fullPath)
                .stream()
                .map(mapper::toResDto)
                .toList();
    }

    public ResourceInfoDto getResourceInfo(Long userId, String path) {
        String fullPath = getFullPath(userId, path);

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

    private void checkResourceNotExists(String path){
        if (repository.isObjectExists(path)) {
            throw new ResourceAlreadyExistsException("Resource is already exists");
        }
    }

    private void checkParentDirectoriesExist(String path) {
        String parentPath = PathUtils.extractPathToResource(path);

        if (parentPath.isEmpty()) {
            return;
        }

        if (!repository.isObjectExists(parentPath)) {
            throw new ParentDirectoryNotExistsException("Parent directory does not exists");
        }
    }

    private String getFullPath(Long userId, String path) {
        return String.format(USER_ROOT_DIRECTORY_PATTERN, userId) + path;
    }

}



