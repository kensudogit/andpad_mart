package jp.andpad.api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import jp.andpad.api.domain.BudgetDashboard;
import jp.andpad.api.domain.BudgetLineItem;
import jp.andpad.api.domain.BudgetType;
import jp.andpad.api.domain.CostEntry;
import jp.andpad.api.domain.ProjectBudget;
import jp.andpad.api.domain.ProjectBudgetSummary;
import jp.andpad.api.graphql.input.CreateBudgetLineItemInput;
import jp.andpad.api.graphql.input.CreateCostEntryInput;
import jp.andpad.api.graphql.input.CreateProjectBudgetInput;
import jp.andpad.api.repository.BudgetRepository;
import jp.andpad.api.security.TenantContext;
import jp.andpad.api.util.Dates;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;

    public List<ProjectBudget> listBudgets(String projectId, BudgetType budgetType) {
        return budgetRepository.listBudgets(TenantContext.orgId(), projectId, budgetType);
    }

    public List<BudgetLineItem> listLineItems(String budgetId) {
        return budgetRepository.listLineItems(TenantContext.orgId(), budgetId);
    }

    public List<CostEntry> listCostEntries(String projectId, String lineItemId) {
        return budgetRepository.listCostEntries(TenantContext.orgId(), projectId, lineItemId);
    }

    public BudgetDashboard budgetDashboard(String projectId) {
        return budgetRepository.budgetDashboard(TenantContext.orgId(), projectId);
    }

    public List<ProjectBudgetSummary> listBudgetSummaries() {
        return budgetRepository.listBudgetSummaries(TenantContext.orgId());
    }

    public ProjectBudget createBudget(CreateProjectBudgetInput input) {
        return budgetRepository.createBudget(
                TenantContext.orgId(),
                input.projectId(),
                input.name(),
                input.budgetType(),
                input.status(),
                input.versionNo() == null ? 1 : input.versionNo(),
                input.contractAmount() == null ? 0 : input.contractAmount(),
                input.notes());
    }

    public BudgetLineItem createLineItem(CreateBudgetLineItemInput input) {
        return budgetRepository.createLineItem(
                TenantContext.orgId(),
                input.budgetId(),
                input.categoryCode(),
                input.categoryName(),
                input.wbsCode(),
                input.description(),
                input.estimateAmount() == null ? 0 : input.estimateAmount(),
                input.budgetAmount() == null ? 0 : input.budgetAmount(),
                input.committedAmount() == null ? 0 : input.committedAmount(),
                input.sortOrder() == null ? 0 : input.sortOrder());
    }

    public CostEntry createCostEntry(CreateCostEntryInput input) {
        return budgetRepository.createCostEntry(
                TenantContext.orgId(),
                input.projectId(),
                input.lineItemId(),
                input.entryType(),
                input.vendorName(),
                input.description(),
                input.amount(),
                Dates.parseDate(input.entryDate()),
                input.invoiceNo(),
                input.recordedBy());
    }

    public ProjectBudget approveBudget(String id) {
        return budgetRepository.approveBudget(TenantContext.orgId(), id);
    }

    public CostEntry createCostFromBilling(String billingRecordId, String projectId) {
        return budgetRepository.createCostFromBilling(TenantContext.orgId(), billingRecordId, projectId);
    }
}
