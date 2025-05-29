package com.dimasukimas.cloud_storage.integration;


import jakarta.servlet.ServletContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
public class SignUpIT {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    @ServiceConnection
    static GenericContainer<?> genericContainer = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);


    @Autowired
    private Environment environment;

    @Test
    void printContextPath() {
        String contextPath = environment.getProperty("server.servlet.context-path");
        System.out.println("Loaded context path = " + contextPath);
    }
    @Test
    void checkContextPathFromServletContext(@Autowired ServletContext servletContext) {
        System.out.println("Servlet context path: " + servletContext.getContextPath());
    }

    @Test
    void printBaseUrl() {
        String baseUrl = testRestTemplate.getRestTemplate().getUriTemplateHandler().expand("/").toString();
        System.out.println("Base URL = " + baseUrl);
    }


    @Test
    public void givenValidRegistrationData_whenSignUp_thenUserIsCreated() throws Exception {

        String json = """
                {
                    "username": "user",
                    "password": "secret"
                }
                """;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new  HttpEntity<>(json, headers);

        ResponseEntity<String> response = testRestTemplate.postForEntity("/auth/sign-up", request, String.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }
}
