package com.dimasukimas.cloudstorage.util.assertion;

import com.dimasukimas.cloudstorage.exception.ResourceNotFoundException;
import com.dimasukimas.cloudstorage.helper.MinioTestHelper;
import io.minio.StatObjectResponse;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class MinioAssert {

    private final MinioTestHelper minioHelper;

    public MinioAssert(MinioTestHelper minioHelper) {
        this.minioHelper = minioHelper;
    }

    public static MinioAssert create(MinioTestHelper minioHelper) {
        return new MinioAssert(minioHelper);
    }

    public MinioAssert assertResourceExist(String objectName) {
        StatObjectResponse objectStat = minioHelper.findObject(objectName).orElseThrow(() -> new ResourceNotFoundException("Test resource " + objectName + " does not exists"));
        assertThat(objectStat.object()).isEqualTo(objectName);
        return this;
    }

    public MinioAssert assertResourceNotExists(String objectName) {
        Optional<StatObjectResponse> objectStat = minioHelper.findObject(objectName);
        assertThat(objectStat).isEmpty();
        return this;
    }

    public MinioAssert assertNoDuplicates(String objectName) {
        List<String> files = minioHelper.listAll(objectName);
        assertThat(files).containsOnlyOnce(objectName);

        return this;
    }

}
