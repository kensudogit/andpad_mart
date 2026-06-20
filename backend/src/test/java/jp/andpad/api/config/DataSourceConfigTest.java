package jp.andpad.api.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jp.andpad.api.AbstractIntegrationTest;

import jp.andpad.api.config.DataSourceConfig;

class DataSourceConfigTest extends AbstractIntegrationTest {

    @Autowired
    javax.sql.DataSource dataSource;

    @Test
    void dataSourceConnects() throws Exception {
        try (var c = dataSource.getConnection()) {
            assertThat(c.isValid(2)).isTrue();
        }
    }

}
