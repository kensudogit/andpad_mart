package jp.andpad.api.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jp.andpad.api.domain.SaasModuleCode;

class SaasModuleCodeTest {

    @Test
    void valuesAreDefined() {
        assertThat(SaasModuleCode.values()).isNotEmpty();    }

}
