package com.dimasukimas.cloudstorage.mapper;

import com.dimasukimas.cloudstorage.repository.ResourceMetadata;
import com.dimasukimas.cloudstorage.service.PathService;
import io.minio.ObjectWriteResponse;
import io.minio.StatObjectResponse;
import io.minio.messages.Item;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class ObjectInfoMapper {

    @Autowired
    protected PathService pathService;

    @Mapping(target = "path", expression = "java(item.objectName())")
    @Mapping(target = "size", expression = "java(pathService.isDir(item.objectName()) ? null : item.size())")
    @Mapping(target = "isDir", expression = "java(pathService.isDir(item.objectName()))")
    public abstract ResourceMetadata toMetadata(Item item);

    @Mapping(target = "path", expression = "java(object.object())")
    @Mapping(target = "size", expression = "java(pathService.isDir(object.object()) ? null : object.size())")
    @Mapping(target = "isDir", expression = "java(pathService.isDir(object.object()))")
    public abstract ResourceMetadata toMetadata(StatObjectResponse object);

    @Mapping(target = "path", expression = "java(object.object())")
    @Mapping(target = "size", expression = "java((Long) null)")
    @Mapping(target = "isDir", expression = "java(pathService.isDir(object.object()))")
    public abstract ResourceMetadata toMetadata(ObjectWriteResponse object);

    @Mapping(target = "path", expression = "java(object.object())")
    @Mapping(target = "isDir", expression = "java(pathService.isDir(object.object()))")
    public abstract ResourceMetadata toMetadata(ObjectWriteResponse object, long size);

}
