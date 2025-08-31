package com.dimasukimas.cloud_storage.util.assertion;

import com.dimasukimas.cloud_storage.helper.MinioTestHelper;
import io.minio.StatObjectResponse;

import static org.assertj.core.api.Assertions.assertThat;

public class MinioAssert {

    private final MinioTestHelper minioHelper;

    public MinioAssert(MinioTestHelper minioHelper) {
        this.minioHelper = minioHelper;
    }

    public static MinioAssert create(MinioTestHelper minioHelper){
        return new MinioAssert(minioHelper);
    }

    public MinioAssert assertUserMinioRootDirectoryCreated(String objectName) {
        StatObjectResponse objectStat = minioHelper.findObject(objectName).orElseThrow();
        assertThat(objectStat.object()).isEqualTo(objectName);
        return this;
    }
}
