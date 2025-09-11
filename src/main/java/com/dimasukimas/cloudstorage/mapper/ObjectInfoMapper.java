package com.dimasukimas.cloudstorage.mapper;

import com.dimasukimas.cloudstorage.dto.ObjectInfo;
import io.minio.StatObjectResponse;
import io.minio.messages.Item;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ObjectInfoMapper {

    @Mapping(target = "path", expression = "java(item.objectName())")
    @Mapping(target = "size", expression = "java(item.size())")
    @Mapping( target = "isDir", expression = "java(item.isDir())")
    ObjectInfo toObjectInfo(Item item);

    ObjectInfo toObjectInfo(StatObjectResponse object);

}
