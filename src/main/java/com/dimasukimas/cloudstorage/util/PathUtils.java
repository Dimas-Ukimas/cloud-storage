package com.dimasukimas.cloudstorage.util;

public class PathUtils {

    private static final String DIRECTORY_SPLITTER = "/";
    private static final String USER_ROOT_DIR_REGEX = "^user-\\d+-files/";

    public static String extractPathToResource(String path) {
        String truncatedPath = truncateEndSplitter(truncateRootDirectory(path));

        return getPathUntilLastSplitter(truncatedPath);
    }

    public static String extractResourceName(String path) {
        int lastSplitterIndex = path.endsWith(DIRECTORY_SPLITTER)
                ? truncateEndSplitter(path).lastIndexOf(DIRECTORY_SPLITTER)
                : path.lastIndexOf(DIRECTORY_SPLITTER);

        return lastSplitterIndex == -1
                ? path
                : path.substring(lastSplitterIndex + 1);
    }

    public static String createUserRootDirectoryName(Long userId) {

        return String.format("user-%d-files/", userId);
    }

    private static String truncateEndSplitter(String path) {

        return path.endsWith(DIRECTORY_SPLITTER)
                ? path.substring(0, path.length() - 1)
                : path;
    }

    private static String truncateRootDirectory(String path) {

        return path.replaceFirst(USER_ROOT_DIR_REGEX, "");
    }

    private static String getPathUntilLastSplitter(String path) {
        int lastSplitterIndex = path.lastIndexOf(DIRECTORY_SPLITTER);

        return lastSplitterIndex == -1 ? "" : path.substring(0, lastSplitterIndex);
    }

}
