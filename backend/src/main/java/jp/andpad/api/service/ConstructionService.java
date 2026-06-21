package jp.andpad.api.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import jp.andpad.api.domain.ConstructionProject;
import jp.andpad.api.domain.ProjectModuleRecord;
import jp.andpad.api.domain.SaasModuleCode;
import jp.andpad.api.graphql.input.CreateConstructionProjectInput;
import jp.andpad.api.graphql.input.CreateProjectModuleRecordInput;
import jp.andpad.api.repository.ConstructionRepository;
import jp.andpad.api.security.TenantContext;
import jp.andpad.api.util.Dates;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConstructionService {

    private static final String DOC_APPROVAL_FLOW_ID = "document-approval";

    private final ConstructionRepository constructionRepository;
    private final WorkflowService workflowService;

    public List<ConstructionProject> listProjects() {
        return constructionRepository.listProjects(TenantContext.orgId());
    }

    public ConstructionProject createProject(CreateConstructionProjectInput input) {
        return constructionRepository.createProject(
                TenantContext.orgId(),
                input.name(),
                input.siteAddress(),
                input.status(),
                input.managerName(),
                Dates.parseDate(input.startDate()),
                Dates.parseDate(input.endDate()));
    }

    public List<ProjectModuleRecord> listModuleRecords(SaasModuleCode moduleCode, String projectId) {
        return constructionRepository.listModuleRecords(TenantContext.orgId(), moduleCode, projectId);
    }

    public ProjectModuleRecord createModuleRecord(CreateProjectModuleRecordInput input) {
        ProjectModuleRecord record = constructionRepository.createModuleRecord(
                TenantContext.orgId(),
                input.projectId(),
                input.moduleCode(),
                input.title(),
                input.status(),
                input.detail(),
                input.amount(),
                input.personName(),
                Dates.parseDate(input.recordDate()));
        if (input.moduleCode() == SaasModuleCode.DOC_APPROVAL) {
            startDocumentApprovalWorkflow(record);
        }
        return record;
    }

    private void startDocumentApprovalWorkflow(ProjectModuleRecord record) {
        try {
            workflowService.startWorkflow(
                    DOC_APPROVAL_FLOW_ID,
                    "DOCUMENT",
                    record.id(),
                    record.title(),
                    Map.of(
                            "projectId", record.projectId(),
                            "projectName", record.projectName(),
                            "detail", record.detail() != null ? record.detail() : "",
                            "personName", record.personName() != null ? record.personName() : "",
                            "status", record.status()),
                    null,
                    null);
        } catch (Exception ex) {
            log.warn(
                    "document-approval workflow/mail skipped for record {}: {}",
                    record.id(),
                    ex.getMessage());
        }
    }
}
