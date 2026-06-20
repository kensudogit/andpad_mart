package jp.andpad.api.graphql;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jp.andpad.api.AbstractIntegrationTest;

import jp.andpad.api.graphql.MutationController;

class MutationControllerTest extends AbstractIntegrationTest {

    @Test
    void updateOrganizationMutation() throws Exception {
        String token = loginToken();
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/graphql")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content("{\"query\":\"mutation { updateOrganization(input: { name: \\\"サンプル建設株式会社\\\", slug: \\\"sample-construction\\\", seatCount: 10, timezone: \\\"Asia/Tokyo\\\" }) { id name } }\"}"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.data.updateOrganization.id").value("org_demo"));
    }

}
