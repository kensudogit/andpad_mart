package jp.andpad.api.repository;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jp.andpad.api.domain.BillingReconciliationItem;
import jp.andpad.api.domain.BudgetCategorySummary;
import jp.andpad.api.domain.BudgetDashboard;
import jp.andpad.api.domain.BudgetLineItem;
import jp.andpad.api.domain.BudgetStatus;
import jp.andpad.api.domain.BudgetType;
import jp.andpad.api.domain.ConstructionProjectStatus;
import jp.andpad.api.domain.CostEntry;
import jp.andpad.api.domain.CostEntryType;
import jp.andpad.api.domain.MonthlyCostMetric;
import jp.andpad.api.domain.ProjectBudget;
import jp.andpad.api.domain.ProjectBudgetSummary;
import jp.andpad.api.util.Dates;
import jp.andpad.api.util.Ids;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BudgetRepository {

    private final JdbcTemplate jdbc;

    public List<ProjectBudget> listBudgets(String orgId, String projectId, BudgetType budgetType) {
        StringBuilder sql = new StringBuilder(
                """
                SELECT b.id, b.project_id, p.name AS project_name, b.name, b.budget_type, b.status,
                       b.version_no, b.contract_amount, b.notes, b.approved_at, b.created_at
                FROM project_budgets b
                JOIN construction_projects p ON p.id = b.project_id
                WHERE b.org_id = ?
                """);
        List<Object> args = new ArrayList<>();
        args.add(orgId);
        if (projectId != null && !projectId.isBlank()) {
            sql.append(" AND b.project_id = ?");
            args.add(projectId);
        }
        if (budgetType != null) {
            sql.append(" AND b.budget_type = ?");
            args.add(budgetType.name());
        }
        sql.append(" ORDER BY b.created_at DESC");
        List<ProjectBudget> budgets = jdbc.query(
                sql.toString(),
                (rs, rowNum) -> mapBudget(rs),
                args.toArray());
        return budgets.stream().map(b -> fillTotals(orgId, b)).toList();
    }

    public List<BudgetLineItem> listLineItems(String orgId, String budgetId) {
        return jdbc.query(
                """
                SELECT id, budget_id, category_code, category_name, wbs_code, description,
                       estimate_amount, budget_amount, committed_amount, actual_amount, sort_order, created_at
                FROM budget_line_items
                WHERE org_id = ? AND budget_id = ?
                ORDER BY sort_order, created_at
                """,
                (rs, rowNum) -> mapLineItem(rs),
                orgId,
                budgetId);
    }

    public List<CostEntry> listCostEntries(String orgId, String projectId, String lineItemId) {
        StringBuilder sql = new StringBuilder(
                """
                SELECT c.id, c.project_id, p.name AS project_name, COALESCE(c.line_item_id, '') AS line_item_id,
                       COALESCE(l.category_name || ' ' || l.description, '') AS line_item_name,
                       c.entry_type, c.vendor_name, c.description, c.amount, c.entry_date,
                       c.invoice_no, c.recorded_by, c.created_at
                FROM cost_entries c
                JOIN construction_projects p ON p.id = c.project_id
                LEFT JOIN budget_line_items l ON l.id = c.line_item_id
                WHERE c.org_id = ? AND c.project_id = ?
                """);
        List<Object> args = new ArrayList<>();
        args.add(orgId);
        args.add(projectId);
        if (lineItemId != null && !lineItemId.isBlank()) {
            sql.append(" AND c.line_item_id = ?");
            args.add(lineItemId);
        }
        sql.append(" ORDER BY c.entry_date DESC, c.created_at DESC");
        return jdbc.query(sql.toString(), (rs, rowNum) -> mapCostEntry(rs), args.toArray());
    }

    public BudgetDashboard budgetDashboard(String orgId, String projectId) {
        String projectName = jdbc.queryForObject(
                "SELECT name FROM construction_projects WHERE id = ? AND org_id = ?",
                String.class,
                projectId,
                orgId);
        BudgetDashboard empty = emptyDashboard(projectId, projectName);
        try {
            var row = jdbc.queryForMap(
                    """
                    SELECT id, contract_amount FROM project_budgets
                    WHERE org_id = ? AND project_id = ? AND budget_type = 'EXECUTION_BUDGET'
                    ORDER BY version_no DESC, created_at DESC LIMIT 1
                    """,
                    orgId,
                    projectId);
            String budgetId = (String) row.get("id");
            double contractAmount = ((Number) row.get("contract_amount")).doubleValue();
            List<BudgetLineItem> lineItems = listLineItems(orgId, budgetId);
            Map<String, BudgetCategorySummary> catMap = new HashMap<>();
            double totalEstimate = 0;
            double totalBudget = 0;
            double totalCommitted = 0;
            double totalActual = 0;
            for (BudgetLineItem item : lineItems) {
                totalEstimate += item.estimateAmount();
                totalBudget += item.budgetAmount();
                totalCommitted += item.committedAmount();
                totalActual += item.actualAmount();
                catMap.compute(item.categoryCode(), (k, v) -> {
                    if (v == null) {
                        return new BudgetCategorySummary(
                                item.categoryCode(), item.categoryName(),
                                item.budgetAmount(), item.actualAmount(),
                                item.budgetAmount() - item.actualAmount());
                    }
                    return new BudgetCategorySummary(
                            v.categoryCode(), v.categoryName(),
                            v.budgetAmount() + item.budgetAmount(),
                            v.actualAmount() + item.actualAmount(),
                            v.budgetAmount() + item.budgetAmount() - v.actualAmount() - item.actualAmount());
                });
            }
            double totalForecast = totalActual + (totalBudget - totalCommitted);
            double varianceAmount = totalBudget - totalActual;
            double variancePct = totalBudget > 0 ? varianceAmount / totalBudget * 100 : 0;
            double completionPct = totalBudget > 0 ? totalActual / totalBudget * 100 : 0;
            double grossMarginPct = contractAmount > 0 ? (contractAmount - totalBudget) / contractAmount * 100 : 0;
            double estimateBudgetTotal = queryEstimateTotal(orgId, projectId);
            double inquiryProfitTotal = sumModuleAmount(orgId, projectId, "INQUIRY_PROFIT");
            double billingTotal = sumModuleAmount(orgId, projectId, "BILLING");
            List<CostEntry> costs = listCostEntries(orgId, projectId, null);
            List<CostEntry> recent = costs.size() > 10 ? costs.subList(0, 10) : costs;
            return new BudgetDashboard(
                    projectId, projectName, contractAmount, totalEstimate, totalBudget, totalCommitted,
                    totalActual, totalForecast, varianceAmount, variancePct, completionPct,
                    estimateBudgetTotal, grossMarginPct, inquiryProfitTotal, billingTotal,
                    billingTotal - totalActual, monthlyCosts(orgId, projectId, 6),
                    billingReconciliation(orgId, projectId), lineItems, recent,
                    new ArrayList<>(catMap.values()), Dates.format(Dates.now()));
        } catch (EmptyResultDataAccessException ex) {
            return empty;
        }
    }

    public List<ProjectBudgetSummary> listBudgetSummaries(String orgId) {
        List<ProjectBudgetSummary> summaries = jdbc.query(
                """
                SELECT p.id, p.name, p.status,
                       COALESCE(MAX(b.contract_amount), 0) AS contract_amount,
                       COALESCE(SUM(li.budget_amount), 0) AS total_budget,
                       COALESCE(SUM(li.actual_amount), 0) AS total_actual
                FROM construction_projects p
                LEFT JOIN project_budgets b ON b.project_id = p.id AND b.org_id = p.org_id
                    AND b.budget_type = 'EXECUTION_BUDGET' AND b.status = 'APPROVED'
                LEFT JOIN budget_line_items li ON li.budget_id = b.id
                WHERE p.org_id = ?
                GROUP BY p.id, p.name, p.status
                ORDER BY p.name
                """,
                (rs, rowNum) -> {
                    double totalBudget = rs.getDouble("total_budget");
                    double totalActual = rs.getDouble("total_actual");
                    double variancePct = totalBudget > 0 ? (totalBudget - totalActual) / totalBudget * 100 : 0;
                    return new ProjectBudgetSummary(
                            rs.getString("id"),
                            rs.getString("name"),
                            ConstructionProjectStatus.valueOf(rs.getString("status")),
                            rs.getDouble("contract_amount"),
                            totalBudget,
                            totalActual,
                            0,
                            variancePct);
                },
                orgId);
        return summaries.stream()
                .map(summary -> new ProjectBudgetSummary(
                        summary.projectId(),
                        summary.projectName(),
                        summary.status(),
                        summary.contractAmount(),
                        summary.totalBudget(),
                        summary.totalActual(),
                        sumModuleAmount(orgId, summary.projectId(), "BILLING"),
                        summary.variancePct()))
                .toList();
    }

    @Transactional
    public ProjectBudget createBudget(String orgId, String projectId, String name, BudgetType budgetType,
            BudgetStatus status, int versionNo, double contractAmount, String notes) {
        String id = Ids.random("bud_");
        BudgetType bt = budgetType == null ? BudgetType.EXECUTION_BUDGET : budgetType;
        BudgetStatus st = status == null ? BudgetStatus.DRAFT : status;
        int version = versionNo <= 0 ? 1 : versionNo;
        String projectName = jdbc.queryForObject(
                "SELECT name FROM construction_projects WHERE id = ? AND org_id = ?",
                String.class,
                projectId,
                orgId);
        jdbc.update(
                """
                INSERT INTO project_budgets (id, org_id, project_id, name, budget_type, status, version_no,
                    contract_amount, notes)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                id, orgId, projectId, name, bt.name(), st.name(), version, contractAmount,
                notes == null ? "" : notes);
        return new ProjectBudget(
                id, projectId, projectName, name, bt, st, version, contractAmount,
                0, 0, 0, 0, notes == null ? "" : notes, null, Dates.format(Dates.now()));
    }

    @Transactional
    public BudgetLineItem createLineItem(String orgId, String budgetId, String categoryCode, String categoryName,
            String wbsCode, String description, double estimateAmount, double budgetAmount,
            double committedAmount, int sortOrder) {
        String id = Ids.random("bli_");
        jdbc.update(
                """
                INSERT INTO budget_line_items (id, org_id, budget_id, category_code, category_name, wbs_code,
                    description, estimate_amount, budget_amount, committed_amount, sort_order)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                id, orgId, budgetId, categoryCode, categoryName,
                wbsCode == null ? "" : wbsCode,
                description == null ? "" : description,
                estimateAmount, budgetAmount, committedAmount, sortOrder);
        return mapLineItemVariance(new BudgetLineItem(
                id, budgetId, categoryCode, categoryName,
                wbsCode == null ? "" : wbsCode, description == null ? "" : description,
                estimateAmount, budgetAmount, committedAmount, 0, 0, 0, sortOrder,
                Dates.format(Dates.now())));
    }

    @Transactional
    public CostEntry createCostEntry(String orgId, String projectId, String lineItemId, CostEntryType entryType,
            String vendorName, String description, double amount, LocalDate entryDate,
            String invoiceNo, String recordedBy) {
        String id = Ids.random("cost_");
        CostEntryType et = entryType == null ? CostEntryType.OTHER : entryType;
        LocalDate date = entryDate == null ? LocalDate.now() : entryDate;
        String projectName = jdbc.queryForObject(
                "SELECT name FROM construction_projects WHERE id = ? AND org_id = ?",
                String.class,
                projectId,
                orgId);
        jdbc.update(
                """
                INSERT INTO cost_entries (id, org_id, project_id, line_item_id, entry_type, vendor_name,
                    description, amount, entry_date, invoice_no, recorded_by)
                VALUES (?, ?, ?, NULLIF(?, ''), ?, ?, ?, ?, ?, ?, ?)
                """,
                id, orgId, projectId, lineItemId == null ? "" : lineItemId, et.name(),
                vendorName == null ? "" : vendorName, description, amount, Date.valueOf(date),
                invoiceNo == null ? "" : invoiceNo, recordedBy == null ? "" : recordedBy);
        if (lineItemId != null && !lineItemId.isBlank()) {
            jdbc.update(
                    "UPDATE budget_line_items SET actual_amount = actual_amount + ? WHERE id = ? AND org_id = ?",
                    amount, lineItemId, orgId);
        }
        String lineItemName = "";
        if (lineItemId != null && !lineItemId.isBlank()) {
            try {
                lineItemName = jdbc.queryForObject(
                        "SELECT category_name || ' ' || description FROM budget_line_items WHERE id = ?",
                        String.class,
                        lineItemId);
            } catch (EmptyResultDataAccessException ignored) {
            }
        }
        return new CostEntry(
                id, projectId, projectName, lineItemId == null ? "" : lineItemId, lineItemName,
                et, vendorName == null ? "" : vendorName, description, amount, date.toString(),
                invoiceNo == null ? "" : invoiceNo, recordedBy == null ? "" : recordedBy,
                Dates.format(Dates.now()));
    }

    @Transactional
    public ProjectBudget approveBudget(String orgId, String id) {
        jdbc.update(
                "UPDATE project_budgets SET status = 'APPROVED', approved_at = NOW() WHERE id = ? AND org_id = ?",
                id, orgId);
        var budget = jdbc.queryForMap(
                """
                SELECT b.id, b.project_id, p.name AS project_name, b.name, b.budget_type, b.status,
                       b.version_no, b.contract_amount, b.notes, b.approved_at, b.created_at
                FROM project_budgets b
                JOIN construction_projects p ON p.id = b.project_id
                WHERE b.id = ? AND b.org_id = ?
                """,
                id, orgId);
        return fillTotals(orgId, mapBudgetFromMap(budget));
    }

    @Transactional
    public CostEntry createCostFromBilling(String orgId, String billingRecordId, String projectId) {
        var row = jdbc.queryForMap(
                """
                SELECT title, detail, COALESCE(amount, 0) AS amount, record_date
                FROM project_module_records
                WHERE id = ? AND org_id = ? AND project_id = ? AND module_code = 'BILLING'
                """,
                billingRecordId, orgId, projectId);
        String title = (String) row.get("title");
        String detail = (String) row.get("detail");
        double amount = ((Number) row.get("amount")).doubleValue();
        LocalDate entryDate = row.get("record_date") instanceof Date d ? d.toLocalDate() : LocalDate.now();
        String desc = detail == null || detail.isBlank() ? title : title + " — " + detail;
        return createCostEntry(orgId, projectId, null, CostEntryType.OTHER, "請求連携",
                desc, amount, entryDate, "BILL-" + billingRecordId, null);
    }

    private ProjectBudget fillTotals(String orgId, ProjectBudget budget) {
        var totals = jdbc.queryForMap(
                """
                SELECT COALESCE(SUM(estimate_amount), 0) AS total_estimate,
                       COALESCE(SUM(budget_amount), 0) AS total_budget,
                       COALESCE(SUM(committed_amount), 0) AS total_committed,
                       COALESCE(SUM(actual_amount), 0) AS total_actual
                FROM budget_line_items WHERE budget_id = ? AND org_id = ?
                """,
                budget.id(), orgId);
        return new ProjectBudget(
                budget.id(), budget.projectId(), budget.projectName(), budget.name(),
                budget.budgetType(), budget.status(), budget.versionNo(), budget.contractAmount(),
                ((Number) totals.get("total_estimate")).doubleValue(),
                ((Number) totals.get("total_budget")).doubleValue(),
                ((Number) totals.get("total_committed")).doubleValue(),
                ((Number) totals.get("total_actual")).doubleValue(),
                budget.notes(), budget.approvedAt(), budget.createdAt());
    }

    private ProjectBudget mapBudget(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new ProjectBudget(
                rs.getString("id"),
                rs.getString("project_id"),
                rs.getString("project_name"),
                rs.getString("name"),
                BudgetType.valueOf(rs.getString("budget_type")),
                BudgetStatus.valueOf(rs.getString("status")),
                rs.getInt("version_no"),
                rs.getDouble("contract_amount"),
                0, 0, 0, 0,
                rs.getString("notes"),
                rs.getTimestamp("approved_at") == null ? null : formatTimestamp(rs.getTimestamp("approved_at")),
                formatTimestamp(rs.getTimestamp("created_at")));
    }

    private ProjectBudget mapBudgetFromMap(Map<String, Object> row) {
        Instant approved = row.get("approved_at") instanceof java.sql.Timestamp ts ? ts.toInstant() : null;
        Instant created = row.get("created_at") instanceof java.sql.Timestamp ts2 ? ts2.toInstant() : Dates.now();
        return new ProjectBudget(
                (String) row.get("id"),
                (String) row.get("project_id"),
                (String) row.get("project_name"),
                (String) row.get("name"),
                BudgetType.valueOf((String) row.get("budget_type")),
                BudgetStatus.valueOf((String) row.get("status")),
                ((Number) row.get("version_no")).intValue(),
                ((Number) row.get("contract_amount")).doubleValue(),
                0, 0, 0, 0,
                (String) row.get("notes"),
                approved == null ? null : Dates.format(approved),
                Dates.format(created));
    }

    private BudgetLineItem mapLineItem(java.sql.ResultSet rs) throws java.sql.SQLException {
        return mapLineItemVariance(new BudgetLineItem(
                rs.getString("id"),
                rs.getString("budget_id"),
                rs.getString("category_code"),
                rs.getString("category_name"),
                rs.getString("wbs_code"),
                rs.getString("description"),
                rs.getDouble("estimate_amount"),
                rs.getDouble("budget_amount"),
                rs.getDouble("committed_amount"),
                rs.getDouble("actual_amount"),
                0,
                0,
                rs.getInt("sort_order"),
                formatTimestamp(rs.getTimestamp("created_at"))));
    }

    private BudgetLineItem mapLineItemVariance(BudgetLineItem item) {
        double variance = item.budgetAmount() - item.actualAmount();
        double variancePct = item.budgetAmount() > 0 ? variance / item.budgetAmount() * 100 : 0;
        return new BudgetLineItem(
                item.id(), item.budgetId(), item.categoryCode(), item.categoryName(),
                item.wbsCode(), item.description(), item.estimateAmount(), item.budgetAmount(),
                item.committedAmount(), item.actualAmount(), variance, variancePct,
                item.sortOrder(), item.createdAt());
    }

    private CostEntry mapCostEntry(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new CostEntry(
                rs.getString("id"),
                rs.getString("project_id"),
                rs.getString("project_name"),
                rs.getString("line_item_id"),
                rs.getString("line_item_name"),
                CostEntryType.valueOf(rs.getString("entry_type")),
                rs.getString("vendor_name"),
                rs.getString("description"),
                rs.getDouble("amount"),
                formatDate(rs.getDate("entry_date")),
                rs.getString("invoice_no"),
                rs.getString("recorded_by"),
                formatTimestamp(rs.getTimestamp("created_at")));
    }

    private double queryEstimateTotal(String orgId, String projectId) {
        try {
            return jdbc.queryForObject(
                    """
                    SELECT COALESCE(SUM(bli.budget_amount), 0) FROM budget_line_items bli
                    WHERE bli.org_id = ? AND bli.budget_id = (
                        SELECT id FROM project_budgets
                        WHERE org_id = ? AND project_id = ? AND budget_type = 'ESTIMATE'
                        ORDER BY version_no DESC LIMIT 1
                    )
                    """,
                    Double.class,
                    orgId, orgId, projectId);
        } catch (EmptyResultDataAccessException ex) {
            return 0;
        }
    }

    private double sumModuleAmount(String orgId, String projectId, String moduleCode) {
        try {
            return jdbc.queryForObject(
                    """
                    SELECT COALESCE(SUM(amount), 0) FROM project_module_records
                    WHERE org_id = ? AND project_id = ? AND module_code = ?
                    """,
                    Double.class,
                    orgId, projectId, moduleCode);
        } catch (EmptyResultDataAccessException ex) {
            return 0;
        }
    }

    private List<MonthlyCostMetric> monthlyCosts(String orgId, String projectId, int months) {
        LocalDate since = LocalDate.now().minusMonths(months - 1L).withDayOfMonth(1);
        Map<String, Double> byMonth = new HashMap<>();
        jdbc.query(
                """
                SELECT to_char(date_trunc('month', entry_date), 'YYYY-MM') AS month,
                       COALESCE(SUM(amount), 0) AS amount
                FROM cost_entries
                WHERE org_id = ? AND project_id = ? AND entry_date >= ?
                GROUP BY 1 ORDER BY 1
                """,
                rs -> {
                    byMonth.put(rs.getString("month"), rs.getDouble("amount"));
                },
                orgId, projectId, Date.valueOf(since));
        List<MonthlyCostMetric> out = new ArrayList<>();
        YearMonth start = YearMonth.from(since);
        for (int i = 0; i < months; i++) {
            String key = start.plusMonths(i).toString();
            out.add(new MonthlyCostMetric(key, byMonth.getOrDefault(key, 0.0)));
        }
        return out;
    }

    private List<BillingReconciliationItem> billingReconciliation(String orgId, String projectId) {
        Map<String, Double> costByMonth = new HashMap<>();
        jdbc.query(
                """
                SELECT to_char(date_trunc('month', entry_date), 'YYYY-MM') AS month,
                       COALESCE(SUM(amount), 0) AS amount
                FROM cost_entries WHERE org_id = ? AND project_id = ?
                GROUP BY 1
                """,
                rs -> {
                    while (rs.next()) {
                        costByMonth.put(rs.getString("month"), rs.getDouble("amount"));
                    }
                },
                orgId, projectId);
        return jdbc.query(
                """
                SELECT id, title, COALESCE(amount, 0) AS amount, record_date
                FROM project_module_records
                WHERE org_id = ? AND project_id = ? AND module_code = 'BILLING'
                ORDER BY record_date DESC NULLS LAST, created_at DESC
                """,
                (rs, rowNum) -> {
                    double billingAmount = rs.getDouble("amount");
                    String monthKey = "";
                    String billingDate = null;
                    if (rs.getDate("record_date") != null) {
                        LocalDate d = rs.getDate("record_date").toLocalDate();
                        billingDate = d.toString();
                        monthKey = YearMonth.from(d).toString();
                    }
                    double costAmount = costByMonth.getOrDefault(monthKey, 0.0);
                    double variance = billingAmount - costAmount;
                    String status;
                    if (billingAmount == 0) {
                        status = "NONE";
                    } else if (costAmount == 0) {
                        status = "UNMATCHED";
                    } else if (Math.abs(variance / billingAmount * 100) <= 5) {
                        status = "MATCHED";
                    } else if (variance > 0) {
                        status = "UNDER";
                    } else {
                        status = "OVER";
                    }
                    return new BillingReconciliationItem(
                            rs.getString("id"), rs.getString("title"), billingAmount,
                            costAmount, variance, status, billingDate);
                },
                orgId, projectId);
    }

    private static BudgetDashboard emptyDashboard(String projectId, String projectName) {
        return new BudgetDashboard(
                projectId, projectName, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                List.of(), List.of(), List.of(), List.of(), List.of(), Dates.format(Dates.now()));
    }

    private static String formatTimestamp(java.sql.Timestamp ts) {
        return ts == null ? Dates.format(Dates.now()) : Dates.format(ts.toInstant());
    }

    private static String formatDate(Date date) {
        return date == null ? null : date.toLocalDate().toString();
    }
}
