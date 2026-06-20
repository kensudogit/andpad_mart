package jp.andpad.imart;

import jp.andpad.imart.spi.IntraMartContextSpi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * ANDPAD API プラグインの intra-mart 統合ブートストラップ。
 *
 * <p>Spring Boot アプリケーション起動完了時（{@link ApplicationReadyEvent}）に、
 * プラグイン ID・テナント ID・ログイン状態をログ出力する。intra-mart WAR デプロイ時の
 * プラグイン登録確認や、ローカル開発時の統合状態確認に使用する。
 *
 * @see IntraMartContextSpi
 */
@Component
public class IntraMartPluginRegistrar {

    private static final Logger log = LoggerFactory.getLogger(IntraMartPluginRegistrar.class);

    /** intra-mart コンテキスト SPI（テナント・ログイン状態の取得元）。 */
    private final IntraMartContextSpi context;

    /**
     * コンストラクタ。
     *
     * @param context intra-mart コンテキスト SPI
     */
    public IntraMartPluginRegistrar(IntraMartContextSpi context) {
        this.context = context;
    }

    /**
     * アプリケーション起動完了時にプラグイン登録情報をログ出力する。
     *
     * <p>出力例: {@code intra-mart ANDPAD plugin registered: pluginId=andpad-api, tenantId=local-dev, imLoggedIn=false}
     */
    @EventListener(ApplicationReadyEvent.class)
    public void registerPlugin() {
        log.info(
                "intra-mart ANDPAD plugin registered: pluginId={}, tenantId={}, imLoggedIn={}",
                context.getPluginId(),
                context.getTenantId(),
                context.isLoggedIn());
    }
}
