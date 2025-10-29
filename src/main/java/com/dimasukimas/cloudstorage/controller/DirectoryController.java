package com.dimasukimas.cloudstorage.controller;

import com.dimasukimas.cloudstorage.dto.ResourceInfoResponseDto;
import com.dimasukimas.cloudstorage.security.CustomUserDetails;
import com.dimasukimas.cloudstorage.service.ResourceService;
import com.dimasukimas.cloudstorage.swagger.storage.CreateDirectoryDocs;
import com.dimasukimas.cloudstorage.swagger.storage.GetDirectoryContentDocs;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/directory")
@RequiredArgsConstructor
@Tag(name = "Directories")
public class DirectoryController {

    private final ResourceService resourceService;

    @PostMapping
    @CreateDirectoryDocs
    public ResponseEntity<ResourceInfoResponseDto> createDirectory(@RequestParam String path,
                                                                   @AuthenticationPrincipal CustomUserDetails user) {
        ResourceInfoResponseDto directoryInfo = resourceService.createDirectory(user.id(), path);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(directoryInfo);
    }

    @GetMapping
    @GetDirectoryContentDocs
    public ResponseEntity<List<ResourceInfoResponseDto>> getDirectoryContentInfo(@RequestParam String path,
                                                                                 @AuthenticationPrincipal CustomUserDetails user
    ) {
        List<ResourceInfoResponseDto> directoryContentInfo = resourceService.getDirectoryContentInfo(user.id(), path);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(directoryContentInfo);
    }


}

