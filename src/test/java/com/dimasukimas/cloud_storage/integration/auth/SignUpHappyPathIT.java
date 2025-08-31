package com.dimasukimas.cloud_storage.integration.auth;


import com.dimasukimas.cloud_storage.annotation.IntegrationTest;
import com.dimasukimas.cloud_storage.config.container.MinioContainerInitializer;
import com.dimasukimas.cloud_storage.config.container.PostgresContainerInitializer;
import com.dimasukimas.cloud_storage.config.container.RedisContainerInitializer;
import com.dimasukimas.cloud_storage.dto.UsernameDto;
import com.dimasukimas.cloud_storage.helper.MinioTestHelper;
import com.dimasukimas.cloud_storage.helper.RedisTestHelper;
import com.dimasukimas.cloud_storage.helper.RequestTestHelper;
import com.dimasukimas.cloud_storage.helper.UserTestDataHelper;
import com.dimasukimas.cloud_storage.util.assertion.*;
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
    private static final String USER_ROOT_DIRECTORY = "user-1-files/";
    private static final String SIGN_UP_URL = "/auth/sign-up";

    @BeforeEach
    void setUp() {
        minioHelper.clearBucket();
        userTestDataHelper.clearRepository();
        redisHelper.clear();
    }

    @Test
    public void givenValidUserData_whenSignUp_thenSuccessful() throws Exception {
        ResponseEntity<UsernameDto> response = testRestTemplate.postForEntity(
                SIGN_UP_URL,
                requestHelper.authRequest(USERNAME, PASSWORD),
                UsernameDto.class);

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
                .assertUserMinioRootDirectoryCreated(USER_ROOT_DIRECTORY)
                .assertUserExists(USERNAME)
                .assertRedisSessionCreated();
    }
}
