package com.dimasukimas.cloud_storage.integration;


import com.dimasukimas.cloud_storage.dto.AuthResponseDto;
import com.dimasukimas.cloud_storage.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
public class SignUpIT {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    private UserRepository userRepository;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    @ServiceConnection
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379)
            .waitingFor(Wait.forListeningPort());

    @Test
    public void givenValidRegistrationData_whenSignUp_thenUserAndSessionIsCreatedWithAppropriateHeadersAndStatusCode() throws Exception {

        String json = """
                {
                    "username": "user",
                    "password": "secret"
                }
                """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(json, headers);

        ResponseEntity<String> response = testRestTemplate.postForEntity("/auth/sign-up", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

        ObjectMapper objectMapper = new ObjectMapper();
        AuthResponseDto responseDto = objectMapper.readValue(response.getBody(), AuthResponseDto.class);
        assertThat(responseDto.username()).isEqualTo("user");

        assertThat(userRepository.findByUsername("user")).isPresent();

        List<String> setCookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        assertThat(setCookies).anyMatch(cookie -> cookie.startsWith("SESSION"));

        Set<String> keys = redisTemplate.keys("spring:session:sessions:*");
        assertThat(keys).isNotEmpty();
    }
}
