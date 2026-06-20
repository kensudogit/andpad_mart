package jp.andpad.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jp.andpad.api.AbstractIntegrationTest;

import jp.andpad.api.AndpadApplication;

class AndpadApplicationTest extends AbstractIntegrationTest {

    @Test
    void contextLoads() {
        assertThat(mockMvc).isNotNull();
    }

}
