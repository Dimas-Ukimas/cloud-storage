package com.dimasukimas.cloudstorage.mapper;

import com.dimasukimas.cloudstorage.dto.ResourceInfoDto;
import com.dimasukimas.cloudstorage.repository.ResourceMetadata;
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

    @Mapping(target = "path", expression = "java(pathService.extractRootlessPathToResource(resourceMetadata.path()))")
    @Mapping(target = "name", expression = "java(pathService.extractResourceName(resourceMetadata.path()))")
    @Mapping(target = "size", expression = "java(resourceMetadata.size())")
    @Mapping(target = "type", expression = "java(resourceMetadata.isDir() ? ResourceType.DIRECTORY : ResourceType.FILE)")
    public abstract ResourceInfoDto toResDto(ResourceMetadata resourceMetadata);

    @Mapping(target = "relativePath", expression = "java(pathService.extractRelativePath(resourceMetadata.path(), rootPath))")
    @Mapping(target = "size", expression = "java(resourceMetadata.size())")
    @Mapping(target = "type", expression = "java(resourceMetadata.isDir() ? ResourceType.DIRECTORY : ResourceType.FILE)")
    public abstract ZipEntrySpec toZipEntrySpec(ResourceMetadata resourceMetadata, String rootPath, Supplier<InputStream> content);

}
