package com.dimasukimas.cloud_storage.controller;

import com.dimasukimas.cloud_storage.dto.CustomUserDetails;
import com.dimasukimas.cloud_storage.dto.UsernameDto;
import com.dimasukimas.cloud_storage.mapper.UserMapper;
import com.dimasukimas.cloud_storage.swagger.GetUserDocs;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/me")
@RequiredArgsConstructor
public class UserController {

    private final UserMapper userMapper;

    @GetMapping
    @GetUserDocs
    public ResponseEntity<UsernameDto> getCurrentUser(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userMapper.toUserDto(user));
    }
}
