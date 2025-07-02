package com.dimasukimas.cloud_storage.unit.controller;

import com.dimasukimas.cloud_storage.config.TestSecurityConfig;
import com.dimasukimas.cloud_storage.controller.AuthController;
import com.dimasukimas.cloud_storage.dto.CustomUserDetails;
import com.dimasukimas.cloud_storage.exception.handler.GlobalExceptionHandler;
import com.dimasukimas.cloud_storage.service.UserService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(AuthController.class)
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class})
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @Mock
    Authentication authentication;

    private String signUpJson = """
            {
                "username": "testUser",
                "password": "secret",
                "password_confirm": "secret"
            }
            """;

    private String signInJson = """
            {
                "username": "testUser",
                "password": "secret"
            }
            """;

    private String invalidSignInJson = """
            {
                "username": "_wrong_username",
                "password": "_wrong_password"
            }
            """;

    private String invalidSignUpJson = """
            {
                "username": "_wrong_username",
                "password": "secret",
                "password_confirm": "wrongConfirm"
            }
            """;

    @Test
    void signUp_shouldReturnUsernameWithAppropriateStatusCode() throws Exception {

        when(userService.signUp(any())).thenReturn(new CustomUserDetails(1L, "testUser", null, List.of()));

        mockMvc.perform(post("/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testUser"));
    }

    @Test
    void givenInvalidData_whenSignUp_shouldReturnBadRequestWithMessage() throws Exception {

        mockMvc.perform(post("/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidSignUpJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }


    @Test
    void signIn_shouldReturnUsernameWithAppropriateHeadersAndStatusCode() throws Exception {

        when(authentication.getPrincipal()).thenReturn(new CustomUserDetails(1L, "testUser", "encodedPassword", List.of()));
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);

        mockMvc.perform(post("/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signInJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testUser"));
    }

    @Test
    void givenInvalidData_whenSignIn_shouldReturnBadRequestWithMessage() throws Exception {

        mockMvc.perform(post("/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidSignInJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void signOut_shouldInvalidateSessionAndClearCookie() throws Exception {

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        User user = new User("testUser", "password", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        Authentication auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        context.setAuthentication(auth);

        MockHttpSession session = new MockHttpSession();

        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

        mockMvc.perform(post("/auth/sign-out").session(session))
                .andExpect(status().isNoContent())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, Matchers.allOf(
                        Matchers.containsString("SESSION="),
                        Matchers.containsString("Max-Age=0")
                )));

    }

    @Test
    void signOut_withoutAuth_shouldThrowUnauthorizedUserSignOutException() throws Exception {

        mockMvc.perform(post("/auth/sign-out"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Cannot sign-out unauthorized user"));
    }


}
