package jp.andpad.api.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jp.andpad.api.domain.SaasModule;
import jp.andpad.api.domain.SaasModuleCode;

class SaasModuleTest {

    @Test
    void createsRecord() {
        var value = new SaasModule(SaasModuleCode.CONSTRUCTION_MGMT, "施工", "desc", true);
        assertThat(value).isNotNull();
        assertThat(value.getClass()).isRecord();
    }

}
