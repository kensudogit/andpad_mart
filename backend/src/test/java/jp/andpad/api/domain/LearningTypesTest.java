package jp.andpad.api.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jp.andpad.api.domain.LearningTypes;
import jp.andpad.api.domain.SkillLevel;
import jp.andpad.api.domain.VideoCategory;
import java.util.List;
import java.util.List;

class LearningTypesTest {

    @Test
    void nestedRecordsConstruct() {
        var stats = new LearningTypes.DashboardStats(10, 6, 3, 1, 2.5, 4);
        assertThat(stats.videosTotal()).isEqualTo(10);
        var video = new LearningTypes.Video("v1", "t", "d", VideoCategory.ENDODONTICS, "p",
                SkillLevel.BEGINNER, 60, "thumb", "url", "i1", "inst", List.of(), 0, "2024-01-01", true);
        assertThat(video.id()).isEqualTo("v1");
    }

}
