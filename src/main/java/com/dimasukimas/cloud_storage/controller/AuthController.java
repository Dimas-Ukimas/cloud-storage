package com.dimasukimas.cloud_storage.controller;

import com.dimasukimas.cloud_storage.dto.AuthRequestDto;
import com.dimasukimas.cloud_storage.dto.AuthResponseDto;
import com.dimasukimas.cloud_storage.dto.UserDetailsImpl;
import com.dimasukimas.cloud_storage.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/sign-up")
    public ResponseEntity<AuthResponseDto> signUp(@Valid @RequestBody AuthRequestDto dto, HttpServletRequest request) {

        UserDetailsImpl registeredUser = userService.signUp(dto);

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        registeredUser,
                        null,
                        registeredUser.getAuthorities()
                );

        SecurityContextHolder.getContext().setAuthentication(authToken);

        request.getSession(true);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new AuthResponseDto(registeredUser.getUsername()));
    }

    @PostMapping("/sign-in")
    public ResponseEntity<AuthResponseDto> signIn(@Valid @RequestBody AuthRequestDto dto, HttpServletRequest request) {

        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(dto.username(), dto.password());

        Authentication authResult = authenticationManager.authenticate(authRequest);

        SecurityContextHolder.getContext().setAuthentication(authResult);

        UserDetails userDetails = (UserDetails) authResult.getPrincipal();
        AuthResponseDto response = new AuthResponseDto(userDetails.getUsername());

        request.getSession(true);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
