package jp.andpad.api.service;

import java.util.List;

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

@Service
@RequiredArgsConstructor
public class ConstructionService {

    private final ConstructionRepository constructionRepository;

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
        return constructionRepository.createModuleRecord(
                TenantContext.orgId(),
                input.projectId(),
                input.moduleCode(),
                input.title(),
                input.status(),
                input.detail(),
                input.amount(),
                input.personName(),
                Dates.parseDate(input.recordDate()));
    }
}
