package com.dimasukimas.cloudstorage.controller;

import com.dimasukimas.cloudstorage.dto.CustomUserDetails;
import com.dimasukimas.cloudstorage.dto.ResourceInfoDto;
import com.dimasukimas.cloudstorage.service.ResourceManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    public ResponseEntity<ResourceInfoDto> upload(@AuthenticationPrincipal CustomUserDetails user,
                                                  @RequestParam String path,
                                                  @RequestParam("file") MultipartFile file) {
        ResourceInfoDto resInfo = resourceManagerService.upload(user.id(), path, file);

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

}
