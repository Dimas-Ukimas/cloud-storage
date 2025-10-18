package com.dimasukimas.cloudstorage.controller;

import com.dimasukimas.cloudstorage.dto.CustomUserDetails;
import com.dimasukimas.cloudstorage.dto.ResourceInfoDto;
import com.dimasukimas.cloudstorage.repository.ArtifactType;
import com.dimasukimas.cloudstorage.repository.ContentSource;
import com.dimasukimas.cloudstorage.service.ResourceManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/resource")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceManagerService resourceManagerService;

    @GetMapping
    public ResponseEntity<ResourceInfoDto> getResourceInfo(@RequestParam String path,
                                                           @AuthenticationPrincipal CustomUserDetails user) {
        ResourceInfoDto resInfo = resourceManagerService.getResourceInfo(user.id(), path);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(resInfo);
    }

    @PostMapping
    public ResponseEntity<List<ResourceInfoDto>> upload(@AuthenticationPrincipal CustomUserDetails user,
                                                        @RequestParam String path,
                                                        @RequestParam("files") List<MultipartFile> files) {
        List<ResourceInfoDto> resInfo = resourceManagerService.upload(user.id(), path, files);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(resInfo);
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@AuthenticationPrincipal CustomUserDetails user,
                                       @RequestParam String path) {
        resourceManagerService.delete(user.id(), path);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/download")
    public ResponseEntity<StreamingResponseBody> download(@AuthenticationPrincipal CustomUserDetails user,
                                                          @RequestParam String path) {

        ContentSource contentSource = resourceManagerService.download(user.id(), path);
        StreamingResponseBody body = contentSource::writeTo;

        ResponseEntity.BodyBuilder builder = ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, buildContentDispositionHeader(contentSource.getMetadata().filename()));

        if (contentSource.getMetadata().type().equals(ArtifactType.FILE)) {
            builder.contentLength(contentSource.getMetadata().size());
        }

        return builder.body(body);
    }

    private String buildContentDispositionHeader(String filename) {
        return "attachment; filename=\"" + filename + "\"; filename*=UTF-8''" +
                URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
    }

}
