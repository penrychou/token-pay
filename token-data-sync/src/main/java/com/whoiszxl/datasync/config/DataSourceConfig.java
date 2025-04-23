package com.whoiszxl.datasync.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * 数据源配置类
 */
@Configuration
public class DataSourceConfig {

    /**
     * 源数据库数据源
     */
    @Bean(name = "sourceDataSource")
    @ConfigurationProperties(prefix = "spring.source-datasource")
    public DataSource sourceDataSource() {
        return DataSourceBuilder.create().build();
    }

    /**
     * 目标数据库数据源
     */
    @Bean(name = "targetDataSource")
    @Primary
    @ConfigurationProperties(prefix = "spring.target-datasource")
    public DataSource targetDataSource() {
        return DataSourceBuilder.create().build();
    }

    /**
     * 源数据库JdbcTemplate
     */
    @Bean(name = "sourceJdbcTemplate")
    public JdbcTemplate sourceJdbcTemplate(@Qualifier("sourceDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    /**
     * 目标数据库JdbcTemplate
     */
    @Bean(name = "targetJdbcTemplate")
    public JdbcTemplate targetJdbcTemplate(@Qualifier("targetDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
} 