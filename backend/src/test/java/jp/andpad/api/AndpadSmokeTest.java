package jp.andpad.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

/** 主要フローの統合スモークテスト（個別 *Test クラスと併用）。 */
class AndpadSmokeTest extends AbstractIntegrationTest {

    @Autowired
    private jp.andpad.api.repository.AuthRepository authRepository;

    @Test
    void healthReturnsOk() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/health"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.ok")
                        .value(true));
    }

    @Test
    void loginAndQueryConstructionProjects() throws Exception {
        String token = loginToken();

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/graphql")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + token)
                                .content("""
                                        {"query":"{ constructionProjects { id name status } }"}
                                        """))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath(
                                "$.data.constructionProjects[0].id")
                        .value("prj-demo-1"));
    }

    @Test
    void loginAndQueryLearningCatalog() throws Exception {
        String token = loginToken();

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/graphql")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + token)
                                .content("""
                                        {"query":"{ dashboard { videosTotal learningPathsTotal } featuredVideos { id featured } learningPaths { id videoIds } }"}
                                        """))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath(
                                "$.data.dashboard.videosTotal")
                        .value(10))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath(
                                "$.data.dashboard.learningPathsTotal")
                        .value(6))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath(
                                "$.data.featuredVideos[0].id")
                        .value("v-1"));
    }

    @Test
    void loginAndQueryRagDocuments() throws Exception {
        String token = loginToken();

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/graphql")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + token)
                                .content("""
                                        {"query":"{ ragDocuments { id title } ragSearch(query: \\"感染\\", limit: 3) { title score } }"}
                                        """))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath(
                                "$.data.ragDocuments[0].id")
                        .value("rag-1"));
    }

    @Test
    void authRepositoryFindsDemoUser() {
        assertThat(authRepository.findUserByEmail("demo@sakura-dental.jp")).isPresent();
    }
}
