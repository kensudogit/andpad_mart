package jp.andpad.api.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jp.andpad.api.AbstractIntegrationTest;

import jp.andpad.api.repository.ExtendedRepository;

class ExtendedRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    jp.andpad.api.repository.ExtendedRepository extendedRepository;

    @Test
    void buildsAnalytics() {
        assertThat(extendedRepository.andpadAnalytics("org_demo", 30).activeProjects()).isGreaterThanOrEqualTo(0);
    }

}
