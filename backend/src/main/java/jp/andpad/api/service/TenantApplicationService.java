package jp.andpad.api.service;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import jp.andpad.api.domain.TenantApplication;
import jp.andpad.api.domain.TenantApplicationDocument;
import jp.andpad.api.domain.TenantApplicationStatus;
import jp.andpad.api.domain.WorkflowTypes.WorkflowInstanceView;
import jp.andpad.api.graphql.input.CreateTenantApplicationInput;
import jp.andpad.api.graphql.input.UpdateTenantApplicationInput;
import jp.andpad.api.graphql.input.UploadTenantApplicationDocumentInput;
import jp.andpad.api.repository.AuthRepository;
import jp.andpad.api.repository.TenantApplicationRepository;
import jp.andpad.api.security.AuthPrincipal;
import jp.andpad.api.security.TenantContext;
import jp.andpad.api.security.UnauthorizedException;
import jp.andpad.api.seed.OrgSampleDataSeeder;
import jp.andpad.imart.workflow.model.WorkflowInstanceStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;

/** 新規テナント作成申請の管理サービス。 */
@Slf4j
@Service
public class TenantApplicationService {

    private static final String TENANT_FLOW_ID = "tenant-provisioning";

    private final TenantApplicationRepository tenantApplicationRepository;
    private final AuthRepository authRepository;
    private final WorkflowService workflowService;
    private final OrgSampleDataSeeder orgSampleDataSeeder;

    public TenantApplicationService(
            TenantApplicationRepository tenantApplicationRepository,
            AuthRepository authRepository,
            @Lazy WorkflowService workflowService,
            OrgSampleDataSeeder orgSampleDataSeeder) {
        this.tenantApplicationRepository = tenantApplicationRepository;
        this.authRepository = authRepository;
        this.workflowService = workflowService;
        this.orgSampleDataSeeder = orgSampleDataSeeder;
    }

    public List<TenantApplication> listApplications() {
        AuthPrincipal principal = TenantContext.requirePrincipal();
        return tenantApplicationRepository.listForApplicant(
                principal.orgId(), principal.userId(), isPlatformAdmin(principal));
    }

