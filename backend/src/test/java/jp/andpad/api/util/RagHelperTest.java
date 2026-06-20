package jp.andpad.api.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jp.andpad.api.util.RagHelper;
import java.util.List;
import jp.andpad.api.domain.ExtendedTypes.RagDocument;
import jp.andpad.api.domain.ExtendedTypes.RagSearchHit;

class RagHelperTest {

    @Test
    void localSearchFindsDocument() {
        var docs = List.of(new RagDocument("d1", "感染管理", "手指衛生の重要性", List.of("tag"), "2024-01-01"));
        List<RagSearchHit> hits = RagHelper.localSearch(docs, "感染", 3);
        assertThat(hits).hasSize(1);
        assertThat(RagHelper.formatHitsAsAnswer(hits)).contains("感染管理");
    }

}
