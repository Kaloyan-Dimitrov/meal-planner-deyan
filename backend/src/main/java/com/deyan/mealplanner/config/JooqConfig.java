package com.deyan.mealplanner.config;

import org.jooq.DSLContext;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultDSLContext;
import org.jooq.impl.SQLDataType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class JooqConfig {
    @Bean
    public DSLContext dslContext(DataSource dataSource) {
        return new DefaultDSLContext(new DefaultConfiguration().set(dataSource));
    }
}
