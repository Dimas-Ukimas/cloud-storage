package com.dimasukimas.cloudstorage.controller;

import com.dimasukimas.cloudstorage.dto.CustomUserDetails;
import com.dimasukimas.cloudstorage.dto.ResourceInfoDto;
import com.dimasukimas.cloudstorage.service.ResourceManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/resource")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceManagerService resourceManagerService;

    @GetMapping
    public ResponseEntity<ResourceInfoDto> getResourceInfo(@RequestParam String path,
                                                           @AuthenticationPrincipal CustomUserDetails user) {
        ResourceInfoDto body = resourceManagerService.getResourceInfo(user.id(), path);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(body);
    }
}
