package com.dimasukimas.cloudstorage.service;

import com.dimasukimas.cloudstorage.config.StorageProperties;
import com.dimasukimas.cloudstorage.dto.ResourceInfoResponseDto;
import com.dimasukimas.cloudstorage.exception.*;
import com.dimasukimas.cloudstorage.mapper.ResourceInfoMapper;
import com.dimasukimas.cloudstorage.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioResourceService implements ResourceService {

    private final StorageRepository repository;
    private final ResourceInfoMapper mapper;
    private final StorageProperties storageProperties;
    private final PathService pathService;

    @Override
    public ResourceInfoResponseDto createDirectory(Long userId, String path) {
        String fullPath = pathService.addRootDirBefore(userId, path);

        checkResourceNotExists(fullPath);
        checkParentDirectoriesExist(fullPath);

        log.info("event=directory_creation, status=completed, path={}", path);
        return mapper.toResDto(repository.putEmptyObject(fullPath), getResourceType(fullPath));
    }

    @Override
    public List<ResourceInfoResponseDto> getDirectoryContentInfo(Long userId, String path) {
        String fullPath = pathService.addRootDirBefore(userId, path);
        checkResourceExists(fullPath);

        log.info("event=get_directory, status=completed,  path={}", path);
        return repository.listChildren(fullPath)
                .stream()
                .map(object -> mapper.toResDto(object, getResourceType(object.key())))
                .toList();
    }

    @Override
    public ResourceInfoResponseDto getResourceInfo(Long userId, String path) {
        String fullPath = pathService.addRootDirBefore(userId, path);
        StorageObjectInfo object = repository.findObject(fullPath).orElseThrow(() -> new ResourceNotFoundException("Resource does not exists"));

        log.info("event=get_resource, status=completed, path={}", path);
        return mapper.toResDto(object, getResourceType(fullPath));
    }

    @Override
    public List<ResourceInfoResponseDto> search(Long userId, String query) {
        String rootDirectory = pathService.getUserRootDirectoryName(userId);
        List<StorageObjectInfo> objects = repository.listRecursive(rootDirectory);

        List<ResourceInfoResponseDto> resourcesInfo = new ArrayList<>();
        for (StorageObjectInfo object : objects) {
            String lowerResourceName = pathService.extractResourceName(object.key()).toLowerCase();
            if (lowerResourceName.contains(query.toLowerCase()))
                resourcesInfo.add(mapper.toResDto(object, getResourceType(object.key())));
        }

        log.info("event=search_resource, status=completed, query={}", query);
        return resourcesInfo;
    }

    @Override
    public List<ResourceInfoResponseDto> upload(Long userId, String path, List<MultipartFile> files) {
        String fullPath = pathService.addRootDirBefore(userId, path);
        checkResourcesNotExist(fullPath, files);
        List<ResourceInfoResponseDto> uploadedFiles = new ArrayList<>();

        for (MultipartFile file : files) {
            String filePath = fullPath + file.getOriginalFilename();
            pathService.extractSubdirectories(filePath)
                    .forEach(this::createSubdirectoryIfAbsent);
            try (InputStream content = file.getInputStream()) {
                StorageObjectInfo object = repository.putObject(filePath, content, file.getSize());
                uploadedFiles.add(mapper.toResDto(object, getResourceType(filePath)));
            } catch (IOException e) {
                throw new FileProcessingException("Failed to read uploaded files");
            }
        }

        log.info("event=upload, status=completed, path={}, fileCount={}, totalBytes={}",
                path, files.size(), files.stream().mapToLong(MultipartFile::getSize).sum());
        return uploadedFiles;
    }

    @Override
    public ContentSource download(Long userId, String path) {
        String fullPath = pathService.addRootDirBefore(userId, path);
        checkResourceExists(fullPath);

        String rootPath = pathService.extractPathToResource(fullPath);
        if (isDir(fullPath)) {
            List<StorageObjectInfo> resources = repository.listRecursive(fullPath);
            resources.sort(Comparator.comparing((StorageObjectInfo res) -> !isDir(res.key()))
                    .thenComparing(StorageObjectInfo::key));

            List<ZipEntrySpec> zipEntries = new ArrayList<>();

            for (StorageObjectInfo resource : resources) {
                ZipEntrySpec zipEntrySpec;
                if (isDir(resource.key())) {
                    zipEntrySpec = mapper.toZipEntrySpec(resource, rootPath, null, getResourceType(resource.key()));
                    zipEntries.add(zipEntrySpec);
                    continue;
                }
                Supplier<InputStream> content = repository.getObjectStream(resource.key());
                zipEntrySpec = mapper.toZipEntrySpec(resource, rootPath, content, getResourceType(resource.key()));
                zipEntries.add(zipEntrySpec);
            }
            String zipName = pathService.truncateEndSplitterIfPresent(pathService.extractResourceName(path));

            log.info("event=download_directory, status=completed, path={} fileCount={} totalBytes={}",
                    path, zipEntries.size(), zipEntries.stream().mapToLong(ZipEntrySpec::size).sum());
            return new ZipContentSource(zipName, zipEntries);
        }
        ResourceInfoResponseDto resourceMetadata = mapper.toResDto(checkResourceExists(fullPath), getResourceType(fullPath));
        Supplier<InputStream> contentSource = repository.getObjectStream(fullPath);

        log.info("event=download_file, status=completed, path={} fileCount={} totalBytes={}",
                path, 1, resourceMetadata.size());
        return new ObjectContentSource(contentSource, resourceMetadata);
    }

    @Override
    public ResourceInfoResponseDto moveOrRename(Long userId, String from, String to) {
        String fromPath = pathService.addRootDirBefore(userId, from);
        String toPath = pathService.addRootDirBefore(userId, to);
        checkResourceExists(fromPath);

        if (isRename(fromPath, toPath)) {
            toPath = addOrRemoveEndSplitterIfNeeded(fromPath, toPath);
        } else checkForbiddenMove(fromPath, toPath);

        checkResourceNotExists(toPath);

        if (isDir(fromPath)) {
            List<StorageObjectInfo> objects = repository.listRecursive(fromPath);
            for (StorageObjectInfo object : objects) {
                repository.copyObject(object.key(), toPath + pathService.extractPathWithoutPrefix(object.key(), fromPath));
            }
            repository.removeObjects(fromPath);
            log.info("event=move_directory status=completed from={} to={} movedObjects={}", from, to, objects.size());
        } else {
            repository.copyObject(fromPath, toPath);
            repository.removeObject(fromPath);
            log.info("event=move_file status=completed from={} to={}", from, to);
        }

        return getResourceInfo(userId, pathService.truncateRootDirectory(toPath));
    }

    @Override
    public void delete(Long userId, String path) {
        String fullPath = pathService.addRootDirBefore(userId, path);
        checkResourceExists(fullPath);

        if (isDir(fullPath)) {
            repository.removeObjects(fullPath);
            log.info("event=delete_directory status=completed path={}", path);
        } else repository.removeObject(fullPath);
        log.info("event=delete_file status=completed path={}", path);
    }

    private void createSubdirectoryIfAbsent(String path) {
        if (!isResourceExists(path)) {
            repository.putEmptyObject(path);
        }
    }

    private boolean isResourceExists(String path) {
        return repository.isObjectExists(path);
    }

    private StorageObjectInfo checkResourceExists(String path) {

        return repository.findObject(path).orElseThrow(() -> new ResourceNotFoundException("Resource does not exists"));
    }

    private void checkResourceNotExists(String path) {
        if (repository.isObjectExists(path)) {
            throw new ResourceAlreadyExistsException("Resource is already exists");
        }
    }

    private void checkResourcesNotExist(String path, List<MultipartFile> files) {
        List<String> existingFiles = new ArrayList<>();

        for (MultipartFile file : files) {
            String fullPath = path + file.getOriginalFilename();
            if (repository.isObjectExists(fullPath)) {
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

        if (!repository.isObjectExists(parentPath)) {
            throw new ParentDirectoryNotExistsException("Parent directory does not exists");
        }
    }

    private void checkForbiddenMove(String from, String to) {
        boolean isDirToFileMove = isDir(from) && !isDir(to);
        boolean isMoveIntoSelf = to.startsWith(from);

        if (isDirToFileMove || isMoveIntoSelf) {
            throw new ForbiddenMovementException("Cannot move directory to file");
        }
    }

    private boolean isDir(String path) {
        return path.endsWith(storageProperties.getDirectorySplitter());
    }

    private ResourceType getResourceType(String path) {
        return isDir(path) ? ResourceType.DIRECTORY : ResourceType.FILE;
    }

    private boolean isRename(String from, String to) {
        String fromParentPath = pathService.extractPathToResource(from);
        String toParentPath = pathService.extractPathToResource(to);

        return fromParentPath.equals(toParentPath);
    }

    private String addOrRemoveEndSplitterIfNeeded(String fromPath, String toPath) {
        boolean isDirToFileRename = isDir(fromPath) && !isDir(toPath);
        boolean isFileToDirRename = !isDir(fromPath) && isDir(toPath);

        if (isDirToFileRename) {
            toPath = pathService.addSplitterToEnd(toPath);
        }
        if (isFileToDirRename) {
            toPath = pathService.truncateEndSplitterIfPresent(toPath);
        }

        return toPath;
    }
}



