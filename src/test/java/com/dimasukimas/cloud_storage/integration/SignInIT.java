package com.dimasukimas.cloud_storage.integration;

import com.dimasukimas.cloud_storage.dto.UsernameDto;
import com.dimasukimas.cloud_storage.model.Role;
import com.dimasukimas.cloud_storage.model.User;
import com.dimasukimas.cloud_storage.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
public class SignInIT {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ObjectMapper objectMapper;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    @ServiceConnection
    @SuppressWarnings("resource")
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    HttpEntity<String> validRequest;
    HttpEntity<String> invalidRequest;

    @BeforeEach
    @Transactional
    void setup() {
        userRepository.deleteAll();
        User user = User.builder()
                .username("testUser")
                .password(new BCryptPasswordEncoder().encode("secret"))
                .role(Role.USER)
                .build();
        userRepository.save(user);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String json = """
                {
                    "username": "testUser",
                    "password": "secret"
                }
                """;

        validRequest = new HttpEntity<>(json, headers);

        String invalidJson = """
                {
                    "username": "notExistentUser",
                    "password": "secret"
                }
                """;

        invalidRequest = new HttpEntity<>(invalidJson, headers);
    }

    @Test
    public void givenValidData_whenSignIn_thenAuthenticationSuccessful() throws Exception {

        ResponseEntity<String> response = testRestTemplate.postForEntity("/auth/sign-in", validRequest, String.class);
        UsernameDto responseDto = objectMapper.readValue(response.getBody(), UsernameDto.class);
        List<String> setCookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        Set<String> redisKeys = redisTemplate.keys("spring:session:sessions:*");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(responseDto.username()).isEqualTo("testUser");
        assertThat(setCookies).anyMatch(cookie -> cookie.startsWith("SESSION"));
        assertThat(redisKeys).isNotEmpty();
    }

    @Test
    public void givenInvalidData_whenSignIn_thenReturnUnauthorized() throws Exception {

       ResponseEntity<String> response = testRestTemplate.postForEntity("/auth/sign-in", invalidRequest, String.class);

       assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
       assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
       assertThat(response.getBody()).contains("Invalid username or password");

    }
}
