package com.dimasukimas.cloudstorage.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.storage")
@Getter
@Setter
public class StorageProperties {

    private String userRootDirectoryPattern;
    private String userRootDirectoryRegex;
    private String directorySplitter;
    private int maxPathLength;
}
