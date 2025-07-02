package com.dimasukimas.cloud_storage.integration;


import com.dimasukimas.cloud_storage.dto.UsernameDto;
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
    ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    @ServiceConnection
    @SuppressWarnings("resource")
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379)
            .waitingFor(Wait.forListeningPort());

    private HttpEntity<String> request;

    @BeforeEach
    void setUpRequest(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String json = """
                {
                    "username": "testUser",
                    "password": "secret"
                }
                """;
        request = new HttpEntity<>(json, headers);
    }

    @Test
    public void givenValidRegistrationData_whenSignUp_thenUserAndSessionIsCreatedWithAppropriateHeadersAndStatusCode() throws Exception {
        ResponseEntity<String> response = testRestTemplate.postForEntity("/auth/sign-up", request, String.class);
        UsernameDto responseDto = objectMapper.readValue(response.getBody(), UsernameDto.class);
        List<String> setCookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        Set<String> redisKeys = redisTemplate.keys("spring:session:sessions:*");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(responseDto.username()).isEqualTo("testUser");
        assertThat(userRepository.findByUsername("testUser")).isPresent();
        assertThat(setCookies).anyMatch(cookie -> cookie.startsWith("SESSION"));
        assertThat(redisKeys).isNotEmpty();
    }

    @Test
    public void givenDuplicatedUsername_whenSignUp_thenReturnConflictResponse() throws Exception {
        testRestTemplate.postForEntity("/auth/sign-up", request, String.class);

        ResponseEntity<String> response = testRestTemplate.postForEntity("/auth/sign-up", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).contains("Username already exists");
    }
}
