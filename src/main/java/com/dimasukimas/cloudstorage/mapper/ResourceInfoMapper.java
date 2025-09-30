package com.dimasukimas.cloudstorage.mapper;

import com.dimasukimas.cloudstorage.dto.ObjectInfo;
import com.dimasukimas.cloudstorage.dto.ResourceInfoDto;
import com.dimasukimas.cloudstorage.service.PathService;
import com.dimasukimas.cloudstorage.service.ResourceType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", imports = ResourceType.class)
public abstract class ResourceInfoMapper {

    @Autowired
    protected PathService pathService;

    @Mapping(target = "path", expression = "java(pathService.extractPathToResourceWithoutRoot(objectInfo.path()))")
    @Mapping(target = "name", expression = "java(pathService.extractResourceName(objectInfo.path()))")
    @Mapping(target = "size", expression = "java(objectInfo.isDir() ? null : objectInfo.size())")
    @Mapping(target = "type", expression = "java(objectInfo.isDir() ? ResourceType.DIRECTORY : ResourceType.FILE)")
    public abstract ResourceInfoDto toResDto(ObjectInfo objectInfo);

}
