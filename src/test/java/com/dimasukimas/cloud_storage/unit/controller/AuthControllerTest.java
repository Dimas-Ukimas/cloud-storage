package com.dimasukimas.cloud_storage.unit.controller;

import com.dimasukimas.cloud_storage.config.TestSecurityConfig;
import com.dimasukimas.cloud_storage.controller.AuthController;
import com.dimasukimas.cloud_storage.dto.AuthResponseDto;
import com.dimasukimas.cloud_storage.dto.UserDetailsImpl;
import com.dimasukimas.cloud_storage.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(AuthController.class)
@Import(TestSecurityConfig.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @Mock
    Authentication authentication;

    private String json = """
            {
                "username": "user",
                "password": "secret"
            }
            """;

    @Test
    void shouldReturnUsernameWithAppropriateHeadersAndStatusCodeWhenSignUp() throws Exception {

        when(userService.signUp(any())).thenReturn(new UserDetailsImpl("user", null, List.of()));

        mockMvc.perform(post("/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("user"));
    }

    @Test
    void shouldReturnUsernameWithAppropriateHeadersAndStatusCodeWhenSignIn() throws Exception {

        when(authentication.getPrincipal()).thenReturn(new User("user", "encodedPassword", List.of()));
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);

        mockMvc.perform(post("/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user"));
    }
}
