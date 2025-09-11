package com.dimasukimas.cloudstorage.controller;

import com.dimasukimas.cloudstorage.dto.CustomUserDetails;
import com.dimasukimas.cloudstorage.dto.ResourceInfoDto;
import com.dimasukimas.cloudstorage.service.ResourceManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/directory")
@RequiredArgsConstructor
public class DirectoryController {

    private final ResourceManagerService resourceManagerService;

    @PostMapping
    public ResponseEntity<ResourceInfoDto> createDirectory(@RequestParam String path,
                                                            @AuthenticationPrincipal CustomUserDetails user) {
        ResourceInfoDto directoryInfo = resourceManagerService.createDirectory(user.id(), path);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(directoryInfo);
    }

    @GetMapping
    public ResponseEntity<List<ResourceInfoDto>> getDirectoryContentInfo(@RequestParam String path,
                                                                         @AuthenticationPrincipal CustomUserDetails user
                                                                   ){
        List<ResourceInfoDto> directoryContentInfo = resourceManagerService.getDirectoryContentInfo(user.id(), path);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(directoryContentInfo);
    }


}
