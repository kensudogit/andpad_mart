#!/usr/bin/env python3
"""Generate a test class for each main Java source under src/main/java."""
from __future__ import annotations

import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MAIN = ROOT / "src/main/java/jp/andpad/api"
TEST = ROOT / "src/test/java/jp/andpad/api"

ENUMS = {
    "MemberRole", "PlanTier", "SubscriptionStatus", "BudgetStatus", "BudgetType",
    "ConstructionProjectStatus", "CostEntryType", "LearningActivityKind",
    "SaasModuleCode", "SkillLevel", "VideoCategory",
}

UTILS = {"Dates", "Ids", "RagHelper", "DatabaseUrlSupport"}

INTEGRATION = {
    "AndpadApplication", "AuthController", "HealthController", "QueryController",
    "MutationController", "DataSourceConfig", "SecurityConfig", "JwtAuthFilter",
    "DemoSeeder", "DemoCatalog",
    "AuthRepository", "BudgetRepository", "ConstructionRepository", "ConsultRagRepository",
    "EngagementRepository", "ExtendedRepository", "LearningRepository",
    "OrganizationRepository", "SaasRepository",
    "AuthService", "BudgetService", "ConstructionService", "ConsultService",
    "ExtendedService", "LearningStubService", "OrganizationService",
    "RuntimeStatusService", "SaasService",
}

UNIT_SECURITY = {"JwtService", "TenantContext", "UnauthorizedException", "AuthPrincipal"}

NESTED_HOLDERS = {"ExtendedTypes", "LearningTypes", "LearningInputs"}

SKIP = {"DatabaseUrlSupport"}  # hand-written test exists

RECORD_FIELD_SAMPLES: dict[str, str] = {
    "User": 'new User("u1", "a@b.jp", "Name", null)',
    "Health": 'new Health(true, "andpad-api", "1.0")',
    "SaasModule": 'new SaasModule(SaasModuleCode.CONSTRUCTION_MGMT, "施工", "desc", true)',
    "UsageSummary": 'new UsageSummary(1, 10, 2, 100, 0, 1000, 0)',
    "MonthlyCostMetric": 'new MonthlyCostMetric("2024-01", 1000.0)',
    "ContractTemplate": 'new ContractTemplate("t1", "Name", "body", "2024-01-01")',
    "CrmInteraction": 'new CrmInteraction("i1", "c1", "CALL", "summary", "2024-01-01")',
    "LoginRequest": 'new LoginRequest("demo@sakura-dental.jp", "demo1234")',
    "RegisterRequest": 'new RegisterRequest("Clinic", "slug", "Owner", "a@b.jp", "pass1234")',
    "AppProperties": 'new AppProperties(new AppProperties.Jwt("secret-min-32-chars-long-enough", 72))',
    "AuthPrincipal": 'new AuthPrincipal("u1", "org1", "OWNER", "a@b.jp", "Name")',
    "CreateConstructionProjectInput": 'new CreateConstructionProjectInput("P", "Addr", ConstructionProjectStatus.PLANNING, "Mgr", "2024-01-01", "2024-12-31")',
    "CreateProjectModuleRecordInput": 'new CreateProjectModuleRecordInput("prj-demo-1", SaasModuleCode.BILLING, "Title", "OPEN", "detail", 100.0, "Person", "2024-01-01")',
    "CreateProjectBudgetInput": 'new CreateProjectBudgetInput("prj-demo-1", "Budget", BudgetType.EXECUTION_BUDGET, BudgetStatus.DRAFT, 1, 1000.0, "notes")',
    "CreateBudgetLineItemInput": 'new CreateBudgetLineItemInput("bud-demo-1", "DIRECT", "直接", "WBS", "desc", 100.0, 100.0, 0.0, 1)',
    "CreateCostEntryInput": 'new CreateCostEntryInput("prj-demo-1", "bli-demo-1", CostEntryType.MATERIAL, "Vendor", "desc", 1000, "2024-01-01", "INV-1", "recorder")',
    "CreateDxInitiativeInput": 'new CreateDxInitiativeInput("DX", "desc", "PLANNED", 0, "Owner", "2024-12-31")',
    "CreateCrmContactInput": 'new CreateCrmContactInput("Co", "contact@x.jp", "090", "Company", "LEAD", "NOTE")',
    "CreateLeaveRequestInput": 'new CreateLeaveRequestInput("2024-01-01", "2024-01-02", "reason")',
    "CreateContractInput": 'new CreateContractInput("tmpl-1", "Title", "Party", "party@x.jp", "body")',
    "UpdateOrganizationInput": 'new UpdateOrganizationInput("New Name", "sample-construction", 10, "Asia/Tokyo")',
}


