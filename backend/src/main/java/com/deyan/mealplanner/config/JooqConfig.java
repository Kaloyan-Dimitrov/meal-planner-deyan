package com.deyan.mealplanner.config;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultDSLContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class JooqConfig {
    @Bean
    public DSLContext dslContext(DataSource dataSource) {

        Settings settings = new Settings();          // leave default values

        return DSL.using(
                dataSource,
                SQLDialect.POSTGRES,                 // <<< forces proper Postgres syntax
                settings);

    }
}
