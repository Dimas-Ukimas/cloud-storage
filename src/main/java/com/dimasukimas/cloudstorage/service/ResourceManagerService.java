package com.dimasukimas.cloudstorage.service;

import com.dimasukimas.cloudstorage.dto.ResourceInfoDto;

import java.util.List;

public interface ResourceManagerService {
    ResourceInfoDto createDirectory(Long id, String path);

    List<ResourceInfoDto> getDirectoryContentInfo(Long id, String path);

    boolean isResourceExists(String path);
}
