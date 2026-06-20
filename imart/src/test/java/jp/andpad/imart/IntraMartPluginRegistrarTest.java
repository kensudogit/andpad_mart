package jp.andpad.imart;

import jp.andpad.imart.spi.IntraMartContextSpi;
import jp.andpad.imart.stub.DevIntraMartContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import jp.andpad.imart.config.IntraMartAutoConfiguration;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {IntraMartAutoConfiguration.class, DevIntraMartContext.class, IntraMartPluginRegistrar.class})
class IntraMartPluginRegistrarTest {

    @Autowired
    IntraMartContextSpi context;

    @Test
    void devContextProvidesPluginId() {
        assertThat(context.getPluginId()).isEqualTo("andpad-api");
        assertThat(context.getTenantId()).isEqualTo("local-dev");
        assertThat(context.isLoggedIn()).isFalse();
    }
}
