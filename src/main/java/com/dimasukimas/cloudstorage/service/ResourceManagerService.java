package com.dimasukimas.cloudstorage.service;

import com.dimasukimas.cloudstorage.dto.ResourceInfoDto;
import com.dimasukimas.cloudstorage.repository.ContentSource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ResourceManagerService {
    ResourceInfoDto createDirectory(Long id, String path);

    List<ResourceInfoDto> getDirectoryContentInfo(Long id, String path);

    boolean isResourceExists(String path);

    ResourceInfoDto getResourceInfo(Long id, String path);

    List<ResourceInfoDto> upload(Long id, String path, List<MultipartFile> files);

    void delete(Long id, String path);

    ContentSource download(Long id, String path);

}
