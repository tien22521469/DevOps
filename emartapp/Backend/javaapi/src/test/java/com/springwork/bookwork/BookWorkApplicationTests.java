package com.springwork.bookwork;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
class BookWorkApplicationTests {

	@Test
	void contextLoads() {
		// This test will verify that the Spring application context loads successfully
	}

}
