package com.dimasukimas.cloudstorage.unit.controller;

import com.dimasukimas.cloudstorage.controller.AuthController;
import com.dimasukimas.cloudstorage.dto.AuthRequestDto;
import com.dimasukimas.cloudstorage.dto.CustomUserDetails;
import com.dimasukimas.cloudstorage.exception.handler.GlobalExceptionHandler;
import com.dimasukimas.cloudstorage.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(AuthController.class)
@AutoConfigureJsonTesters
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JacksonTester<AuthRequestDto> jacksonTester;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthenticationManager authenticationManager;


    @Test
    void signUp_shouldReturnUsernameWithCreatedStatus() throws Exception {
        var signUpRequest = new AuthRequestDto("testUser", "secret");

        when(userService.signUp(any())).thenReturn(new CustomUserDetails(1L, "testUser", null, List.of()));

        mockMvc.perform(post("/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jacksonTester.write(signUpRequest).getJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testUser"));
    }

    @Test
    void signUp_withInvalidData_shouldReturnBadRequest() throws Exception {
        var signUpRequest = new AuthRequestDto("_wrong_username", "_wrong_password");

        mockMvc.perform(post("/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jacksonTester.write(signUpRequest).getJson()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }


    @Test
    void signIn_shouldReturnUsernameWithOk() throws Exception {
        var principal = new CustomUserDetails(
                1L, "testUser", "encoded", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        var authenticated = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());
        var signInRequest = new AuthRequestDto("testUser", "secret");

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(authenticated);

        mockMvc.perform(post("/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jacksonTester.write(signInRequest).getJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testUser"));
    }

    @Test
    void signIn_withInvalidData_shouldReturnBadRequest() throws Exception {
        var signInRequest = new AuthRequestDto("_wrong_username", "secret");

        mockMvc.perform(post("/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jacksonTester.write(signInRequest).getJson()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void signOut_shouldInvalidateSessionAndClearCookie() throws Exception {
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/auth/sign-out").session(session))
                .andExpect(status().isNoContent())
                .andExpect(cookie().exists("SESSION"))
                .andExpect((cookie().maxAge("SESSION", 0)))
                .andExpect(cookie().httpOnly("SESSION", true))
                .andExpect(cookie().path("SESSION", "/"));

       assertTrue(session.isInvalid());
    }

    @Test
    void signOut_withoutAuth_shouldThrowUnauthorizedUserSignOutException() throws Exception {

        mockMvc.perform(post("/auth/sign-out"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Cannot sign-out unauthorized user"));
    }
}
