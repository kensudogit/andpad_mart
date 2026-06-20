package jp.andpad.api.graphql;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

import jp.andpad.api.domain.AttendanceRecord;
import jp.andpad.api.domain.BudgetLineItem;
import jp.andpad.api.domain.ConstructionProject;
import jp.andpad.api.domain.Contract;
import jp.andpad.api.domain.ContractTemplate;
import jp.andpad.api.domain.CostEntry;
import jp.andpad.api.domain.CrmContact;
import jp.andpad.api.domain.CrmInteraction;
import jp.andpad.api.domain.DxInitiative;
import jp.andpad.api.domain.ExtendedTypes.ApiIntegration;
import jp.andpad.api.domain.ExtendedTypes.BimModel;
import jp.andpad.api.domain.ExtendedTypes.ConsultMessageReply;
import jp.andpad.api.domain.ExtendedTypes.RagDocument;
import jp.andpad.api.domain.LeaveRequest;
import jp.andpad.api.domain.LearningTypes.AnalyticsInsight;
import jp.andpad.api.domain.LearningTypes.Bookmark;
import jp.andpad.api.domain.LearningTypes.LearningPath;
import jp.andpad.api.domain.LearningTypes.QuizAttempt;
import jp.andpad.api.domain.LearningTypes.Video;
import jp.andpad.api.domain.LearningTypes.VideoNote;
import jp.andpad.api.domain.LearningTypes.WatchProgress;
import jp.andpad.api.domain.Organization;
import jp.andpad.api.domain.ProjectBudget;
import jp.andpad.api.domain.ProjectModuleRecord;
import jp.andpad.api.domain.SaasModule;
import jp.andpad.api.domain.SaasModuleCode;
import jp.andpad.api.graphql.input.CreateBudgetLineItemInput;
import jp.andpad.api.graphql.input.CreateConstructionProjectInput;
import jp.andpad.api.graphql.input.CreateContractInput;
import jp.andpad.api.graphql.input.CreateCostEntryInput;
import jp.andpad.api.graphql.input.CreateCrmContactInput;
import jp.andpad.api.graphql.input.CreateDxInitiativeInput;
import jp.andpad.api.graphql.input.CreateLeaveRequestInput;
import jp.andpad.api.graphql.input.CreateProjectBudgetInput;
import jp.andpad.api.graphql.input.CreateProjectModuleRecordInput;
import jp.andpad.api.graphql.input.LearningInputs.CreateApiIntegrationInput;
import jp.andpad.api.graphql.input.LearningInputs.CreateBimModelInput;
import jp.andpad.api.graphql.input.LearningInputs.CreateRagDocumentInput;
import jp.andpad.api.graphql.input.LearningInputs.CreateVideoNoteInput;
import jp.andpad.api.graphql.input.LearningInputs.SubmitQuizAttemptInput;
import jp.andpad.api.graphql.input.LearningInputs.UpdateWatchProgressInput;
import jp.andpad.api.graphql.input.UpdateOrganizationInput;
import jp.andpad.api.service.BudgetService;
import jp.andpad.api.service.ConstructionService;
import jp.andpad.api.service.ConsultService;
import jp.andpad.api.service.ExtendedService;
import jp.andpad.api.service.LearningStubService;
import jp.andpad.api.service.OrganizationService;
import jp.andpad.api.service.SaasService;
import jp.andpad.api.util.Dates;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class MutationController {

    private final OrganizationService organizationService;
    private final LearningStubService learningStubService;
    private final SaasService saasService;
    private final ConstructionService constructionService;
    private final BudgetService budgetService;
    private final ExtendedService extendedService;
    private final ConsultService consultService;

    @MutationMapping
    public Organization updateOrganization(@Argument UpdateOrganizationInput input) {
        return organizationService.updateOrganization(input);
    }

    @MutationMapping
    public AnalyticsInsight generateAnalyticsInsight(@Argument int periodDays) {
        return extendedService.generateAnalyticsInsight(periodDays);
    }

    @MutationMapping
    public WatchProgress updateWatchProgress(@Argument UpdateWatchProgressInput input) {
        return learningStubService.updateWatchProgress(input);
    }

    @MutationMapping
    public Video recordVideoView(@Argument String videoId) {
        return learningStubService.recordVideoView(videoId);
    }

    @MutationMapping
    public VideoNote createVideoNote(@Argument CreateVideoNoteInput input) {
        return learningStubService.createVideoNote(input);
    }

    @MutationMapping
    public boolean deleteVideoNote(@Argument String id) {
        return learningStubService.deleteVideoNote(id);
    }

    @MutationMapping
    public Bookmark toggleBookmark(@Argument String videoId, @Argument String learnerId) {
        return learningStubService.toggleBookmark(videoId, learnerId);
    }

    @MutationMapping
    public LearningPath enrollLearningPath(@Argument String pathId, @Argument String learnerId) {
        return learningStubService.enrollLearningPath(pathId, learnerId);
    }

    @MutationMapping
    public QuizAttempt submitQuizAttempt(@Argument SubmitQuizAttemptInput input) {
        return learningStubService.submitQuizAttempt(input);
    }

    @MutationMapping
    public DxInitiative createDxInitiative(@Argument CreateDxInitiativeInput input) {
        return saasService.createDxInitiative(input);
    }

    @MutationMapping
    public CrmContact createCrmContact(@Argument CreateCrmContactInput input) {
        return saasService.createCrmContact(input);
    }

    @MutationMapping
    public CrmInteraction createCrmInteraction(
            @Argument String contactId, @Argument String kind, @Argument String summary) {
        return saasService.createCrmInteraction(contactId, kind, summary);
    }

    @MutationMapping
    public AttendanceRecord clockIn(@Argument String note) {
        return saasService.clockIn(note);
    }

    @MutationMapping
    public AttendanceRecord clockOut() {
        return saasService.clockOut();
    }

    @MutationMapping
    public LeaveRequest createLeaveRequest(@Argument CreateLeaveRequestInput input) {
        return saasService.createLeaveRequest(input);
    }

    @MutationMapping
    public LeaveRequest approveLeaveRequest(@Argument String id) {
        return saasService.approveLeaveRequest(id);
    }

    @MutationMapping
    public ContractTemplate createContractTemplate(@Argument String name, @Argument String body) {
        return saasService.createContractTemplate(name, body);
    }

    @MutationMapping
    public Contract createContract(@Argument CreateContractInput input) {
        return saasService.createContract(input);
    }

    @MutationMapping
    public Contract signContract(@Argument String id) {
        return saasService.signContract(id);
    }

    @MutationMapping
    public ConsultMessageReply sendConsultMessage(@Argument String threadId, @Argument String message) {
        return consultService.sendMessage(threadId, message);
    }

    @MutationMapping
    public RagDocument createRagDocument(@Argument CreateRagDocumentInput input) {
        return consultService.createRagDocument(input);
    }

    @MutationMapping
    public SaasModule setSaasModuleEnabled(@Argument SaasModuleCode code, @Argument boolean enabled) {
        return saasService.setModuleEnabled(code, enabled);
    }

    @MutationMapping
    public ConstructionProject createConstructionProject(@Argument CreateConstructionProjectInput input) {
        return constructionService.createProject(input);
    }

    @MutationMapping
    public ProjectModuleRecord createProjectModuleRecord(@Argument CreateProjectModuleRecordInput input) {
        return constructionService.createModuleRecord(input);
    }

    @MutationMapping
    public ApiIntegration createApiIntegration(@Argument CreateApiIntegrationInput input) {
        return extendedService.createApiIntegration(input);
    }

    @MutationMapping
    public ApiIntegration syncApiIntegration(@Argument String id) {
        return extendedService.syncApiIntegration(id);
    }

    @MutationMapping
    public BimModel createBimModel(@Argument CreateBimModelInput input) {
        return extendedService.createBimModel(input);
    }

    @MutationMapping
    public ProjectBudget createProjectBudget(@Argument CreateProjectBudgetInput input) {
        return budgetService.createBudget(input);
    }

    @MutationMapping
    public BudgetLineItem createBudgetLineItem(@Argument CreateBudgetLineItemInput input) {
        return budgetService.createLineItem(input);
    }

    @MutationMapping
    public CostEntry createCostEntry(@Argument CreateCostEntryInput input) {
        return budgetService.createCostEntry(input);
    }

    @MutationMapping
    public ProjectBudget approveProjectBudget(@Argument String id) {
        return budgetService.approveBudget(id);
    }

    @MutationMapping
    public CostEntry createCostFromBilling(@Argument String billingRecordId, @Argument String projectId) {
        return budgetService.createCostFromBilling(billingRecordId, projectId);
    }
}
