package jp.andpad.api.web;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jp.andpad.api.domain.WorkflowTypes.WorkflowDefinitionView;
import jp.andpad.api.domain.WorkflowTypes.WorkflowInstanceView;
import jp.andpad.api.domain.WorkflowTypes.WorkflowTaskView;
import jp.andpad.api.service.WorkflowService;
import jp.andpad.imart.workflow.model.WorkflowAction;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/workflow")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;

    @GetMapping("/definitions")
    public List<WorkflowDefinitionView> definitions() {
        return workflowService.listDefinitions();
    }

    @GetMapping("/instances")
    public List<WorkflowInstanceView> instances(
            @RequestParam(required = false) String entityType, @RequestParam(required = false) String status) {
        return workflowService.listInstances(entityType, status);
    }

    @GetMapping("/instances/{id}")
    public WorkflowInstanceView instance(@PathVariable String id) {
        return workflowService.getInstance(id);
    }

    @GetMapping("/tasks/my")
    public List<WorkflowTaskView> myTasks() {
        return workflowService.myPendingTasks();
    }

    @PostMapping("/instances/start")
    public WorkflowInstanceView start(
            @RequestHeader(value = "X-IM-Session-Id", required = false) String imSessionId,
            @RequestBody StartWorkflowBody body) {
        return workflowService.startWorkflow(
                body.flowId(),
                body.entityType(),
                body.entityId(),
                body.title(),
                body.payload(),
                imSessionId,
                body.imSystemMatterId());
    }

    @PostMapping("/instances/{id}/submit")
    public WorkflowInstanceView submit(
            @PathVariable String id,
            @RequestHeader(value = "X-IM-Session-Id", required = false) String imSessionId) {
        return workflowService.submitWorkflow(id, imSessionId);
    }

    @PostMapping("/tasks/{taskId}/complete")
    public WorkflowInstanceView completeTask(
            @PathVariable String taskId,
            @RequestHeader(value = "X-IM-Session-Id", required = false) String imSessionId,
            @RequestBody CompleteTaskBody body) {
        WorkflowAction action = WorkflowAction.valueOf(body.action().toUpperCase());
        return workflowService.completeTask(taskId, action, body.comment(), imSessionId);
    }

    public record StartWorkflowBody(
            String flowId,
            String entityType,
            String entityId,
            String title,
            Map<String, Object> payload,
            String imSystemMatterId) {}

    public record CompleteTaskBody(String action, String comment) {}
}
