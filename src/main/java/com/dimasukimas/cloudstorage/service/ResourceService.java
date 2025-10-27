package com.dimasukimas.cloudstorage.service;

import com.dimasukimas.cloudstorage.dto.ResourceInfoResponseDto;
import com.dimasukimas.cloudstorage.repository.ContentSource;
import com.dimasukimas.cloudstorage.validation.SafePath;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Validated
public interface ResourceService {
    ResourceInfoResponseDto createDirectory(Long id, @SafePath String path);

    List<ResourceInfoResponseDto> getDirectoryContentInfo(Long id, @SafePath String path);

    ResourceInfoResponseDto getResourceInfo(Long id, @SafePath String path);

    List<ResourceInfoResponseDto> search(Long userId, String query);

    List<ResourceInfoResponseDto> upload(Long id, @SafePath String path, List<MultipartFile> files);

    void delete(Long id, @SafePath String path);

    ContentSource download(Long id, @SafePath String path);

    ResourceInfoResponseDto moveOrRename(Long id, @SafePath String from, @SafePath String to);

}
