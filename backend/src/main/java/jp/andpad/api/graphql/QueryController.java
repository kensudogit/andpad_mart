package jp.andpad.api.graphql;

import java.util.List;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import jp.andpad.api.domain.BudgetDashboard;
import jp.andpad.api.domain.BudgetLineItem;
import jp.andpad.api.domain.BudgetType;
import jp.andpad.api.domain.ConstructionProject;
import jp.andpad.api.domain.Contract;
import jp.andpad.api.domain.ContractTemplate;
import jp.andpad.api.domain.CostEntry;
import jp.andpad.api.domain.CrmContact;
import jp.andpad.api.domain.CrmInteraction;
import jp.andpad.api.domain.DxInitiative;
import jp.andpad.api.domain.ExtendedTypes.AndpadAnalyticsDashboard;
import jp.andpad.api.domain.ExtendedTypes.ApiIntegration;
import jp.andpad.api.domain.ExtendedTypes.BimModel;
import jp.andpad.api.domain.ExtendedTypes.ConsultThread;
import jp.andpad.api.domain.ExtendedTypes.RagAnswer;
import jp.andpad.api.domain.ExtendedTypes.RagDocument;
import jp.andpad.api.domain.ExtendedTypes.RagSearchHit;
import jp.andpad.api.domain.Health;
import jp.andpad.api.domain.AttendanceRecord;
import jp.andpad.api.domain.LeaveRequest;
import jp.andpad.api.domain.LearningTypes.AnalyticsBoard;
import jp.andpad.api.domain.LearningTypes.Bookmark;
import jp.andpad.api.domain.LearningTypes.Certificate;
import jp.andpad.api.domain.LearningTypes.DashboardStats;
import jp.andpad.api.domain.LearningTypes.Instructor;
import jp.andpad.api.domain.LearningTypes.LearningPath;
import jp.andpad.api.domain.LearningTypes.Quiz;
import jp.andpad.api.domain.LearningTypes.QuizAttempt;
import jp.andpad.api.domain.LearningTypes.Video;
import jp.andpad.api.domain.LearningTypes.VideoNote;
import jp.andpad.api.domain.LearningTypes.VideoPage;
import jp.andpad.api.domain.LearningTypes.WatchProgress;
import jp.andpad.api.domain.Organization;
import jp.andpad.api.domain.ProjectBudget;
import jp.andpad.api.domain.ProjectBudgetSummary;
import jp.andpad.api.domain.ProjectModuleRecord;
import jp.andpad.api.domain.SaasModule;
import jp.andpad.api.domain.SaasModuleCode;
import jp.andpad.api.domain.Session;
import jp.andpad.api.domain.SkillLevel;
import jp.andpad.api.domain.TeamMember;
import jp.andpad.api.domain.UsageSummary;
import jp.andpad.api.domain.VideoCategory;
import jp.andpad.api.service.AuthService;
import jp.andpad.api.service.BudgetService;
import jp.andpad.api.service.ConstructionService;
import jp.andpad.api.service.ConsultService;
import jp.andpad.api.service.ExtendedService;
import jp.andpad.api.service.LearningStubService;
import jp.andpad.api.service.OrganizationService;
import jp.andpad.api.service.SaasService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class QueryController {

    private final AuthService authService;
    private final OrganizationService organizationService;
    private final LearningStubService learningStubService;
    private final SaasService saasService;
    private final ConstructionService constructionService;
    private final BudgetService budgetService;
    private final ExtendedService extendedService;
    private final ConsultService consultService;

    @QueryMapping
    public Health health() {
        return new Health(true, "andpad-api", "2.0.0-saas");
    }

    @QueryMapping
    public DashboardStats dashboard() {
        return learningStubService.dashboard();
    }

    @QueryMapping
    public Session currentSession() {
        return authService.currentSession();
    }

    @QueryMapping
    public Organization organization() {
        return organizationService.getOrganization();
    }

    @QueryMapping
    public UsageSummary usageSummary() {
        return organizationService.usageSummary();
    }

    @QueryMapping
    public List<TeamMember> teamMembers() {
        return organizationService.teamMembers();
    }

    @QueryMapping
    public AnalyticsBoard analyticsBoard(@Argument int periodDays) {
        return learningStubService.analyticsBoard(periodDays);
    }

    @QueryMapping
    public VideoPage videos(
            @Argument VideoCategory category,
            @Argument SkillLevel skillLevel,
            @Argument String search,
            @Argument Integer page,
            @Argument Integer pageSize) {
        return learningStubService.videos(category, skillLevel, search, page, pageSize);
    }

    @QueryMapping
    public Video video(@Argument String id) {
        return learningStubService.video(id);
    }

    @QueryMapping
    public List<Video> featuredVideos() {
        return learningStubService.featuredVideos();
    }

    @QueryMapping
    public List<Instructor> instructors() {
        return learningStubService.instructors();
    }

    @QueryMapping
    public Instructor instructor(@Argument String id) {
        return learningStubService.instructor(id);
    }

    @QueryMapping
    public List<LearningPath> learningPaths(@Argument VideoCategory category, @Argument SkillLevel skillLevel) {
        return learningStubService.learningPaths(category, skillLevel);
    }

    @QueryMapping
    public LearningPath learningPath(@Argument String id) {
        return learningStubService.learningPath(id);
    }

    @QueryMapping
    public List<WatchProgress> myProgress(@Argument String learnerId) {
        return learningStubService.myProgress(learnerId);
    }

    @QueryMapping
    public List<Bookmark> myBookmarks(@Argument String learnerId) {
        return learningStubService.myBookmarks(learnerId);
    }

    @QueryMapping
    public List<VideoNote> videoNotes(@Argument String videoId, @Argument String learnerId) {
        return learningStubService.videoNotes(videoId, learnerId);
    }

    @QueryMapping
    public List<Quiz> quizzes(@Argument String videoId) {
        return learningStubService.quizzes(videoId);
    }

    @QueryMapping
    public Quiz quiz(@Argument String id) {
        return learningStubService.quiz(id);
    }

    @QueryMapping
    public List<QuizAttempt> myQuizAttempts(@Argument String learnerId) {
        return learningStubService.myQuizAttempts(learnerId);
    }

    @QueryMapping
    public List<Certificate> myCertificates(@Argument String learnerId) {
        return learningStubService.myCertificates(learnerId);
    }

    @QueryMapping
    public List<SaasModule> saasModules() {
        return saasService.saasModules();
    }

    @QueryMapping
    public List<DxInitiative> dxInitiatives() {
        return saasService.dxInitiatives();
    }

    @QueryMapping
    public List<CrmContact> crmContacts() {
        return saasService.crmContacts();
    }

    @QueryMapping
    public List<CrmInteraction> crmInteractions(@Argument String contactId) {
        return saasService.crmInteractions(contactId);
    }

    @QueryMapping
    public List<AttendanceRecord> attendanceRecords() {
        return saasService.attendanceRecords();
    }

    @QueryMapping
    public List<LeaveRequest> leaveRequests() {
        return saasService.leaveRequests();
    }

    @QueryMapping
    public List<ContractTemplate> contractTemplates() {
        return saasService.contractTemplates();
    }

    @QueryMapping
    public List<Contract> contracts() {
        return saasService.contracts();
    }

    @QueryMapping
    public List<ConsultThread> consultThreads() {
        return consultService.listThreads();
    }

    @QueryMapping
    public ConsultThread consultThread(@Argument String id) {
        return consultService.getThread(id);
    }

    @QueryMapping
    public List<RagDocument> ragDocuments() {
        return consultService.listRagDocuments();
    }

    @QueryMapping
    public List<RagSearchHit> ragSearch(@Argument String query, @Argument int limit) {
        return consultService.searchRag(query, limit);
    }

    @QueryMapping
    public RagAnswer ragAnswer(@Argument String query) {
        return consultService.ragAnswer(query);
    }

    @QueryMapping
    public List<ConstructionProject> constructionProjects() {
        return constructionService.listProjects();
    }

    @QueryMapping
    public List<ProjectModuleRecord> projectModuleRecords(
            @Argument SaasModuleCode moduleCode, @Argument String projectId) {
        return constructionService.listModuleRecords(moduleCode, projectId);
    }

    @QueryMapping
    public AndpadAnalyticsDashboard andpadAnalytics(@Argument int periodDays) {
        return extendedService.andpadAnalytics(periodDays);
    }

    @QueryMapping
    public List<ApiIntegration> apiIntegrations() {
        return extendedService.listApiIntegrations();
    }

    @QueryMapping
    public List<BimModel> bimModels(@Argument String projectId) {
        return extendedService.listBimModels(projectId);
    }

    @QueryMapping
    public BimModel bimModel(@Argument String id) {
        return extendedService.getBimModel(id);
    }

    @QueryMapping
    public List<ProjectBudget> projectBudgets(@Argument String projectId, @Argument BudgetType budgetType) {
        return budgetService.listBudgets(projectId, budgetType);
    }

    @QueryMapping
    public List<BudgetLineItem> budgetLineItems(@Argument String budgetId) {
        return budgetService.listLineItems(budgetId);
    }

    @QueryMapping
    public List<CostEntry> costEntries(@Argument String projectId, @Argument String lineItemId) {
        return budgetService.listCostEntries(projectId, lineItemId);
    }

    @QueryMapping
    public BudgetDashboard budgetDashboard(@Argument String projectId) {
        return budgetService.budgetDashboard(projectId);
    }

    @QueryMapping
    public List<ProjectBudgetSummary> projectBudgetSummaries() {
        return budgetService.listBudgetSummaries();
    }
}
