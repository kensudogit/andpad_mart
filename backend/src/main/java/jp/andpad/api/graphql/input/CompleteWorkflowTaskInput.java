package jp.andpad.api.graphql.input;

public record CompleteWorkflowTaskInput(String taskId, String action, String comment, String imSessionId) {}
