package com.dimasukimas.cloudstorage.model.storage;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RequiredArgsConstructor
public class ZipContentSource implements ContentSource {

    private final String filename;
    private final List<ZipEntrySpec> zipEntriesSpecs;
    AtomicBoolean isUsed = new AtomicBoolean(false);

    @Override
    public void writeTo(OutputStream out) throws IOException {
        if (isUsed.compareAndSet(false, true)) {
            ZipOutputStream zip = new ZipOutputStream(out);

            for (ZipEntrySpec zipEntrySpec : zipEntriesSpecs) {
                ZipEntry zipEntry = new ZipEntry(zipEntrySpec.relativePath());
                if (zipEntry.isDirectory()) {
                    zip.putNextEntry(zipEntry);
                    zip.closeEntry();
                    continue;
                }
                try (InputStream in = zipEntrySpec.content().get()) {
                    zip.putNextEntry(zipEntry);
                    in.transferTo(zip);
                    zip.closeEntry();
                }
            }
            zip.finish();
        }
    }

    @Override
    public ContentMetadata getMetadata() {
        return new ContentMetadata(filename + ".zip", null, ArtifactType.ARCHIVE);
    }
}
