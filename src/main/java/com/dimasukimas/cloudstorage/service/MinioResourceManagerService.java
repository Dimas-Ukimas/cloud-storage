package com.dimasukimas.cloudstorage.service;

import com.dimasukimas.cloudstorage.config.StorageProperties;
import com.dimasukimas.cloudstorage.dto.ResourceInfoDto;
import com.dimasukimas.cloudstorage.exception.*;
import com.dimasukimas.cloudstorage.mapper.ResourceInfoMapper;
import com.dimasukimas.cloudstorage.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class MinioResourceManagerService implements ResourceManagerService {

    private final StorageRepository repository;
    private final ResourceInfoMapper mapper;
    private final StorageProperties storageProperties;
    private final PathService pathService;

    @Override
    public ResourceInfoDto createDirectory(Long userId, String path) {
        String fullPath = pathService.addRootDirBefore(userId, path);

        checkResourceNotExists(fullPath);
        checkParentDirectoriesExist(fullPath);

        return mapper.toResDto(repository.createDirectory(fullPath));
    }

    @Override
    public List<ResourceInfoDto> getDirectoryContentInfo(Long userId, String path) {
        String fullPath = pathService.addRootDirBefore(userId, path);
        checkResourceExists(fullPath);

        return repository.getDirectoryContentInfo(fullPath)
                .stream()
                .map(mapper::toResDto)
                .toList();
    }

    @Override
    public ResourceInfoDto getResourceInfo(Long userId, String path) {
        String fullPath = pathService.addRootDirBefore(userId, path);

        ResourceMetadata object = repository.findResource(fullPath).orElseThrow(() -> new ResourceNotFoundException("Resource does not exists"));

        return mapper.toResDto(object);
    }

    @Override
    public List<ResourceInfoDto> upload(Long userId, String path, List<MultipartFile> files) {
        String fullPath = pathService.addRootDirBefore(userId, path);
        checkResourcesNotExist(fullPath, files);
        checkExceedingSizeFiles(files);
        List<ResourceInfoDto> uploadedFiles = new ArrayList<>();

        for (MultipartFile file : files) {
            String filePath = fullPath + file.getOriginalFilename();
            pathService.extractSubdirectories(filePath)
                    .forEach(this::createSubdirectoryIfAbsent);
            try (InputStream content = file.getInputStream()) {
                ResourceMetadata objectInfo = repository.upload(filePath, content, file.getSize());
                uploadedFiles.add(mapper.toResDto(objectInfo));
            } catch (IOException e) {
                throw new FileProcessingException("Failed to read uploaded files");
            }
        }
        return uploadedFiles;
    }

    @Override
    public ContentSource download(Long userId, String path) {
        String fullPath = pathService.addRootDirBefore(userId, path);
        String rootPath = pathService.extractPathToResource(fullPath);
        if (pathService.isDir(fullPath)) {
            List<ResourceMetadata> resourcesMetadata = repository.listAll(fullPath);
            resourcesMetadata.sort(Comparator.comparing((ResourceMetadata res) -> !res.isDir())
                    .thenComparing(ResourceMetadata::path));

            List<ZipEntrySpec> zipEntries = new ArrayList<>();

            for (ResourceMetadata resourceMetadata : resourcesMetadata) {
                ZipEntrySpec zipEntrySpec;
                if (resourceMetadata.isDir()) {
                    zipEntrySpec = mapper.toZipEntrySpec(resourceMetadata, rootPath, null);
                    zipEntries.add(zipEntrySpec);
                    continue;
                }
                Supplier<InputStream> content = repository.download(resourceMetadata.path());
                zipEntrySpec = mapper.toZipEntrySpec(resourceMetadata, rootPath, content);
                zipEntries.add(zipEntrySpec);
            }
            String zipName = pathService.truncateEndSplitterIfPresent(pathService.extractResourceName(path));
            return new ZipContentSource(zipName, zipEntries);
        }
        ResourceInfoDto resourceMetadata = mapper.toResDto(checkResourceExists(fullPath));
        Supplier<InputStream> contentSource = repository.download(fullPath);

        return new ObjectContentSource(contentSource, resourceMetadata);
    }

    @Override
    public void delete(Long userId, String path) {
        String fullPath = pathService.addRootDirBefore(userId, path);
        checkResourceExists(fullPath);

        if (pathService.isDir(fullPath)) {
            repository.deleteDirectory(path);
        } else repository.deleteFile(fullPath);
    }

    private void createSubdirectoryIfAbsent(String path) {
        if (!isResourceExists(path)) {
            repository.createDirectory(path);
        }
    }

    public boolean isResourceExists(String path) {
        return repository.isResourceExists(path);
    }

    private ResourceMetadata checkResourceExists(String path) {

        return repository.findResource(path).orElseThrow(() -> new ResourceNotFoundException("Resource does not exists"));
    }

    private void checkResourceNotExists(String path) {
        if (repository.isResourceExists(path)) {
            throw new ResourceAlreadyExistsException("Resource is already exists");
        }
    }

    private void checkResourcesNotExist(String path, List<MultipartFile> files) {
        List<String> existingFiles = new ArrayList<>();

        for (MultipartFile file : files) {
            String fullPath = path + file.getOriginalFilename();
            if (repository.isResourceExists(fullPath)) {
                existingFiles.add(file.getOriginalFilename());
            }
        }
        if (!existingFiles.isEmpty()) {
            throw new ResourceAlreadyExistsException("Resources are already exist: " + existingFiles);
        }
    }


    private void checkParentDirectoriesExist(String path) {
        String parentPath = pathService.extractPathToResource(path);

        if (parentPath.isEmpty()) {
            return;
        }

        if (!repository.isResourceExists(parentPath)) {
            throw new ParentDirectoryNotExistsException("Parent directory does not exists");
        }
    }

    private void checkExceedingSizeFiles(List<MultipartFile> files) {
        List<String> exceedingSizeFiles = new ArrayList<>();


        for (MultipartFile file : files) {
            if (file.getSize() > storageProperties.getFileMaxSize().toMegabytes()) {
                exceedingSizeFiles.add(file.getOriginalFilename());
            }
        }
        if (!exceedingSizeFiles.isEmpty()) {
            throw new FileMaxSizeExceedingException("Files cannot be uploaded due to max size exceeding: " + exceedingSizeFiles);
        }
    }

}



