package jp.andpad.api.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jp.andpad.api.AbstractIntegrationTest;

import jp.andpad.api.security.JwtAuthFilter;

class JwtAuthFilterTest extends AbstractIntegrationTest {

    @Autowired
    JwtAuthFilter jwtAuthFilter;

    @Test
    void filterBeanLoads() {
        assertThat(jwtAuthFilter).isNotNull();
    }

}
