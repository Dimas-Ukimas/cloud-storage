package com.dimasukimas.cloudstorage.repository;

//TODO возможно убрать тип
public record ContentMetadata(String filename, Long size, ArtifactType type) {
}
