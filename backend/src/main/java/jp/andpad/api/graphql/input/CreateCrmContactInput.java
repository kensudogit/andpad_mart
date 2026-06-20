package jp.andpad.api.graphql.input;

public record CreateCrmContactInput(
        String name, String email, String phone, String company, String stage, String notes) {}
