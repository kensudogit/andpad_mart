package jp.andpad.imart.stub;

import jp.andpad.imart.spi.IntraMartContextSpi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * ローカル開発用 intra-mart コンテキストスタブ。
 *
 * <p>intra-mart ライセンス環境や IM コンテナが無い状態でも Spring Boot アプリケーションを
 * 起動できるようにする。IM 認証（{@code app.imart.auth.enabled=true}）が無効な場合にのみ
 * 有効化される。
 *
 * <p>返却値:
 * <ul>
 *   <li>ログインユーザ — 常に {@code null}（未ログイン扱い）</li>
 *   <li>テナント ID — 固定値 {@code local-dev}</li>
 * </ul>
 *
 * @see IntraMartContextSpi
 * @see jp.andpad.imart.auth.ImAuthIntraMartContext
 */
@Component
@ConditionalOnProperty(name = "app.imart.auth.enabled", havingValue = "false", matchIfMissing = true)
public class DevIntraMartContext implements IntraMartContextSpi {

    /** {@inheritDoc} — スタブでは常に未ログイン。 */
    @Override
    public String getLoginUserId() {
        return null;
    }

    /** {@inheritDoc} — ローカル開発用固定テナント。 */
    @Override
    public String getTenantId() {
        return "local-dev";
    }

    /** {@inheritDoc} — スタブでは常に {@code false}。 */
    @Override
    public boolean isLoggedIn() {
        return false;
    }
}
