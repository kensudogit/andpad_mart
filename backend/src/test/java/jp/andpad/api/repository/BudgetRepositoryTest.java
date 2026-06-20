package jp.andpad.api.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jp.andpad.api.AbstractIntegrationTest;

import jp.andpad.api.repository.BudgetRepository;

class BudgetRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    jp.andpad.api.repository.BudgetRepository budgetRepository;

    @Test
    void listsBudgets() {
        assertThat(budgetRepository.listBudgets("org_demo", "prj-demo-1", null)).isNotEmpty();
    }

}
