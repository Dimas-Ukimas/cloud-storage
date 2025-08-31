package com.dimasukimas.cloud_storage.service;

import com.dimasukimas.cloud_storage.dto.ResourceInfoDto;

import java.util.Optional;

public interface ResourceManagerService {

    ResourceInfoDto createDirectory(Long id, String path);

    ResourceInfoDto getResourceInfo(String path);

    boolean isResourceExists(String path);
}
