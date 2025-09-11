package com.dimasukimas.cloudstorage.util.assertion;


public class MinioAuthAssert extends AuthAssert<MinioAuthAssert> {

    private final MinioAssert minioAssert;

    private MinioAuthAssert(RedisAssert redisAssert, UserAssert userAssert, HttpAssert httpAssert, MinioAssert minioAssert) {
        super(redisAssert, userAssert, httpAssert);
        this.minioAssert = minioAssert;
    }

    public static MinioAuthAssert create(RedisAssert redisAssert, UserAssert userAssert, HttpAssert httpAssert, MinioAssert minioAssert) {
        return new MinioAuthAssert(redisAssert, userAssert, httpAssert, minioAssert);
    }

    public MinioAuthAssert assertUserMinioRootDirectoryCreated(String objectName) {
        minioAssert.assertDirectoryExist(objectName);
        return self();
    }

}
