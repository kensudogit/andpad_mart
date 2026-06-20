package jp.andpad.api.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jp.andpad.api.AbstractIntegrationTest;

import jp.andpad.api.repository.LearningRepository;

class LearningRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    jp.andpad.api.repository.LearningRepository learningRepository;

    @Test
    void listsFeaturedVideos() {
        assertThat(learningRepository.featuredVideos("org_demo")).isNotEmpty();
    }

}
