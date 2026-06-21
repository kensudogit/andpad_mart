package jp.andpad.api.seed;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import jp.andpad.api.AbstractIntegrationTest;
import jp.andpad.api.security.TenantContext;

class OrgSampleDataSeederTest extends AbstractIntegrationTest {

    @Autowired
    OrgSampleDataSeeder orgSampleDataSeeder;

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void seedsAllConstructionModuleRecordsForDemoOrg() {
        orgSampleDataSeeder.seedForOrg(TenantContext.DEMO_ORG_ID);

        Integer count = jdbc.queryForObject(
                """
                SELECT COUNT(DISTINCT module_code) FROM project_module_records
                WHERE org_id = ?
                """,
                Integer.class,
                TenantContext.DEMO_ORG_ID);

        assertThat(count).isGreaterThanOrEqualTo(15);
    }
}
