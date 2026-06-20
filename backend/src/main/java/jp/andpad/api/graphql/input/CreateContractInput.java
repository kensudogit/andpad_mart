package jp.andpad.api.graphql.input;

public record CreateContractInput(
        String templateId, String title, String partyName, String partyEmail, String body) {}
