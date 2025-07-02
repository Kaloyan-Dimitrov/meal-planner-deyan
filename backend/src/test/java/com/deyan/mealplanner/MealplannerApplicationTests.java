package com.deyan.mealplanner;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.PostgreSQLContainer;
@Testcontainers
@SpringBootTest
class MealplannerApplicationTests {


	// ② one shared container for the whole class
	@Container
	static PostgreSQLContainer<?> postgres =
			new PostgreSQLContainer<>("postgres:15")
					.withDatabaseName("mealplanner")
					.withUsername("postgres")
					.withPassword("postgres");

	// ③ wire the container’s connection info into Spring Boot
	@DynamicPropertySource
	static void datasourceProps(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url",      postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
		registry.add("spring.datasource.driver-class-name",
				postgres::getDriverClassName);
	}

	@Test
	void contextLoads() { /* nothing – just ensure startup */ }

}
