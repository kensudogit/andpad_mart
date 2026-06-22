package jp.andpad.api.graphql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import jp.andpad.api.AbstractIntegrationTest;

class TenantApplicationMutationTest extends AbstractIntegrationTest {

    @Test
    void createTenantApplicationWithJapaneseName() throws Exception {
        String token = loginToken();
        var result = mockMvc.perform(
                        post("/graphql")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + token)
                                .content(
                                        """
                                        {
                                          "query": "mutation($input: CreateTenantApplicationInput!) { createTenantApplication(input: $input) { id name slug status } }",
                                          "variables": {
                                            "input": {
                                              "name": "テナント３",
                                              "address": "東京都港区",
                                              "contactName": "テナント３",
                                              "contactEmail": "kensudo1203@gmail.com",
                                              "contactPhone": "090-0000-0000",
                                              "ownerName": "テナント３ オーナ",
                                              "ownerEmail": "owner@example.com"
                                            }
                                          }
                                        }
                                        """))
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThat(body).doesNotContain("INTERNAL_ERROR");
        assertThat(body).contains("\"status\":\"DRAFT\"");
        assertThat(body).contains("\"name\":\"テナント３\"");
        assertThat(body).contains("\"slug\":\"tenant-");
    }
}
