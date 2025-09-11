package com.dimasukimas.cloudstorage.mapper;

import com.dimasukimas.cloudstorage.dto.ResourceInfoDto;
import com.dimasukimas.cloudstorage.util.PathUtils;
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
    public ResourceInfoDto toResDto(String path, String type);

    @Named("mapPath")
    default String mapPath(String path) {
        return PathUtils.extractPathToResource(path);
    }

    @Named("mapName")
    default String mapName(String path) {
        return PathUtils.extractResourceName(path);
    }

}
