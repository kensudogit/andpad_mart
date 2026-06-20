package jp.andpad.api.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jp.andpad.api.AbstractIntegrationTest;

import jp.andpad.api.repository.SaasRepository;

class SaasRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    jp.andpad.api.repository.SaasRepository saasRepository;

    @Test
    void listsModules() {
        assertThat(saasRepository.listOrgModules("org_demo")).isNotEmpty();
    }

}
