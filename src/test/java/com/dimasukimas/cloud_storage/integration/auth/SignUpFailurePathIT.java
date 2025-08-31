package com.dimasukimas.cloud_storage.integration.auth;

import com.dimasukimas.cloud_storage.annotation.IntegrationTest;
import com.dimasukimas.cloud_storage.config.container.MinioContainerInitializer;
import com.dimasukimas.cloud_storage.config.container.PostgresContainerInitializer;
import com.dimasukimas.cloud_storage.config.container.RedisContainerInitializer;
import com.dimasukimas.cloud_storage.dto.AuthRequestDto;
import com.dimasukimas.cloud_storage.exception.MinioOperationException;
import com.dimasukimas.cloud_storage.exception.handler.ErrorResponse;
import com.dimasukimas.cloud_storage.helper.MinioTestHelper;
import com.dimasukimas.cloud_storage.helper.RedisTestHelper;
import com.dimasukimas.cloud_storage.helper.RequestTestHelper;
import com.dimasukimas.cloud_storage.helper.UserTestDataHelper;
import com.dimasukimas.cloud_storage.repository.StorageRepository;
import com.dimasukimas.cloud_storage.service.UserService;
import com.dimasukimas.cloud_storage.util.assertion.AuthAssert;
import com.dimasukimas.cloud_storage.util.assertion.HttpAssert;
import com.dimasukimas.cloud_storage.util.assertion.RedisAssert;
import com.dimasukimas.cloud_storage.util.assertion.UserAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;

@IntegrationTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ContextConfiguration(initializers = {
        PostgresContainerInitializer.class,
        MinioContainerInitializer.class,
        RedisContainerInitializer.class
})
public class SignUpFailurePathIT {

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

    @MockitoBean
    private StorageRepository minioRepository;

    @Autowired
    private UserService userService;

    private static final String USERNAME = "testUser";
    private static final String PASSWORD = "password";
    private static final String CONFLICT_MESSAGE = "Username already exists";
    private static final String SIGN_UP_URL = "/auth/sign-up";

    @BeforeEach
    void setUp() {
        minioHelper.clearBucket();
        userTestDataHelper.clearRepository();
        redisHelper.clear();
    }

    @Test
    public void givenUnsuccessfulDirectoryCreation_whenSignUp_thenRegistrationFailed() {
        doThrow(new MinioOperationException("Cannot create directory")).when(minioRepository).createDirectory(anyString());

        assertThrows(MinioOperationException.class, () -> {
            userService.signUp(new AuthRequestDto(USERNAME, PASSWORD));
        });
        assertThat(userTestDataHelper.findUser(USERNAME)).isEmpty();
    }

    @Test
    public void givenDuplicatedUsername_whenSignUp_thenReturnConflictResponse() throws Exception {
        userTestDataHelper.createUser(USERNAME, PASSWORD);

        ResponseEntity<?> response = testRestTemplate.postForEntity(SIGN_UP_URL,
                requestHelper.authRequest(USERNAME, PASSWORD),
                ErrorResponse.class);

        AuthAssert.create(
                        RedisAssert.create(redisHelper),
                        UserAssert.create(userTestDataHelper),
                        HttpAssert.create(response)
                )
                .assertStatus(HttpStatus.CONFLICT)
                .assertBodyContainsMessage(CONFLICT_MESSAGE);
    }
}
