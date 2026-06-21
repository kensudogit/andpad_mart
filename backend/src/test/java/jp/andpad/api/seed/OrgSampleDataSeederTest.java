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

    @Test
    void seedsMatterStampSamplesForDemoOrg() {
        orgSampleDataSeeder.seedForOrg(TenantContext.DEMO_ORG_ID);

        Integer count = jdbc.queryForObject(
                """
                SELECT COUNT(*) FROM wf_matter_stamps
                WHERE org_id = ? AND flow_id = 'document-approval'
                """,
                Integer.class,
                TenantContext.DEMO_ORG_ID);

        assertThat(count).isGreaterThanOrEqualTo(3);

        String stampType = jdbc.queryForObject(
                """
                SELECT stamp_type FROM wf_matter_stamps
                WHERE id = 'stamp-doc-demo-2'
                """,
                String.class);
        assertThat(stampType).isEqualTo("approveEnd");
    }
}
