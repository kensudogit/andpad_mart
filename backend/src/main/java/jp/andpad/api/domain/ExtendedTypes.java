package jp.andpad.api.domain;

import java.util.List;

public final class ExtendedTypes {

    private ExtendedTypes() {}

    public record AndpadAnalyticsKpi(String label, double value, String unit, Double trendPct) {}

    public record ProjectStatusCount(ConstructionProjectStatus status, int count) {}

    public record ModuleUsageMetric(SaasModuleCode moduleCode, String moduleName, int recordCount) {}

    public record AndpadAnalyticsDashboard(
            int periodDays,
            List<AndpadAnalyticsKpi> kpis,
            List<ProjectStatusCount> projectsByStatus,
            List<ModuleUsageMetric> moduleUsage,
            double billingTotal,
            int activeProjects,
            List<Double> recordsByWeek,
            double projectHealthScore,
            double budgetTotal,
            double costTotal,
            double budgetVariancePct,
            List<MonthlyCostMetric> costByMonth,
            String generatedAt) {}

    public record ApiIntegration(
            String id,
            String name,
            String provider,
            String endpointUrl,
            String apiKeyHint,
            String status,
            String lastSyncAt,
            String createdAt) {}

    public record BimModel(
            String id,
            String projectId,
            String projectName,
            String title,
            String format,
            String viewerUrl,
            Double fileSizeMb,
            String status,
            String uploadedBy,
            String createdAt) {}

    public record ConsultMessage(String id, String role, String content, String createdAt) {}

    public record ConsultThread(String id, String title, String createdAt, List<ConsultMessage> messages) {}

    public record ConsultMessageReply(
            String threadId, ConsultMessage userMessage, ConsultMessage assistantMessage) {}

    public record RagDocument(String id, String title, String content, List<String> tags, String createdAt) {}

    public record RagSearchHit(String documentId, String title, String snippet, double score) {}

    public record RagAnswer(String answer, List<RagSearchHit> sources) {}
}
