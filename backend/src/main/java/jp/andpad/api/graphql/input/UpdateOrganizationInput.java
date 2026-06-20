package jp.andpad.api.graphql.input;

public record UpdateOrganizationInput(String name, String slug, Integer seatCount, String timezone) {}
