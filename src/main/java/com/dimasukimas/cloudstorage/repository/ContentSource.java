package com.dimasukimas.cloudstorage.repository;

import java.io.IOException;
import java.io.OutputStream;

public interface ContentSource {

    void writeTo(OutputStream out) throws IOException;

    ContentMetadata getMetadata();

}
