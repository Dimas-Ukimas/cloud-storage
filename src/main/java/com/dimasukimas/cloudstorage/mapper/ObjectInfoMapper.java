package com.dimasukimas.cloudstorage.mapper;

import com.dimasukimas.cloudstorage.repository.StorageObjectInfo;
import io.minio.ObjectWriteResponse;
import io.minio.StatObjectResponse;
import io.minio.messages.Item;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ObjectInfoMapper {

    @Mapping(target = "key", expression = "java(item.objectName())")
    @Mapping(target = "size", expression = "java(item.size())")
    StorageObjectInfo toMetadata(Item item);

    @Mapping(target = "key", expression = "java(object.object())")
    @Mapping(target = "size", expression = "java(object.size())")
    StorageObjectInfo toMetadata(StatObjectResponse object);

    @Mapping(target = "key", expression = "java(object.object())")
    @Mapping(target = "size", expression = "java((Long) null)")
    StorageObjectInfo toMetadata(ObjectWriteResponse object);

    @Mapping(target = "key", expression = "java(object.object())")
    StorageObjectInfo toMetadata(ObjectWriteResponse object, long size);
}
