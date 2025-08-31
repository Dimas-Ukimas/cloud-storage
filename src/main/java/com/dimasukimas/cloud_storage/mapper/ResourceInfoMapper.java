package com.dimasukimas.cloud_storage.mapper;

import com.dimasukimas.cloud_storage.dto.ResourceInfoDto;
import com.dimasukimas.cloud_storage.util.PathUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface ResourceInfoMapper {

    @Mapping(target = "path", source = "path", qualifiedByName = "mapPath")
    @Mapping(target = "name",source = "path", qualifiedByName = "mapName")
    public ResourceInfoDto toResDto(String path, Long size, String type);

    @Mapping(target = "path", source = "path", qualifiedByName = "mapPath")
    @Mapping(target = "name",source = "path", qualifiedByName = "mapName")
    public ResourceInfoDto toResDto(String path, String name, String type);

    @Named("mapPath")
    default String mapPath(String path) {
        return PathUtils.extractParentPathToResource(path);
    }

    @Named("mapName")
    default String mapName(String path) {
        return PathUtils.extractResourceName(path);
    }

}
