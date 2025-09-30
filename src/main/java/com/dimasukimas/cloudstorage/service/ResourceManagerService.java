package com.dimasukimas.cloudstorage.service;

import com.dimasukimas.cloudstorage.dto.ResourceInfoDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ResourceManagerService {
    ResourceInfoDto createDirectory(Long id, String path);

    List<ResourceInfoDto> getDirectoryContentInfo(Long id, String path);

    boolean isResourceExists(String path);

    ResourceInfoDto getResourceInfo(Long id, String path);

    ResourceInfoDto upload(Long id, String path, MultipartFile file);

}
