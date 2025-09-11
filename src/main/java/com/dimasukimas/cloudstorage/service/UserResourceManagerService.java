package com.dimasukimas.cloudstorage.service;

import com.dimasukimas.cloudstorage.dto.ResourceInfoDto;
import com.dimasukimas.cloudstorage.exception.DirectoryAlreadyExists;
import com.dimasukimas.cloudstorage.exception.ParentDirectoryNotExistsException;
import com.dimasukimas.cloudstorage.mapper.ResourceInfoMapper;
import com.dimasukimas.cloudstorage.repository.StorageRepository;
import com.dimasukimas.cloudstorage.util.PathUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserResourceManagerService implements ResourceManagerService {

    private final StorageRepository repository;
    private final ResourceInfoMapper mapper;

    private static final String USER_ROOT_DIRECTORY_PATTERN = "user-%d-files/";

    public ResourceInfoDto createDirectory(Long userId, String path) {
        String fullPath = getFullPath(userId, path);

        checkTargetResourceNotExists(fullPath);
        checkParentDirectoriesExist(fullPath);

        repository.createDirectory(fullPath);

        return mapper.toResDto(fullPath, ResourceType.DIRECTORY.toString());
    }


    public List<ResourceInfoDto> getDirectoryContentInfo(Long userId, String path) {
        String fullPath = getFullPath(userId, path);

        return repository.getDirectoryContentInfo(fullPath).stream()
                .map(object -> object.isDir()
                        ? mapper.toResDto(object.path(), ResourceType.DIRECTORY.toString())
                        : mapper.toResDto(object.path(), object.size(), ResourceType.FILE.toString()))
                .toList();
    }

    public boolean isResourceExists(String path) {
        return repository.isObjectExists(path);
    }

    private void checkTargetResourceNotExists(String path) {
        if (repository.isObjectExists(path)) {
            throw new DirectoryAlreadyExists("Resource is already exists");
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



