package com.deyan.mealplanner.service

import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Shared
import spock.lang.Specification


import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.DynamicPropertyRegistry
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.junit.jupiter.Container

@SpringBootTest
@Testcontainers
abstract class AbstractIT extends Specification{
    /** One reusable container for the whole test JVM */
    @Shared
    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("mealplanner")
                    .withUsername("postgres")
                    .withPassword("postgres")
                    .withReuse(true);           // speeds up repeated `./gradlew test`

    /** Wire container values into Spring’s Environment before the context starts */
    @DynamicPropertySource
    static void configureSpringDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",      POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }
}
