package com.dimasukimas.cloudstorage;

import com.dimasukimas.cloudstorage.annotation.IntegrationTest;
import com.dimasukimas.cloudstorage.integration.operations.BaseResourceOperationsIT;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@IntegrationTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "app.security.enabled=false")
class CloudStorageApplicationTests extends BaseResourceOperationsIT {

	@Test
	void contextLoads() {
	}

}
