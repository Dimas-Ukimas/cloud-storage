package com.dimasukimas.cloudstorage.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

@ConfigurationProperties(prefix = "spring.storage")
@Getter
@Setter
public class StorageProperties {

    private String userRootDirectoryPattern;
    private String userRootDirectoryRegex;
    private String directorySplitter;
    private DataSize fileMaxSize;
}
