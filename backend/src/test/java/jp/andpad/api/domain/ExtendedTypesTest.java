package jp.andpad.api.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jp.andpad.api.domain.ExtendedTypes;
import jp.andpad.api.domain.ConstructionProjectStatus;
import jp.andpad.api.domain.SaasModuleCode;
import java.util.List;
import java.util.List;

class ExtendedTypesTest {

    @Test
    void nestedRecordsConstruct() {
        var kpi = new ExtendedTypes.AndpadAnalyticsKpi("Projects", 3, "件", 1.0);
        assertThat(kpi.label()).isEqualTo("Projects");
        var doc = new ExtendedTypes.RagDocument("r1", "t", "c", List.of(), "2024-01-01");
        assertThat(doc.id()).isEqualTo("r1");
    }

}
