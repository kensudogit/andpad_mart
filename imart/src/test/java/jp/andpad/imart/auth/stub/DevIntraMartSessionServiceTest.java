package jp.andpad.imart.auth.stub;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jp.andpad.imart.auth.IntraMartAuthProperties;
import jp.andpad.imart.auth.model.ImLoginSession;

/**
 * {@link DevIntraMartSessionService} の単体テスト。
 *
 * <p>開発用セッション ID の有効/無効判定と、返却されるセッション情報を検証する。
 */
class DevIntraMartSessionServiceTest {

    /**
     * 設定された開発用セッション ID に対して有効セッションが返ることを確認する。
     */
    @Test
    void returnsValidSessionForDevId() {
        var properties = new IntraMartAuthProperties();
        properties.setDevSessionId("dev-imart-session");
        var service = new DevIntraMartSessionService(properties);

        ImLoginSession session =
                service.getLoginSession("dev-imart-session").orElseThrow();
        assertThat(session.valid()).isTrue();
        assertThat(session.userCode()).isEqualTo("dev-user");
    }

    /**
     * 未知のセッション ID に対して無効セッション（{@code valid=false}）が返ることを確認する。
     */
    @Test
    void returnsInvalidForUnknownSession() {
        var properties = new IntraMartAuthProperties();
        var service = new DevIntraMartSessionService(properties);

        ImLoginSession session = service.getLoginSession("unknown").orElseThrow();
        assertThat(session.valid()).isFalse();
    }
}
