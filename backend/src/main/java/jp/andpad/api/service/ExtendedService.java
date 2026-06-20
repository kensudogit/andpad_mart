package jp.andpad.api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import jp.andpad.api.domain.ExtendedTypes.AndpadAnalyticsDashboard;
import jp.andpad.api.domain.ExtendedTypes.ApiIntegration;
import jp.andpad.api.domain.ExtendedTypes.BimModel;
import jp.andpad.api.domain.LearningTypes.AnalyticsInsight;
import jp.andpad.api.graphql.input.LearningInputs.CreateApiIntegrationInput;
import jp.andpad.api.graphql.input.LearningInputs.CreateBimModelInput;
import jp.andpad.api.repository.ExtendedRepository;
import jp.andpad.api.security.TenantContext;
import jp.andpad.api.util.Dates;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExtendedService {

    private final ExtendedRepository extendedRepository;

    public AndpadAnalyticsDashboard andpadAnalytics(int periodDays) {
        return extendedRepository.andpadAnalytics(TenantContext.orgId(), periodDays);
    }

    public List<ApiIntegration> listApiIntegrations() {
        return extendedRepository.listApiIntegrations(TenantContext.orgId());
    }

    public ApiIntegration createApiIntegration(CreateApiIntegrationInput input) {
        return extendedRepository.createApiIntegration(TenantContext.orgId(), input);
    }

    public ApiIntegration syncApiIntegration(String id) {
        return extendedRepository.syncApiIntegration(TenantContext.orgId(), id);
    }

    public List<BimModel> listBimModels(String projectId) {
        return extendedRepository.listBimModels(TenantContext.orgId(), projectId);
    }

    public BimModel getBimModel(String id) {
        return extendedRepository.getBimModel(TenantContext.orgId(), id);
    }

    public BimModel createBimModel(CreateBimModelInput input) {
        return extendedRepository.createBimModel(TenantContext.orgId(), input);
    }

    public AnalyticsInsight generateAnalyticsInsight(int periodDays) {
        AndpadAnalyticsDashboard dash = andpadAnalytics(periodDays);
        return fallbackConstructionInsight(dash);
    }

    private AnalyticsInsight fallbackConstructionInsight(AndpadAnalyticsDashboard d) {
        String summary = String.format(
                "過去%d日間: 進行中案件%d件、健全性%.0f点。請求¥%.0f、実行予算¥%.0f、期間原価¥%.0f、予算差異率%.1f%%。",
                d.periodDays(),
                d.activeProjects(),
                d.projectHealthScore(),
                d.billingTotal(),
                d.budgetTotal(),
                d.costTotal(),
                d.budgetVariancePct());
        return new AnalyticsInsight(
                summary,
                List.of("予算・原価・請求データが案件横断で可視化されています"),
                List.of("原価進捗が予算に対して高い案件は完工予想の再算定が必要です"),
                List.of("月次原価レポートで費目別差異を確認", "請求と原価のバランスを四半期ごとにレビュー"),
                Dates.format(Dates.now()));
    }
}
