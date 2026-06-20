package jp.andpad.api.config;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.zaxxer.hikari.HikariDataSource;

/** Railway の postgresql:// URL を Spring JDBC 向け jdbc:postgresql:// に正規化する。 */
@Configuration
public class DataSourceConfig {

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    DataSource dataSource(DataSourceProperties props) {
        String raw = DatabaseUrlSupport.resolveRawUrl(props.getUrl());
        if (DatabaseUrlSupport.isPostgresUrl(raw)) {
            DatabaseUrlSupport.applyCredentialsFromUrl(props, raw);
            props.setUrl(DatabaseUrlSupport.normalizeForJdbc(raw));
        }
        return props.initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }
}
