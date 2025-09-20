package com.dimasukimas.cloudstorage.mapper;

import com.dimasukimas.cloudstorage.dto.ObjectInfo;
import io.minio.ObjectWriteResponse;
import io.minio.StatObjectResponse;
import io.minio.messages.Item;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface ObjectInfoMapper {

    @Mapping(target = "path", expression = "java(item.objectName())")
    @Mapping(target = "size", expression = "java(item.isDir() ? null : item.size())")
    @Mapping(target = "isDir", expression = "java(item.isDir())")
    ObjectInfo toObjectInfo(Item item);

    @Mapping(target = "path", expression = "java(object.object())")
    @Mapping(target = "size", expression = "java(object.size())")
    @Mapping(target = "isDir", expression = "java(mapIsDirFromPath(object.object()))")
    ObjectInfo toObjectInfo(StatObjectResponse object);

    @Mapping(target = "path", expression = "java(object.object())")
    @Mapping(target = "size", expression = "java((Long) null)")
    @Mapping(target = "isDir", constant = "true")
    ObjectInfo toObjectInfo(ObjectWriteResponse object);

    @Named("mapIsDirFromPath")
    default boolean mapIsDirFromPath(String path) {
        return path != null && path.endsWith("/");
    }

}
