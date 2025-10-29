package com.dimasukimas.cloudstorage.unit.controller;

import com.dimasukimas.cloudstorage.controller.DirectoryController;
import com.dimasukimas.cloudstorage.controller.ResourceController;
import com.dimasukimas.cloudstorage.controller.UserController;
import com.dimasukimas.cloudstorage.mapper.UserMapper;
import com.dimasukimas.cloudstorage.security.SecurityConfig;
import com.dimasukimas.cloudstorage.service.ResourceService;
import com.dimasukimas.cloudstorage.service.UserService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {DirectoryController.class, ResourceController.class, UserController.class})
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
public class AnonymousDeniedTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ResourceService resourceService;

    @MockitoBean
    UserMapper userMapper;

    @MockitoBean
    PasswordEncoder passwordEncoder;

    @MockitoBean
    UserService userService;

    static Stream<Arguments> protectedEndpoints() {
        return Stream.of(
                Arguments.of(HttpMethod.GET.toString(), "/directory/folder1/"),
                Arguments.of(HttpMethod.POST.toString(), "/directory/folder1/"),
                Arguments.of(HttpMethod.GET.toString(), "/resource/folder1/"),
                Arguments.of(HttpMethod.POST.toString(), "/resource/folder1/"),
                Arguments.of(HttpMethod.DELETE.toString(), "/resource/folder1/"),
                Arguments.of(HttpMethod.GET.toString(), "/resource/download/folder1/"),
                Arguments.of(HttpMethod.GET.toString(), "/resource/move/folder1/"),
                Arguments.of(HttpMethod.GET.toString(), "/resource/search/folder1/")
        );
    }

    @ParameterizedTest
    @MethodSource("protectedEndpoints")
    void anonymous_isUnauthorized(String httpMethod, String endpoint) throws Exception {

        var reqBuilder = switch (httpMethod) {
            case "GET" -> get(endpoint);
            case "POST" -> post(endpoint);
            case "PUT" -> put(endpoint);
            case "PATCH" -> patch(endpoint);
            case "DELETE" -> delete(endpoint);
            default -> throw new IllegalStateException();
        };

        mockMvc.perform(reqBuilder).andExpect(status().isUnauthorized());
    }
}
