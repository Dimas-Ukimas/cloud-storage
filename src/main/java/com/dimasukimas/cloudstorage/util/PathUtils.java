package com.dimasukimas.cloudstorage.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PathUtils {

    public static String extractPathToResourceWithoutRoot(String path, String rootDirRegex, String splitter) {
        String truncatedPath = truncateEndSplitter(truncateRootDirectory(path, rootDirRegex), splitter);

        return getPathUntilLastSplitter(truncatedPath, splitter);
    }

    public static String extractPathToResource(String path, String splitter) {
        String truncatedPath = truncateEndSplitter(path, splitter);

        return getPathUntilLastSplitter(truncatedPath, splitter);
    }

    public static String extractResourceName(String path, String splitter) {
        int lastSplitterIndex = path.endsWith(splitter)
                ? truncateEndSplitter(path, splitter).lastIndexOf(splitter)
                : path.lastIndexOf(splitter);

        return lastSplitterIndex == -1
                ? path
                : path.substring(lastSplitterIndex + 1);
    }

    public static List<String> extractSubdirectoriesFromPath(String path, String splitter) {
        checkIsPathExist(path);
        int lastSplitterIndex = path.lastIndexOf(splitter);
        boolean hasSubdirectories = lastSplitterIndex > -1;

        if (hasSubdirectories) {
            String pathWithoutFileName = lastSplitterIndex == path.length() - 1 ? path : path.substring(0, lastSplitterIndex);
            String[] parts = pathWithoutFileName.split(splitter);
            List<String> subdirectories = new ArrayList<>();
            StringBuilder previousSubdirectory = new StringBuilder();

            for (String part : parts) {
                StringBuilder currentSubdirectory = previousSubdirectory.append(part).append(splitter);
                subdirectories.add(currentSubdirectory.toString());
            }

            return subdirectories;
        }

        return Collections.emptyList();
    }

    public static String createUserRootDirectoryName(Long userId, String pattern) {
        return String.format(pattern, userId);
    }

    private static String truncateEndSplitter(String path, String splitter) {
        return path.endsWith(splitter)
                ? path.substring(0, path.length() - 1)
                : path;
    }

    private static String truncateRootDirectory(String path, String rootRegex) {
        return path.replaceFirst(rootRegex, "");
    }

    private static String getPathUntilLastSplitter(String path, String splitter) {
        int lastSplitterIndex = path.lastIndexOf(splitter);

        return lastSplitterIndex == -1 ? "" : path.substring(0, lastSplitterIndex) + splitter;
    }

    //TODO: написать кастомное исключение
    private static void checkIsPathExist(String path) {
        if (path == null || path.isBlank()) {
            throw new RuntimeException("Path is not exist");
        }
    }

}
