package com.deyan.mealplanner
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.spock.Testcontainers
import spock.lang.Specification;
@Testcontainers
@SpringBootTest
abstract class AbstractIT extends Specification {
    static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer('postgres:16-alpine')
                        .withDatabaseName('mealplanner')
                        .withUsername('postgres')
                        .withPassword('postgres');

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void registerPgProps(DynamicPropertyRegistry reg) {
        reg.add('spring.datasource.url', POSTGRES::getJdbcUrl)
        reg.add('spring.datasource.username', POSTGRES::getUsername)
        reg.add('spring.datasource.password', POSTGRES::getPassword)
    }
}