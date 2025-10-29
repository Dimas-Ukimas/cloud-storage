package com.dimasukimas.cloudstorage.integration.auth;


import com.dimasukimas.cloudstorage.annotation.IntegrationTest;
import com.dimasukimas.cloudstorage.config.container.MinioContainerInitializer;
import com.dimasukimas.cloudstorage.config.container.PostgresContainerInitializer;
import com.dimasukimas.cloudstorage.config.container.RedisContainerInitializer;
import com.dimasukimas.cloudstorage.dto.UsernameRequestDto;
import com.dimasukimas.cloudstorage.helper.MinioTestHelper;
import com.dimasukimas.cloudstorage.helper.RedisTestHelper;
import com.dimasukimas.cloudstorage.helper.RequestTestHelper;
import com.dimasukimas.cloudstorage.helper.UserTestDataHelper;
import com.dimasukimas.cloudstorage.util.assertion.*;
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
        MinioContainerInitializer.class,
        RedisContainerInitializer.class
})
public class SignUpHappyPathIT {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private RequestTestHelper requestHelper;

    @Autowired
    private MinioTestHelper minioHelper;

    @Autowired
    RedisTestHelper redisHelper;

    @Autowired
    UserTestDataHelper userTestDataHelper;

    private static final String USERNAME = "testUser";
    private static final String PASSWORD = "password";

    @BeforeEach
    void setUp() {
        minioHelper.clearBucket();
        userTestDataHelper.clearRepository();
        redisHelper.clear();
    }

    @Test
    public void givenValidUserData_whenSignUp_thenSuccessful() throws Exception {
        ResponseEntity<UsernameRequestDto> response = testRestTemplate.postForEntity(
                "/auth/sign-up",
                requestHelper.authRequest(USERNAME, PASSWORD),
                UsernameRequestDto.class);

        long userId = userTestDataHelper.findUser(USERNAME).orElseThrow().getId();
        String userRootDirectory = "user-" + userId + "-files/";

        MinioAuthAssert.create(
                        RedisAssert.create(redisHelper),
                        UserAssert.create(userTestDataHelper),
                        HttpAssert.create(response),
                        MinioAssert.create(minioHelper)
                )
                .assertStatus(HttpStatus.CREATED)
                .assertJsonContentType()
                .assertBodyContainsUsername(USERNAME)
                .assertSetCookieHeader()
                .assertUserMinioRootDirectoryCreated(userRootDirectory)
                .assertUserExists(USERNAME)
                .assertRedisSessionCreated();
    }
}
