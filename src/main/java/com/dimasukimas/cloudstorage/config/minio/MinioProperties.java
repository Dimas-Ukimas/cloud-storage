package com.dimasukimas.cloudstorage.config.minio;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

@ConfigurationProperties(prefix = "spring.minio")
@Getter
@Setter
public class MinioProperties {

    private String bucketName;
    private String url;
    private String accessKey;
    private String secretKey;
    private String userRootDirectoryPattern;
    private String userRootDirectoryRegex;
    private String directorySplitter;
    private DataSize fileMaxSize;
}
