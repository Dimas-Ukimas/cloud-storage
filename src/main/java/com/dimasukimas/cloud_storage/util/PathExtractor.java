package com.dimasukimas.cloud_storage.util;

import java.util.Optional;

public class PathExtractor {

    private static final String DIRECTORY_SPLITTER = "/";

    public static String extractParentPath(String path) {
        String normalizedPath = normalizePath(path);
        int lastSplitterIndex = normalizedPath.lastIndexOf(DIRECTORY_SPLITTER);

        return lastSplitterIndex > 0
                ? normalizedPath.substring(0, lastSplitterIndex + 1)
                : "";
    }

    public static String extractFileName(String path) {
        String normalizedPath = normalizePath(path);
        int lastSplitterIndex = normalizedPath.lastIndexOf(DIRECTORY_SPLITTER);
        String name = lastSplitterIndex != -1 ? normalizedPath.substring(lastSplitterIndex + 1) : normalizedPath;

        return path.endsWith(DIRECTORY_SPLITTER)
                ? name + DIRECTORY_SPLITTER
                : name;
    }

    private static String normalizePath(String path) {
        return path.endsWith(DIRECTORY_SPLITTER)
                ? path.substring(0, path.length() - 1)
                : path;
    }

}
