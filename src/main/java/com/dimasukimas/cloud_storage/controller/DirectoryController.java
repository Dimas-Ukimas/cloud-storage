package com.dimasukimas.cloud_storage.controller;

import com.dimasukimas.cloud_storage.dto.CustomUserDetails;
import com.dimasukimas.cloud_storage.dto.DirectoryInfoDto;
import com.dimasukimas.cloud_storage.service.ResourceManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/directory")
@RequiredArgsConstructor
public class DirectoryController {

    private final ResourceManagerService resourceManagerService;

    @PostMapping
    public ResponseEntity<DirectoryInfoDto> createDirectory(@RequestParam String path,
                                                            @AuthenticationPrincipal CustomUserDetails user) {
        DirectoryInfoDto directoryInfo = resourceManagerService.createDirectory(user.id(), path);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(directoryInfo);
    }

}
