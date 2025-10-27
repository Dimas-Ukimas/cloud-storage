package com.dimasukimas.cloudstorage.mapper;

import com.dimasukimas.cloudstorage.dto.ResourceInfoResponseDto;
import com.dimasukimas.cloudstorage.repository.StorageObjectInfo;
import com.dimasukimas.cloudstorage.repository.ZipEntrySpec;
import com.dimasukimas.cloudstorage.service.PathService;
import com.dimasukimas.cloudstorage.service.ResourceType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.InputStream;
import java.util.function.Supplier;

@Mapper(componentModel = "spring", imports = ResourceType.class)
public abstract class ResourceInfoMapper {

    @Autowired
    protected PathService pathService;

    @Mapping(target = "path", expression = "java(pathService.extractRootlessPathToResource(storageObjectInfo.key()))")
    @Mapping(target = "name", expression = "java(pathService.extractResourceName(storageObjectInfo.key()))")
    @Mapping(target = "size", expression = "java(ResourceType.DIRECTORY.equals(type) ? null : storageObjectInfo.size())")
    public abstract ResourceInfoResponseDto toResDto(StorageObjectInfo storageObjectInfo, ResourceType type);

    @Mapping(target = "relativePath", expression = "java(pathService.extractRelativePath(storageObjectInfo.key(), rootPath))")
    @Mapping(target = "size", expression = "java(ResourceType.DIRECTORY.equals(type) ? null : storageObjectInfo.size())")
    public abstract ZipEntrySpec toZipEntrySpec(StorageObjectInfo storageObjectInfo, String rootPath, Supplier<InputStream> content, ResourceType type);


}