def pkg_and_class(path: Path) -> tuple[str, str]:
    rel = path.relative_to(MAIN)
    class_name = path.stem
    pkg = "jp.andpad.api." + ".".join(rel.parts[:-1]).replace("\\", ".")
    if pkg.endswith("."):
        pkg = pkg[:-1]
    return pkg, class_name


def test_class_name(class_name: str) -> str:
    return class_name + "Test"


def imports_for(pkg: str, class_name: str, kind: str) -> list[str]:
    lines = ["import static org.assertj.core.api.Assertions.assertThat;", ""]
    if kind in ("integration", "unit_jwt"):
        lines.append("import org.junit.jupiter.api.Test;")
        lines.append("import org.springframework.beans.factory.annotation.Autowired;")
        lines.append("")
        lines.append("import jp.andpad.api.AbstractIntegrationTest;")
    elif kind == "unit_security":
        lines.append("import org.junit.jupiter.api.Test;")
        if class_name == "TenantContext":
            lines.extend([
                "import org.junit.jupiter.api.AfterEach;",
                "import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;",
                "import org.springframework.security.core.context.SecurityContextHolder;",
            ])
        if class_name == "UnauthorizedException":
            pass
        else:
            lines.append("import org.springframework.beans.factory.annotation.Autowired;")
            lines.append("")
            lines.append("import jp.andpad.api.AbstractIntegrationTest;")
    elif kind == "enum":
        lines.append("import org.junit.jupiter.api.Test;")
    else:
        lines.append("import org.junit.jupiter.api.Test;")
    lines.append("")
    lines.append(f"import {pkg}.{class_name};")
    extra = EXTRA_IMPORTS.get(class_name, [])
    for imp in extra:
        lines.append(f"import {imp};")
    return lines


EXTRA_IMPORTS: dict[str, list[str]] = {
    "SaasModule": ["jp.andpad.api.domain.SaasModuleCode"],
    "CreateConstructionProjectInput": ["jp.andpad.api.domain.ConstructionProjectStatus"],
    "CreateProjectModuleRecordInput": ["jp.andpad.api.domain.SaasModuleCode"],
    "CreateProjectBudgetInput": ["jp.andpad.api.domain.BudgetStatus", "jp.andpad.api.domain.BudgetType"],
    "CreateCostEntryInput": ["jp.andpad.api.domain.CostEntryType"],
    "UpdateOrganizationInput": [],
    "RagHelper": [
        "java.util.List",
        "jp.andpad.api.domain.ExtendedTypes.RagDocument",
        "jp.andpad.api.domain.ExtendedTypes.RagSearchHit",
    ],
    "Dates": ["java.time.Instant", "java.time.LocalDate"],
    "JwtService": ["jp.andpad.api.security.AuthPrincipal"],
    "TenantContext": ["jp.andpad.api.security.AuthPrincipal"],
    "DemoCatalog": ["jp.andpad.api.demo.DemoCatalog.CatalogVideo"],
    "LearningTypes": ["jp.andpad.api.domain.SkillLevel", "jp.andpad.api.domain.VideoCategory", "java.util.List"],
    "ExtendedTypes": ["jp.andpad.api.domain.ConstructionProjectStatus", "jp.andpad.api.domain.SaasModuleCode", "java.util.List"],
    "LearningInputs": [],
}


