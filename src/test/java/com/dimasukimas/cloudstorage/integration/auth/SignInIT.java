package com.dimasukimas.cloudstorage.integration.auth;

import com.dimasukimas.cloudstorage.annotation.IntegrationTest;
import com.dimasukimas.cloudstorage.config.container.MinioContainerInitializer;
import com.dimasukimas.cloudstorage.config.container.PostgresContainerInitializer;
import com.dimasukimas.cloudstorage.config.container.RedisContainerInitializer;
import com.dimasukimas.cloudstorage.dto.UsernameDto;
import com.dimasukimas.cloudstorage.exception.handler.ErrorResponse;
import com.dimasukimas.cloudstorage.helper.RedisTestHelper;
import com.dimasukimas.cloudstorage.helper.RequestTestHelper;
import com.dimasukimas.cloudstorage.helper.UserTestDataHelper;
import com.dimasukimas.cloudstorage.util.assertion.AuthAssert;
import com.dimasukimas.cloudstorage.util.assertion.HttpAssert;
import com.dimasukimas.cloudstorage.util.assertion.RedisAssert;
import com.dimasukimas.cloudstorage.util.assertion.UserAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;

@IntegrationTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ContextConfiguration(initializers = {
        PostgresContainerInitializer.class,
        RedisContainerInitializer.class,
        MinioContainerInitializer.class
})
public class SignInIT {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private RequestTestHelper requestHelper;

    @Autowired
    private RedisTestHelper redisHelper;

    @Autowired
    private UserTestDataHelper userTestDataHelper;

    private static final String USERNAME = "testUser";
    private static final String PASSWORD = "password";
    private static final String NONEXISTENT_USER = "anonymous";
    private static final String UNAUTHORIZED_MESSAGE ="Invalid username or password";

    @BeforeEach
    void setUp() {
        userTestDataHelper.clearRepository();
        userTestDataHelper.createUser(USERNAME, PASSWORD);
        redisHelper.clear();
    }

    @Test
    public void givenExistentUser_whenSignIn_thenAuthenticationSuccessful() throws Exception {
        ResponseEntity<UsernameDto> response = testRestTemplate.postForEntity(
                "/auth/sign-in",
                requestHelper.authRequest(USERNAME, PASSWORD),
                UsernameDto.class);

        AuthAssert.create(
                        RedisAssert.create(redisHelper),
                        UserAssert.create(userTestDataHelper),
                        HttpAssert.create(response)
                )
                .assertStatus(HttpStatus.OK)
                .assertJsonContentType()
                .assertBodyContainsUsername(USERNAME)
                .assertSetCookieHeader()
                .assertRedisSessionCreated();
    }

    @Test
    public void givenNonexistentUser_whenSignIn_thenReturnUnauthorized() throws Exception {
        ResponseEntity<ErrorResponse> response = testRestTemplate.postForEntity(
                "/auth/sign-in",
                requestHelper.authRequest(NONEXISTENT_USER, PASSWORD),
                ErrorResponse.class);

        AuthAssert.create(
                        RedisAssert.create(redisHelper),
                        UserAssert.create(userTestDataHelper),
                        HttpAssert.create(response)
                )
                .assertStatus(HttpStatus.UNAUTHORIZED)
                .assertJsonContentType()
                .assertBodyContainsMessage(UNAUTHORIZED_MESSAGE);
    }
}
