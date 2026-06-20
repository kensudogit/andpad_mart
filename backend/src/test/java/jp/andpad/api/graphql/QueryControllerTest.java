package jp.andpad.api.graphql;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jp.andpad.api.AbstractIntegrationTest;

import jp.andpad.api.graphql.QueryController;

class QueryControllerTest extends AbstractIntegrationTest {

    @Test
    void currentSessionWithoutAuthReturnsNull() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/graphql")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"query\":\"{ currentSession { user { email } } }\"}"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.data.currentSession").isEmpty());
    }

    @Test
    void consultThreadsWithoutAuthReturnsUnauthorized() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/graphql")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"query\":\"{ consultThreads { id } }\"}"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.errors[0].message")
                        .value("authentication required"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.errors[0].extensions.classification")
                        .value("UNAUTHORIZED"));
    }

    @Test
    void constructionProjectsQuery() throws Exception {
        String token = loginToken();
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/graphql")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content("{\"query\":\"{ constructionProjects { id } }\"}"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.data.constructionProjects[0].id").value("prj-demo-1"));
    }

}
