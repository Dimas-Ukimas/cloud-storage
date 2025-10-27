package com.dimasukimas.cloudstorage.repository;

public record ContentMetadata(String filename, Long size, ArtifactType type) {
}
