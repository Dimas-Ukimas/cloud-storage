package com.dimasukimas.cloudstorage.controller;

import com.dimasukimas.cloudstorage.dto.UsernameRequestDto;
import com.dimasukimas.cloudstorage.mapper.UserMapper;
import com.dimasukimas.cloudstorage.security.CustomUserDetails;
import com.dimasukimas.cloudstorage.swagger.auth.GetUserDocs;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "User")
public class UserController {

    private final UserMapper userMapper;

    @GetMapping
    @GetUserDocs
    public ResponseEntity<UsernameRequestDto> getCurrentUser(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userMapper.toUserDto(user));
    }
}
