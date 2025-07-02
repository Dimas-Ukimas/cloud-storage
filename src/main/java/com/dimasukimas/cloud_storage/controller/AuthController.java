package com.dimasukimas.cloud_storage.controller;

import com.dimasukimas.cloud_storage.dto.AuthResponseDto;
import com.dimasukimas.cloud_storage.dto.SignInRequestDto;
import com.dimasukimas.cloud_storage.dto.SignUpRequestDto;
import com.dimasukimas.cloud_storage.dto.UserDetailsImpl;
import com.dimasukimas.cloud_storage.dto.AuthRequestDto;
import com.dimasukimas.cloud_storage.dto.UsernameDto;
import com.dimasukimas.cloud_storage.exception.UnauthorizedUserSignOutException;
import com.dimasukimas.cloud_storage.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
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

import java.util.Optional;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/sign-up")
    public ResponseEntity<AuthResponseDto> signUp(@Valid @RequestBody SignUpRequestDto dto, HttpServletRequest request) {
        UserDetailsImpl registeredUser = userService.signUp(dto);
    public ResponseEntity<UsernameDto> signUp(@Valid @RequestBody AuthRequestDto dto, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        registeredUser,
                        null,
                        registeredUser.getAuthorities()
                );

        SecurityContextHolder.getContext().setAuthentication(authToken);
        request.getSession(true);
        UsernameDto response = new UsernameDto(registeredUser.getUsername());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/sign-in")
    @SignInDocs
    public ResponseEntity<UsernameDto> signIn(@Valid @RequestBody AuthRequestDto dto, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(dto.username(), dto.password());
        Authentication authResult = authenticationManager.authenticate(authRequest);

        SecurityContextHolder.getContext().setAuthentication(authResult);
        UserDetails userDetails = (UserDetails) authResult.getPrincipal();
        AuthResponseDto response = new AuthResponseDto(userDetails.getUsername());
        request.getSession(true);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @PostMapping("/sign-out")
    public ResponseEntity<Void> signOut(HttpServletRequest request, HttpServletResponse response) {

        Optional.ofNullable(request.getSession(false))
                .ifPresentOrElse(
                        HttpSession::invalidate,
                        () -> {
                            throw new UnauthorizedUserSignOutException("Cannot sign-out unauthorized user");
                        }
                );

        ResponseCookie cookie = ResponseCookie.from("SESSION")
                .path("/")
                .maxAge(0)
                .httpOnly(true)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity
                .noContent()
                .build();
    }
}