def body(class_name: str, pkg: str, kind: str) -> str:
    if kind == "enum":
        return f"""
    @Test
    void valuesAreDefined() {{
        assertThat({class_name}.values()).isNotEmpty();
        assertThat({class_name}.valueOf("{class_name}.values()[0].name()")).isNotNull();
    }}
""".replace("{class_name}.values()[0].name()", f"{{}}".format(""))  # fix below

    if kind == "enum":
        return f"""
    @Test
    void valuesAreDefined() {{
        assertThat({class_name}.values()).isNotEmpty();
        assertThat({class_name}.values()[0].name()).isNotBlank();
    }}
"""

    if kind == "record":
        sample = RECORD_FIELD_SAMPLES.get(class_name)
        if sample:
            return f"""
    @Test
    void createsRecord() {{
        var value = {sample};
        assertThat(value).isNotNull();
        assertThat(value.getClass()).isRecord();
    }}
"""
        return f"""
    @Test
    void isRecordType() {{
        assertThat({class_name}.class.isRecord()).isTrue();
    }}
"""

    if kind == "util":
        return UTIL_BODIES[class_name]

    if kind == "nested":
        return NESTED_BODIES[class_name]

    if kind == "integration":
        return INTEGRATION_BODIES.get(class_name, INTEGRATION_DEFAULT.format(cn=class_name))

    if class_name == "JwtService":
        return """
    @Autowired
    JwtService jwtService;

    @Test
    void issueAndParseToken() {
        AuthPrincipal principal = new AuthPrincipal("u1", "org1", "OWNER", "a@b.jp", "Name");
        String token = jwtService.issueToken(principal);
        AuthPrincipal parsed = jwtService.parseToken(token);
        assertThat(parsed.userId()).isEqualTo("u1");
        assertThat(parsed.orgId()).isEqualTo("org1");
    }
"""

    if class_name == "TenantContext":
        return """
    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void returnsDemoIdsWhenUnauthenticated() {
        assertThat(TenantContext.orgId()).isEqualTo(TenantContext.DEMO_ORG_ID);
        assertThat(TenantContext.userId()).isEqualTo(TenantContext.DEMO_USER_ID);
    }

    @Test
    void readsAuthenticatedPrincipal() {
        AuthPrincipal principal = new AuthPrincipal("u9", "org9", "OWNER", "a@b.jp", "Name");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null));
        assertThat(TenantContext.orgId()).isEqualTo("org9");
        assertThat(TenantContext.userId()).isEqualTo("u9");
    }
"""

    if class_name == "UnauthorizedException":
        return """
    @Test
    void carriesMessage() {
        assertThat(new UnauthorizedException("denied").getMessage()).isEqualTo("denied");
    }
"""

    if class_name == "AuthPrincipal":
        return """
    @Test
    void holdsFields() {
        AuthPrincipal p = new AuthPrincipal("u1", "org1", "OWNER", "a@b.jp", "Name");
        assertThat(p.userId()).isEqualTo("u1");
        assertThat(p.email()).isEqualTo("a@b.jp");
    }
"""

    return ""


INTEGRATION_DEFAULT = """
    @Autowired
    {cn} target;

    @Test
    void beanLoads() {{
        assertThat(target).isNotNull();
    }}
"""

