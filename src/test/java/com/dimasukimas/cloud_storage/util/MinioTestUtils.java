package com.dimasukimas.cloud_storage.util;

import io.minio.*;
import io.minio.messages.Item;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MinioTestUtils {

    public static Result<Item> getMockResult() {

        Item mockItem = mock(Item.class);
        Result<Item> mockResult = mock(Result.class);

        try {
            when(mockResult.get()).thenReturn(mockItem);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get mock result");
        }

        return mockResult;
    }

    public static void mockListObjects(
            MinioClient minioClient,
            String expectedBucket,
            String expectedPrefix,
            Iterable<Result<Item>> resultToReturn
    ) {
        when(minioClient.listObjects(argThat(args ->
                args != null &&
                        args.bucket().equals(expectedBucket) && args.prefix().equals(expectedPrefix)
        ))).thenReturn(resultToReturn);
    }

    public static Item getItem(Iterable<Result<Item>> results) {
        Item item = null;
        for (Result<Item> result : results) {
            try {
                item = result.get();
            } catch (Exception e) {
                throw new RuntimeException();
            }
        }

        return item;
    }



}
