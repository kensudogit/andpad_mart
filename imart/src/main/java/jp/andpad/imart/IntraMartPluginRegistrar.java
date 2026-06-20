package jp.andpad.imart;

import jp.andpad.imart.spi.IntraMartContextSpi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * ANDPAD API プラグインを intra-mart 上に登録するブートストラップ。
 * アプリ起動完了時にプラグイン ID とテナント情報をログ出力する。
 */
@Component
public class IntraMartPluginRegistrar {

    private static final Logger log = LoggerFactory.getLogger(IntraMartPluginRegistrar.class);

    private final IntraMartContextSpi context;

    public IntraMartPluginRegistrar(IntraMartContextSpi context) {
        this.context = context;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void registerPlugin() {
        log.info(
                "intra-mart ANDPAD plugin registered: pluginId={}, tenantId={}, imLoggedIn={}",
                context.getPluginId(),
                context.getTenantId(),
                context.isLoggedIn());
    }
}
