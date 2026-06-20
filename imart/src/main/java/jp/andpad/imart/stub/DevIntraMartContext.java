package jp.andpad.imart.stub;

import jp.andpad.imart.spi.IntraMartContextSpi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * ローカル開発用 intra-mart コンテキストスタブ。
 * IM ライセンス環境が無い場合に Spring コンテキストを起動できるようにする。
 */
@Component
@ConditionalOnProperty(name = "app.imart.enabled", havingValue = "false", matchIfMissing = true)
public class DevIntraMartContext implements IntraMartContextSpi {

    @Override
    public String getLoginUserId() {
        return null;
    }

    @Override
    public String getTenantId() {
        return "local-dev";
    }

    @Override
    public boolean isLoggedIn() {
        return false;
    }
}
