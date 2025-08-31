package com.dimasukimas.cloud_storage.service;

import com.dimasukimas.cloud_storage.dto.ObjectInfo;
import com.dimasukimas.cloud_storage.dto.ResourceInfoDto;
import com.dimasukimas.cloud_storage.exception.DirectoryAlreadyExists;
import com.dimasukimas.cloud_storage.exception.ParentDirectoryNotExistsException;
import com.dimasukimas.cloud_storage.exception.ResourceNotFoundException;
import com.dimasukimas.cloud_storage.mapper.ResourceInfoMapper;
import com.dimasukimas.cloud_storage.repository.StorageRepository;
import com.dimasukimas.cloud_storage.util.PathUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserResourceManagerService implements ResourceManagerService {

    private final StorageRepository repository;
    private final ResourceInfoMapper mapper;

    public ResourceInfoDto createDirectory(Long userId, String path) {
        String userRootDirectoryPath = createUserRootDirectoryIfNotExists(userId);
        String fullPath = userRootDirectoryPath + path;

        checkParentDirectoriesExist(fullPath);
        checkTargetDirectoryNotExists(fullPath);

        repository.createDirectory(path);

        return getResourceInfo(path);
    }


    public ResourceInfoDto getResourceInfo(String fullPath) {
        ObjectInfo info = repository.findObject(fullPath).orElseThrow(() -> new ResourceNotFoundException("Resource was not found"));

        if (info.isDir()) {
//            return mapper.toResDto(info.path(), ResourceType.DIRECTORY.toString());
        }

        return mapper.toResDto(info.path(), info.size(), ResourceType.FILE.toString());
    }

    public boolean isResourceExists(String path) {
        return repository.isObjectExists(path);
    }

    private void checkTargetDirectoryNotExists(String path) {
        if (repository.isObjectExists(path)) {
            throw new DirectoryAlreadyExists("Directory already exists");
        }
    }

    private void checkParentDirectoriesExist(String path) {
        String parentPath = PathUtils.extractParentPathToResource(path);

        if (parentPath.isEmpty()) {
            return;
        }

        if (!repository.isObjectExists(parentPath)) {
            throw new ParentDirectoryNotExistsException("Parent directory does not exists");
        }
    }

    public String createUserRootDirectoryIfNotExists(Long userId) {
        String userRootDirectoryPath = PathUtils.createUserDirectoryName(userId);

        boolean isExist = repository.isObjectExists(userRootDirectoryPath);

        if (!isExist) {
            repository.isObjectExists(userRootDirectoryPath);
        }

        return userRootDirectoryPath;
    }

}



