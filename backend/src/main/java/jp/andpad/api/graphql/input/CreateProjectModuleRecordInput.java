package jp.andpad.api.graphql.input;

import jp.andpad.api.domain.SaasModuleCode;

public record CreateProjectModuleRecordInput(
        String projectId,
        SaasModuleCode moduleCode,
        String title,
        String status,
        String detail,
        Double amount,
        String personName,
        String recordDate) {}
