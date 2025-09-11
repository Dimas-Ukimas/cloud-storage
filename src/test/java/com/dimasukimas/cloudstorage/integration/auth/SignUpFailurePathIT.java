package com.dimasukimas.cloudstorage.integration.auth;

import com.dimasukimas.cloudstorage.annotation.IntegrationTest;
import com.dimasukimas.cloudstorage.config.container.MinioContainerInitializer;
import com.dimasukimas.cloudstorage.config.container.PostgresContainerInitializer;
import com.dimasukimas.cloudstorage.config.container.RedisContainerInitializer;
import com.dimasukimas.cloudstorage.dto.AuthRequestDto;
import com.dimasukimas.cloudstorage.exception.MinioOperationException;
import com.dimasukimas.cloudstorage.exception.handler.ErrorResponse;
import com.dimasukimas.cloudstorage.helper.MinioTestHelper;
import com.dimasukimas.cloudstorage.helper.RedisTestHelper;
import com.dimasukimas.cloudstorage.helper.RequestTestHelper;
import com.dimasukimas.cloudstorage.helper.UserTestDataHelper;
import com.dimasukimas.cloudstorage.repository.StorageRepository;
import com.dimasukimas.cloudstorage.service.UserService;
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