INTEGRATION_BODIES: dict[str, str] = {
    "AndpadApplication": """
    @Test
    void contextLoads() {
        assertThat(mockMvc).isNotNull();
    }
""",
    "AuthController": """
    @Test
    void loginReturnsToken() throws Exception {
        String token = loginToken();
        assertThat(token).isNotBlank();
    }
""",
    "HealthController": """
    @Test
    void healthAndStatus() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/health"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/status"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.postgres").value(true));
    }
""",
    "QueryController": """
    @Test
    void constructionProjectsQuery() throws Exception {
        String token = loginToken();
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/graphql")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content("{\\"query\\":\\"{ constructionProjects { id } }\\"}"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.data.constructionProjects[0].id").value("prj-demo-1"));
    }
""",
    "MutationController": """
    @Test
    void updateOrganizationMutation() throws Exception {
        String token = loginToken();
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/graphql")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content("{\\"query\\":\\"mutation { updateOrganization(input: { name: \\\\\\"サンプル建設株式会社\\\\\\", slug: \\\\\\"sample-construction\\\\\\", seatCount: 10, timezone: \\\\\\"Asia/Tokyo\\\\\\" }) { id name } }\\"}"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.data.updateOrganization.id").value("org_demo"));
    }
""",
    "AuthRepository": """
    @Autowired
    jp.andpad.api.repository.AuthRepository authRepository;

    @Test
    void findsDemoUser() {
        assertThat(authRepository.findUserByEmail("demo@sakura-dental.jp")).isPresent();
    }
""",
    "OrganizationRepository": """
    @Autowired
    jp.andpad.api.repository.OrganizationRepository organizationRepository;

    @Test
    void demoOrganizationExists() {
        assertThat(organizationRepository.orgExists("org_demo")).isTrue();
    }
""",
    "ConstructionRepository": """
    @Autowired
    jp.andpad.api.repository.ConstructionRepository constructionRepository;

    @Test
    void listsDemoProjects() {
        assertThat(constructionRepository.listProjects("org_demo")).isNotEmpty();
    }
""",
    "LearningRepository": """
    @Autowired
    jp.andpad.api.repository.LearningRepository learningRepository;

    @Test
    void listsFeaturedVideos() {
        assertThat(learningRepository.featuredVideos("org_demo")).isNotEmpty();
    }
""",
    "ConsultRagRepository": """
    @Autowired
    jp.andpad.api.repository.ConsultRagRepository consultRagRepository;

    @Test
    void listsRagDocuments() {
        assertThat(consultRagRepository.listRagDocuments("org_demo")).isNotEmpty();
    }
""",
    "BudgetRepository": """
    @Autowired
    jp.andpad.api.repository.BudgetRepository budgetRepository;

    @Test
    void listsBudgets() {
        assertThat(budgetRepository.listBudgets("org_demo", "prj-demo-1", null)).isNotEmpty();
    }
""",
    "SaasRepository": """
    @Autowired
    jp.andpad.api.repository.SaasRepository saasRepository;

    @Test
    void listsModules() {
        assertThat(saasRepository.listOrgModules("org_demo")).isNotEmpty();
    }
""",
    "ExtendedRepository": """
    @Autowired
    jp.andpad.api.repository.ExtendedRepository extendedRepository;

    @Test
    void buildsAnalytics() {
        assertThat(extendedRepository.andpadAnalytics("org_demo", 30).activeProjects()).isGreaterThanOrEqualTo(0);
    }
""",
    "EngagementRepository": """
    @Autowired
    jp.andpad.api.repository.EngagementRepository engagementRepository;

    @Test
    void beanLoads() {
        assertThat(engagementRepository).isNotNull();
    }
""",
    "RuntimeStatusService": """
    @Autowired
    RuntimeStatusService runtimeStatusService;

    @Test
    void postgresConnected() {
        assertThat(runtimeStatusService.isPostgresConnected()).isTrue();
    }
""",
    "DemoCatalog": """
    @Test
    void catalogHasVideosAndPaths() {
        assertThat(DemoCatalog.videos()).hasSize(10);
        assertThat(DemoCatalog.paths()).hasSize(6);
        CatalogVideo v = DemoCatalog.videos().getFirst();
        assertThat(v.thumbnailUrl()).contains("youtube.com");
    }
""",
    "DemoSeeder": """
    @Autowired
    DemoSeeder demoSeeder;

    @Test
    void seederBeanLoads() {
        assertThat(demoSeeder).isNotNull();
    }
""",
    "DataSourceConfig": """
    @Autowired
    javax.sql.DataSource dataSource;

    @Test
    void dataSourceConnects() throws Exception {
        try (var c = dataSource.getConnection()) {
            assertThat(c.isValid(2)).isTrue();
        }
    }
""",
    "SecurityConfig": """
    @Test
    void healthIsPublic() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/health"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
    }
""",
    "JwtAuthFilter": """
    @Autowired
    JwtAuthFilter jwtAuthFilter;

    @Test
    void filterBeanLoads() {
        assertThat(jwtAuthFilter).isNotNull();
    }
""",
}

UTIL_BODIES = {
    "Dates": """
    @Test
    void formatsAndParsesDates() {
        Instant instant = Instant.parse("2024-01-01T00:00:00Z");
        assertThat(Dates.format(instant)).isEqualTo("2024-01-01T00:00:00Z");
        assertThat(Dates.parseInstant("2024-01-01T00:00:00Z")).isEqualTo(instant);
        LocalDate date = LocalDate.of(2024, 6, 1);
        assertThat(Dates.format(date)).isEqualTo("2024-06-01");
        assertThat(Dates.parseDate("2024-06-01")).isEqualTo(date);
    }
""",
    "Ids": """
    @Test
    void randomIdHasPrefix() {
        assertThat(Ids.random("test-")).startsWith("test-");
    }
""",
    "RagHelper": """
    @Test
    void localSearchFindsDocument() {
        var docs = List.of(new RagDocument("d1", "感染管理", "手指衛生の重要性", List.of("tag"), "2024-01-01"));
        List<RagSearchHit> hits = RagHelper.localSearch(docs, "感染", 3);
        assertThat(hits).hasSize(1);
        assertThat(RagHelper.formatHitsAsAnswer(hits)).contains("感染管理");
    }
""",
}

