package jp.andpad.api.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jp.andpad.api.AbstractIntegrationTest;

import jp.andpad.api.service.SaasService;

class SaasServiceTest extends AbstractIntegrationTest {

    @Autowired
    SaasService target;

    @Test
    void beanLoads() {
        assertThat(target).isNotNull();
    }

}
