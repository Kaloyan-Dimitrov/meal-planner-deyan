package com.deyan.mealplanner;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class AbstractIT {

    @Container
    public static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("mealplanner")
                    .withUsername("postgres")
                    .withPassword("postgres")
                    .withReuse(true); // speeds up repeated test runs

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry reg) {
        reg.add("spring.datasource.url",      POSTGRES::getJdbcUrl);
        reg.add("spring.datasource.username", POSTGRES::getUsername);
        reg.add("spring.datasource.password", POSTGRES::getPassword);
    }
}
