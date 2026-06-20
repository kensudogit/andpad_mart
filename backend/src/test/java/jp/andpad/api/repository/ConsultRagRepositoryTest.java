package jp.andpad.api.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jp.andpad.api.AbstractIntegrationTest;

import jp.andpad.api.repository.ConsultRagRepository;

class ConsultRagRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    jp.andpad.api.repository.ConsultRagRepository consultRagRepository;

    @Test
    void listsRagDocuments() {
        assertThat(consultRagRepository.listRagDocuments("org_demo")).isNotEmpty();
    }

}
