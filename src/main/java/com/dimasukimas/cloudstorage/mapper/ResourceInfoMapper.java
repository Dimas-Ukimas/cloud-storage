package com.dimasukimas.cloudstorage.mapper;

import com.dimasukimas.cloudstorage.dto.ObjectInfo;
import com.dimasukimas.cloudstorage.dto.ResourceInfoDto;
import com.dimasukimas.cloudstorage.service.ResourceType;
import com.dimasukimas.cloudstorage.util.PathUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", imports = ResourceType.class)
public interface ResourceInfoMapper {

    @Mapping(target = "path", source = "path", qualifiedByName = "mapPath")
    @Mapping(target = "name",source = "path", qualifiedByName = "mapName")
    @Mapping(target = "size", expression = "java(objectInfo.isDir() ? null : objectInfo.size())")
    @Mapping(target = "type", expression = "java(objectInfo.isDir() ? ResourceType.DIRECTORY : ResourceType.FILE)")
    public ResourceInfoDto toResDto(ObjectInfo objectInfo);

    @Named("mapPath")
    default String mapPath(String path) {
        return PathUtils.extractPathToResource(path);
    }

    @Named("mapName")
    default String mapName(String path) {
        return PathUtils.extractResourceName(path);
    }

}
