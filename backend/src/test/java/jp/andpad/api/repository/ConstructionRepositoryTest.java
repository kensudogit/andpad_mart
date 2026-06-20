package jp.andpad.api.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jp.andpad.api.AbstractIntegrationTest;

import jp.andpad.api.repository.ConstructionRepository;

class ConstructionRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    jp.andpad.api.repository.ConstructionRepository constructionRepository;

    @Test
    void listsDemoProjects() {
        assertThat(constructionRepository.listProjects("org_demo")).isNotEmpty();
    }

}
