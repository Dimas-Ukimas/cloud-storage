package com.dimasukimas.cloudstorage.repository;

import com.dimasukimas.cloudstorage.dto.ResourceInfoDto;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class ObjectContentSource implements ContentSource {

    private final Supplier<InputStream> contentSource;
    private final ResourceInfoDto metadata;
    AtomicBoolean isUsed = new AtomicBoolean(false);

    @Override
    public void writeTo(OutputStream out) throws IOException {
        if (isUsed.compareAndSet(false, true)) {
            try (InputStream in = contentSource.get()) {
                in.transferTo(out);
            }
        }
    }

    @Override
    public ContentMetadata getMetadata() {
        return new ContentMetadata(metadata.name(), metadata.size(), ArtifactType.FILE);
    }
}
