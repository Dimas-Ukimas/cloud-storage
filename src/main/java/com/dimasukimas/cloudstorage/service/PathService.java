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

    //TODO проверить, нужно ли возвращать имена папок с / на конце для фронта
    public String extractResourceName(String path) {
        int lastSplitterIndex = isDir(path)
                ? truncateEndSplitterIfPresent(path).lastIndexOf(storageProperties.getDirectorySplitter())
                : path.lastIndexOf(storageProperties.getDirectorySplitter());

        return lastSplitterIndex == -1
                ? path
                : path.substring(lastSplitterIndex + 1);
    }

    public List<String> extractSubdirectories(String path) {
        checkIsPathExist(path);
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
        return isDir(path)
                ? path.substring(0, path.length() - 1)
                : path;
    }

    private String truncateRootDirectory(String path) {
        return path.replaceFirst(storageProperties.getUserRootDirectoryRegex(), "");
    }

    private String getPathUntilLastSplitter(String path) {
        int lastSplitterIndex = path.lastIndexOf(storageProperties.getDirectorySplitter());

        return lastSplitterIndex == -1 ? "" : path.substring(0, lastSplitterIndex) + storageProperties.getDirectorySplitter();
    }

    //TODO: написать кастомное исключение
    private void checkIsPathExist(String path) {
        if (path == null || path.isBlank()) {
            throw new RuntimeException("Path is not exist");
        }
    }

    public boolean isDir(String path) {
        return path.endsWith(storageProperties.getDirectorySplitter());
    }

    public String addRootDirBefore(Long userId, String path) {
        return String.format(storageProperties.getUserRootDirectoryPattern(), userId) + path;
    }

}
