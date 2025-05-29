package com.dimasukimas.cloud_storage.unit.controller;

import com.dimasukimas.cloud_storage.config.TestSecurityConfig;
import com.dimasukimas.cloud_storage.controller.AuthController;
import com.dimasukimas.cloud_storage.dto.SignUpResponseDto;
import com.dimasukimas.cloud_storage.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

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

    @Test
    void givenValidRegistrationData_whenSignUp_thenUserIsCreated() throws Exception {

        String json = """
                {
                    "username": "user",
                    "password": "secret"
                }
                """;

        when(userService.signUp(any())).thenReturn(new SignUpResponseDto("user"));

        mockMvc.perform(post("/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("user"));
    }
}
