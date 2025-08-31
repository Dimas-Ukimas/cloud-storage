package com.dimasukimas.cloud_storage.mapper;

import com.dimasukimas.cloud_storage.dto.ObjectInfo;
import io.minio.messages.Item;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ObjectInfoMapper {

    @Mapping(target = "path", expression = "java(item.objectName())")
    @Mapping(target = "size", expression = "java(item.size())")
    @Mapping( target = "isDir", expression = "java(item.isDir())")
    public ObjectInfo toObjectInfo(Item item);

}
