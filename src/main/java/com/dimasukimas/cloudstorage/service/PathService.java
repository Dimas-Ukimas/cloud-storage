package com.dimasukimas.cloudstorage.service;

import com.dimasukimas.cloudstorage.config.StorageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PathService {

    private final StorageProperties storageProperties;

    public String extractRootlessPathToResource(String path) {
        String rootlessPath = truncateRootDirectory(path);
        return extractPathToResource(rootlessPath);
    }

    public String extractPathToResource(String path) {
        String truncatedPath = truncateEndSplitterIfPresent(path);

        return getPathUntilLastSplitter(truncatedPath);
    }

    public String extractRelativePath(String path, String rootPath) {
        return path.length() - rootPath.length() > 0 ? path.substring(rootPath.length()) : extractResourceName(path);
    }

    public String extractPathWithoutPrefix(String path, String prefix) {
        return path.length() == prefix.length() ? "" : path.substring(prefix.length());
    }

    public String extractResourceName(String path) {
        int lastSplitterIndex = path.endsWith(storageProperties.getDirectorySplitter())
                ? truncateEndSplitterIfPresent(path).lastIndexOf(storageProperties.getDirectorySplitter())
                : path.lastIndexOf(storageProperties.getDirectorySplitter());

        return lastSplitterIndex == -1
                ? path
                : path.substring(lastSplitterIndex + 1);
    }

    public List<String> extractSubdirectories(String path) {
        int lastSplitterIndex = path.lastIndexOf(storageProperties.getDirectorySplitter());
        boolean hasSubdirectories = lastSplitterIndex > -1;

        if (hasSubdirectories) {
            String pathWithoutFileName = lastSplitterIndex == path.length() - 1 ? path : path.substring(0, lastSplitterIndex);
            String[] parts = pathWithoutFileName.split(storageProperties.getDirectorySplitter());
            List<String> subdirectories = new ArrayList<>();
            StringBuilder previousSubdirectory = new StringBuilder();

            for (String part : parts) {
                StringBuilder currentSubdirectory = previousSubdirectory.append(part).append(storageProperties.getDirectorySplitter());
                subdirectories.add(currentSubdirectory.toString());
            }

            return subdirectories;
        }

        return Collections.emptyList();
    }

    public String getUserRootDirectoryName(Long userId) {
        return String.format(storageProperties.getUserRootDirectoryPattern(), userId);
    }

    public String truncateEndSplitterIfPresent(String path) {
        return path.endsWith(storageProperties.getDirectorySplitter())
                ? path.substring(0, path.length() - 1)
                : path;
    }

    public String truncateRootDirectory(String path) {
        return path.replaceFirst(storageProperties.getUserRootDirectoryRegex(), "");
    }

    private String getPathUntilLastSplitter(String path) {
        int lastSplitterIndex = path.lastIndexOf(storageProperties.getDirectorySplitter());

        return lastSplitterIndex == -1 ? "" : path.substring(0, lastSplitterIndex) + storageProperties.getDirectorySplitter();
    }

    public String addRootDirBefore(Long userId, String path) {
        String rootDirectory = String.format(storageProperties.getUserRootDirectoryPattern(), userId);

        return rootDirectory + path;
    }

    public String addSplitterToEnd(String path) {
        return path + storageProperties.getDirectorySplitter();
    }

}