NESTED_BODIES = {
    "ExtendedTypes": """
    @Test
    void nestedRecordsConstruct() {
        var kpi = new ExtendedTypes.AndpadAnalyticsKpi("Projects", 3, "件", 1.0);
        assertThat(kpi.label()).isEqualTo("Projects");
        var doc = new ExtendedTypes.RagDocument("r1", "t", "c", List.of(), "2024-01-01");
        assertThat(doc.id()).isEqualTo("r1");
    }
""",
    "LearningTypes": """
    @Test
    void nestedRecordsConstruct() {
        var stats = new LearningTypes.DashboardStats(10, 6, 3, 1, 2.5, 4);
        assertThat(stats.videosTotal()).isEqualTo(10);
        var video = new LearningTypes.Video("v1", "t", "d", VideoCategory.ENDODONTICS, "p",
                SkillLevel.BEGINNER, 60, "thumb", "url", "i1", "inst", List.of(), 0, "2024-01-01", true);
        assertThat(video.id()).isEqualTo("v1");
    }
""",
    "LearningInputs": """
    @Test
    void nestedInputsConstruct() {
        var input = new LearningInputs.UpdateWatchProgressInput("v-1", "user_demo", 30, false);
        assertThat(input.videoId()).isEqualTo("v-1");
    }
""",
}


def classify(class_name: str, path: Path) -> str:
    if class_name in SKIP:
        return "skip"
    if class_name in ENUMS:
        return "enum"
    if class_name in UTILS:
        return "util"
    if class_name in NESTED_HOLDERS:
        return "nested"
    if class_name in INTEGRATION:
        return "integration"
    if class_name in UNIT_SECURITY:
        if class_name in {"JwtService"}:
            return "unit_jwt"
        if class_name in {"TenantContext", "UnauthorizedException", "AuthPrincipal"}:
            return "unit_security"
    if path.parent.name == "input" or path.parent.name == "dto" or path.parent.name == "domain" or path.parent.name == "config":
        if class_name.endswith("Input") or class_name.endswith("Request") or class_name.endswith("Response") or class_name == "AppProperties":
            return "record"
        if path.parent.name == "domain" and class_name not in ENUMS and class_name not in NESTED_HOLDERS:
            return "record"
    return "record"


def generate(path: Path) -> None:
    pkg, class_name = pkg_and_class(path)
    kind = classify(class_name, path)
    if kind == "skip":
        return

    test_pkg = pkg
    test_dir = TEST / path.relative_to(MAIN).parent
    test_dir.mkdir(parents=True, exist_ok=True)
    test_path = test_dir / (test_class_name(class_name) + ".java")

    extends = ""
    if kind in ("integration", "unit_jwt") or (kind == "unit_security" and class_name == "JwtService"):
        extends = " extends AbstractIntegrationTest"
    elif kind == "unit_security" and class_name == "TenantContext":
        extends = ""  # no spring for tenant default test - actually uses SecurityContextHolder only for second test
    elif kind == "integration" or (kind == "unit_security" and class_name not in {"UnauthorizedException", "AuthPrincipal", "TenantContext"}):
        extends = " extends AbstractIntegrationTest"

    if kind == "unit_security":
        if class_name in {"UnauthorizedException", "AuthPrincipal"}:
            extends = ""
        elif class_name == "TenantContext":
            extends = ""

    if kind == "integration" or class_name == "JwtService":
        extends = " extends AbstractIntegrationTest"

    imp_lines = imports_for(pkg, class_name, kind)
    if class_name in {"ExtendedTypes", "LearningTypes", "LearningInputs"}:
        imp_lines.append("import java.util.List;")

    content = f"""package {test_pkg};

{chr(10).join(imp_lines)}

class {test_class_name(class_name)}{extends} {{
{body(class_name, pkg, kind)}
}}
"""
    test_path.write_text(content, encoding="utf-8")
    print(f"Wrote {test_path.relative_to(ROOT)}")


def main() -> None:
    for path in sorted(MAIN.rglob("*.java")):
        generate(path)


if __name__ == "__main__":
    main()
