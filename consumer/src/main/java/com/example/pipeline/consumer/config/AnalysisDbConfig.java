package com.example.pipeline.consumer.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import jakarta.persistence.EntityManagerFactory;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = "com.example.pipeline.consumer.repository.analysis",
    entityManagerFactoryRef = "analysisEntityManagerFactory",
    transactionManagerRef = "analysisTransactionManager"
)
public class AnalysisDbConfig {

    @Bean
    @ConfigurationProperties("spring.analysis-datasource")
    public DataSourceProperties analysisDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource analysisDataSource() {
        return analysisDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean analysisEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("analysisDataSource") DataSource dataSource) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");

        return builder
                .dataSource(dataSource)
                .packages("com.example.pipeline.common.model")
                .persistenceUnit("analysis")
                .properties(properties)
                .build();
    }

    @Bean
    public PlatformTransactionManager analysisTransactionManager(
            @Qualifier("analysisEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
