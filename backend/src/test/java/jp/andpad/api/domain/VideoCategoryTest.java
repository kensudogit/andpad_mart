package jp.andpad.api.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jp.andpad.api.domain.VideoCategory;

class VideoCategoryTest {

    @Test
    void valuesAreDefined() {
        assertThat(VideoCategory.values()).isNotEmpty();    }

}