    public TenantApplication getApplication(String id) {
        AuthPrincipal principal = TenantContext.requirePrincipal();
        TenantApplication app = tenantApplicationRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("tenant application not found: " + id));
        assertReadable(principal, app);
        return app;
    }

    public TenantApplication createApplication(CreateTenantApplicationInput input) {
        AuthPrincipal principal = TenantContext.requirePrincipal();
        validateInput(input.name(), input.slug(), input.contactName(), input.contactEmail(), input.ownerName(), input.ownerEmail());
        String slug = normalizeSlug(input.slug(), input.name());
        if (tenantApplicationRepository.slugTaken(slug, null)) {
            throw new IllegalArgumentException("slug already taken: " + slug);
        }
        return tenantApplicationRepository.create(
                principal.orgId(),
                principal.userId(),
                input.name().trim(),
                slug,
                trimToNull(input.address()),
                input.contactName().trim(),
                input.contactEmail().trim().toLowerCase(Locale.ROOT),
                trimToNull(input.contactPhone()),
                input.ownerName().trim(),
                input.ownerEmail().trim().toLowerCase(Locale.ROOT),
                trimToNull(input.notes()));
    }

    public TenantApplication updateApplication(UpdateTenantApplicationInput input) {
        AuthPrincipal principal = TenantContext.requirePrincipal();
        TenantApplication existing = getApplication(input.id());
        assertOwner(principal, existing);
        if (existing.status() != TenantApplicationStatus.DRAFT) {
            throw new IllegalStateException("only DRAFT applications can be updated");
        }
        String name = input.name() != null ? input.name().trim() : existing.name();
        String slug = input.slug() != null ? normalizeSlug(input.slug(), name) : existing.slug();
        if (tenantApplicationRepository.slugTaken(slug, existing.id())) {
            throw new IllegalArgumentException("slug already taken: " + slug);
        }
        return tenantApplicationRepository.updateDraft(
                existing.id(),
                name,
                slug,
                input.address() != null ? trimToNull(input.address()) : existing.address(),
                input.contactName() != null ? input.contactName().trim() : existing.contactName(),
                input.contactEmail() != null
                        ? input.contactEmail().trim().toLowerCase(Locale.ROOT)
                        : existing.contactEmail(),
                input.contactPhone() != null ? trimToNull(input.contactPhone()) : existing.contactPhone(),
                input.ownerName() != null ? input.ownerName().trim() : existing.ownerName(),
                input.ownerEmail() != null
                        ? input.ownerEmail().trim().toLowerCase(Locale.ROOT)
                        : existing.ownerEmail(),
                input.notes() != null ? trimToNull(input.notes()) : existing.notes());
    }

    public TenantApplicationDocument uploadDocument(UploadTenantApplicationDocumentInput input) {
        AuthPrincipal principal = TenantContext.requirePrincipal();
        TenantApplication existing = getApplication(input.applicationId());
        assertOwner(principal, existing);
        if (existing.status() != TenantApplicationStatus.DRAFT) {
            throw new IllegalStateException("documents can only be added to DRAFT applications");
        }
        if (input.fileName() == null || input.fileName().isBlank()) {
            throw new IllegalArgumentException("fileName is required");
        }
        return tenantApplicationRepository.addDocument(
                existing.id(),
                input.fileName().trim(),
                trimToNull(input.contentType()),
                trimToNull(input.contentText()));
    }

    /** 申請を確定し、承認ワークフローを起票する。 */
    public TenantApplication submitApplication(String id, String imSessionId) {
        AuthPrincipal principal = TenantContext.requirePrincipal();
        TenantApplication existing = getApplication(id);
        assertOwner(principal, existing);
        if (existing.status() != TenantApplicationStatus.DRAFT) {
            throw new IllegalStateException("application is not in DRAFT status");
        }
        WorkflowInstanceView instance = workflowService.startWorkflow(
                TENANT_FLOW_ID,
                "TENANT_APPLICATION",
                existing.id(),
                "テナント作成: " + existing.name(),
                Map.of(
                        "tenantName", existing.name(),
                        "slug", existing.slug(),
                        "contactEmail", existing.contactEmail()),
                imSessionId,
                null);
        tenantApplicationRepository.markSubmitted(existing.id(), instance.id());
        return getApplication(existing.id());
    }

    /** ワークフロー完了時にテナントを正式作成する。 */
    public void handleWorkflowCompletion(String applicationId, WorkflowInstanceStatus status) {
        if (status != WorkflowInstanceStatus.APPROVED
                && status != WorkflowInstanceStatus.REJECTED
                && status != WorkflowInstanceStatus.CANCELLED) {
            return;
        }
        TenantApplication app = tenantApplicationRepository
                .findById(applicationId)
                .orElse(null);
        if (app == null || app.status() != TenantApplicationStatus.PENDING_APPROVAL) {
            return;
        }
        if (status == WorkflowInstanceStatus.REJECTED || status == WorkflowInstanceStatus.CANCELLED) {
            tenantApplicationRepository.markRejected(applicationId);
            return;
        }
        try {
            String orgId = authRepository.createOrganizationFromTenant(
                    app.name(),
                    app.slug(),
                    app.address(),
                    app.contactName(),
                    app.contactEmail(),
                    app.contactPhone(),
                    app.ownerName(),
                    app.ownerEmail());
            orgSampleDataSeeder.seedForOrg(orgId);
            tenantApplicationRepository.markApproved(applicationId, orgId);
            log.info("tenant application approved: applicationId={} orgId={}", applicationId, orgId);
        } catch (Exception ex) {
            log.warn("tenant activation failed for {}: {}", applicationId, ex.getMessage());
            tenantApplicationRepository.markRejected(applicationId);
        }
    }

    public static boolean isPlatformAdmin(AuthPrincipal principal) {
        return TenantContext.DEMO_ORG_ID.equals(principal.orgId())
                && "OWNER".equalsIgnoreCase(principal.role());
    }

    private static void assertReadable(AuthPrincipal principal, TenantApplication app) {
        if (isPlatformAdmin(principal)) {
            return;
        }
        if (!principal.userId().equals(app.applicantUserId()) || !principal.orgId().equals(app.applicantOrgId())) {
            throw new UnauthorizedException("not authorized to view tenant application");
        }
    }

    private static void assertOwner(AuthPrincipal principal, TenantApplication app) {
        if (!principal.userId().equals(app.applicantUserId()) || !principal.orgId().equals(app.applicantOrgId())) {
            throw new UnauthorizedException("not authorized to modify tenant application");
        }
    }

    private static void validateInput(
            String name, String slug, String contactName, String contactEmail, String ownerName, String ownerEmail) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        if (contactName == null || contactName.isBlank()) {
            throw new IllegalArgumentException("contactName is required");
        }
        if (contactEmail == null || contactEmail.isBlank()) {
            throw new IllegalArgumentException("contactEmail is required");
        }
        if (ownerName == null || ownerName.isBlank()) {
            throw new IllegalArgumentException("ownerName is required");
        }
        if (ownerEmail == null || ownerEmail.isBlank()) {
            throw new IllegalArgumentException("ownerEmail is required");
        }
    }

    private static String normalizeSlug(String slug, String name) {
        String candidate;
        if (slug != null && !slug.isBlank()) {
            candidate = slug.toLowerCase(Locale.ROOT).trim().replaceAll("[^a-z0-9-]", "-");
        } else {
            candidate = name.toLowerCase(Locale.ROOT).trim().replaceAll("[^a-z0-9]+", "-");
        }
        candidate = candidate.replaceAll("-+", "-").replaceAll("^-|-$", "");
        if (candidate.isBlank()) {
            candidate = "tenant-" + UUID.randomUUID().toString().substring(0, 8);
        }
        return candidate;
    }

    private static String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
