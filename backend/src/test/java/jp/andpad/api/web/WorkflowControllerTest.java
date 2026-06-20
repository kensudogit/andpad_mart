package jp.andpad.api.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import jp.andpad.api.AbstractIntegrationTest;

class WorkflowControllerTest extends AbstractIntegrationTest {

    @Test
    void runsGenericSingleApprovalFlow() throws Exception {
        String token = loginToken();

        String startBody =
                """
                {"flowId":"generic-single-approval","entityType":"GENERIC","entityId":"wf-test-1","title":"テスト申請","payload":{"note":"demo"}}
                """;
        String started = mockMvc.perform(post("/api/workflow/instances/start")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(startBody))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(started).contains("\"status\":\"RUNNING\"");
        assertThat(started).contains("manager_approval");

        String instanceId = extractId(started, "wfi_");
        String taskId = extractId(started, "wft_");

        String completeBody = """
                {"action":"APPROVE","comment":"ok"}
                """;
        String approved = mockMvc.perform(post("/api/workflow/tasks/" + taskId + "/complete")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(completeBody))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(approved).contains("\"status\":\"APPROVED\"");

        mockMvc.perform(get("/api/workflow/instances/" + instanceId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void listsDefinitions() throws Exception {
        String token = loginToken();
        String body = mockMvc.perform(get("/api/workflow/definitions").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(body).contains("generic-single-approval");
    }

    private static String extractId(String json, String prefix) {
        int idx = json.indexOf(prefix);
        if (idx < 0) {
            throw new IllegalStateException("id prefix not found: " + prefix);
        }
        int end = json.indexOf('"', idx);
        return json.substring(idx, end);
    }
}
