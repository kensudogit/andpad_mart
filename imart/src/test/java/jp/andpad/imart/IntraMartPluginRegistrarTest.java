package jp.andpad.imart;

import jp.andpad.imart.spi.IntraMartContextSpi;
import jp.andpad.imart.stub.DevIntraMartContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import jp.andpad.imart.config.IntraMartAutoConfiguration;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link IntraMartPluginRegistrar} および {@link DevIntraMartContext} の結合テスト。
 *
 * <p>IM 認証無効・開発スタブ有効の状態で、プラグイン ID とテナント ID が
 * 期待どおり返却されることを検証する。
 */
@SpringBootTest(classes = {IntraMartAutoConfiguration.class, DevIntraMartContext.class, IntraMartPluginRegistrar.class})
class IntraMartPluginRegistrarTest {

    /** テスト対象: intra-mart コンテキスト SPI。 */
    @Autowired
    IntraMartContextSpi context;

    /**
     * 開発スタブが正しいプラグイン ID・テナント ID を返すことを確認する。
     */
    @Test
    void devContextProvidesPluginId() {
        assertThat(context.getPluginId()).isEqualTo("andpad-api");
        assertThat(context.getTenantId()).isEqualTo("local-dev");
        assertThat(context.isLoggedIn()).isFalse();
    }
}
