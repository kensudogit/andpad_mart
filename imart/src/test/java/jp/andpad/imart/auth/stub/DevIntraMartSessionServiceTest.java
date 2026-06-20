package jp.andpad.imart.auth.stub;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jp.andpad.imart.auth.IntraMartAuthProperties;
import jp.andpad.imart.auth.model.ImLoginSession;

class DevIntraMartSessionServiceTest {

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

    @Test
    void returnsInvalidForUnknownSession() {
        var properties = new IntraMartAuthProperties();
        var service = new DevIntraMartSessionService(properties);

        ImLoginSession session = service.getLoginSession("unknown").orElseThrow();
        assertThat(session.valid()).isFalse();
    }
}
