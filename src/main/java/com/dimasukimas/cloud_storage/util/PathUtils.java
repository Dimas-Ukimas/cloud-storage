package com.dimasukimas.cloud_storage.util;

public class PathUtils {

    private static final String DIRECTORY_SPLITTER = "/";

    public static String extractParentPathToResource(String path) {
        String normalizedPath = normalizePath(path);
        int lastSplitterIndex = normalizedPath.lastIndexOf(DIRECTORY_SPLITTER);

        return lastSplitterIndex > 0
                ? normalizedPath.substring(0, lastSplitterIndex + 1)
                : "";
    }

    public static String extractResourceName(String path) {
        String normalizedPath = normalizePath(path);
        int lastSplitterIndex = normalizedPath.lastIndexOf(DIRECTORY_SPLITTER);
        String name = lastSplitterIndex != -1 ? normalizedPath.substring(lastSplitterIndex + 1) : normalizedPath;

        return name;
    }

    public static String createUserDirectoryName(Long userId){
        return String.format("user-%d-files/", userId);
    }

    private static String normalizePath(String path) {
        String pathWithoutRootDir = path.replaceFirst("^user-\\d+-files/", "");

        return path.endsWith(DIRECTORY_SPLITTER)
                ? pathWithoutRootDir.substring(0, path.length() - 1)
                : pathWithoutRootDir;
    }

}
