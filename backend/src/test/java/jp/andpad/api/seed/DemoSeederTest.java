package jp.andpad.api.seed;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jp.andpad.api.AbstractIntegrationTest;

import jp.andpad.api.seed.DemoSeeder;

class DemoSeederTest extends AbstractIntegrationTest {

    @Autowired
    DemoSeeder demoSeeder;

    @Test
    void seederBeanLoads() {
        assertThat(demoSeeder).isNotNull();
    }

}
