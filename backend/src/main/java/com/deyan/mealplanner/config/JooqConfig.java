package com.deyan.mealplanner.config;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class JooqConfig {
    /**
     * Creates a DSLContext bean configured with the application's DataSource
     * and the PostgreSQL SQL dialect.
     * This is the primary interface for using JOOQ to construct and execute queries in a type-safe manner.
     *
     * @param dataSource The injected Spring-managed {@link DataSource} for connecting to the database.
     * @return A fully configured {@link DSLContext} instance.
     */
    @Bean
    public DSLContext dslContext(DataSource dataSource) {

        Settings settings = new Settings();

        return DSL.using(
                dataSource,
                SQLDialect.POSTGRES,
                settings);

    }
}
