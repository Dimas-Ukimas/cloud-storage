package com.dimasukimas.cloudstorage.controller;

import com.dimasukimas.cloudstorage.dto.ResourceInfoResponseDto;
import com.dimasukimas.cloudstorage.repository.ArtifactType;
import com.dimasukimas.cloudstorage.repository.ContentSource;
import com.dimasukimas.cloudstorage.security.CustomUserDetails;
import com.dimasukimas.cloudstorage.service.ResourceService;
import com.dimasukimas.cloudstorage.swagger.storage.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/resource")
@RequiredArgsConstructor
@Tag(name = "Resources")
public class ResourceController {

    private final ResourceService resourceService;

    @GetMapping
    @GetResourceDocs
    public ResponseEntity<ResourceInfoResponseDto> getResourceInfo(@RequestParam String path,
                                                                   @AuthenticationPrincipal CustomUserDetails user) {
        ResourceInfoResponseDto resInfo = resourceService.getResourceInfo(user.id(), path);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(resInfo);
    }

    @PostMapping
    @UploadResourceDocs
    public ResponseEntity<List<ResourceInfoResponseDto>> upload(@AuthenticationPrincipal CustomUserDetails user,
                                                                @RequestParam String path,
                                                                @RequestPart("object") List<MultipartFile> files) {
        List<ResourceInfoResponseDto> resInfo = resourceService.upload(user.id(), path, files);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(resInfo);
    }

    @DeleteMapping
    @DeleteResourceDocs
    public ResponseEntity<Void> delete(@AuthenticationPrincipal CustomUserDetails user,
                                       @RequestParam String path) {
        resourceService.delete(user.id(), path);

        return ResponseEntity
                .noContent()
                .build();
    }

    @GetMapping("/download")
    @DownloadResourceDocs
    public ResponseEntity<StreamingResponseBody> download(@AuthenticationPrincipal CustomUserDetails user,
                                                          @RequestParam String path) {

        ContentSource contentSource = resourceService.download(user.id(), path);
        StreamingResponseBody body = contentSource::writeTo;

        ContentDisposition contentDisposition = ContentDisposition
                .attachment()
                .filename(contentSource.getMetadata().filename(), StandardCharsets.UTF_8)
                .build();

        ResponseEntity.BodyBuilder builder = ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .headers(h -> h.setContentDisposition(contentDisposition));

        if (contentSource.getMetadata().type().equals(ArtifactType.FILE)) {
            builder.contentLength(contentSource.getMetadata().size());
        }

        return builder.body(body);
    }

    @GetMapping("/move")
    @MoveResourceDocs
    public ResponseEntity<ResourceInfoResponseDto> move(@AuthenticationPrincipal CustomUserDetails user,
                                                        @RequestParam String from,
                                                        @RequestParam String to) {
        ResourceInfoResponseDto resInfo = resourceService.moveOrRename(user.id(), from, to);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(resInfo);
    }

    @GetMapping("/search")
    @SearchResourcesDocs
    public ResponseEntity<List<ResourceInfoResponseDto>> search(@AuthenticationPrincipal CustomUserDetails user,
                                                                @RequestParam String query
    ) {
        List<ResourceInfoResponseDto> resourcesInfo = resourceService.search(user.id(), query);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(resourcesInfo);
    }

}
