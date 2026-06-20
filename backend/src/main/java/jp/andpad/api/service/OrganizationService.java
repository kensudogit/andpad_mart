package jp.andpad.api.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import jp.andpad.api.domain.Organization;
import jp.andpad.api.domain.SaasModule;
import jp.andpad.api.domain.TeamMember;
import jp.andpad.api.domain.UsageSummary;
import jp.andpad.api.graphql.input.UpdateOrganizationInput;
import jp.andpad.api.repository.OrganizationRepository;
import jp.andpad.api.repository.SaasRepository;
import jp.andpad.api.security.TenantContext;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final SaasRepository saasRepository;

    public Organization getOrganization() {
        String orgId = TenantContext.orgId();
        List<SaasModule> modules = saasRepository.listOrgModules(orgId);
        return organizationRepository.getOrganization(orgId, modules);
    }

    public Organization enrichOrganization(Organization organization) {
        List<SaasModule> modules = saasRepository.listOrgModules(organization.id());
        return new Organization(
                organization.id(),
                organization.name(),
                organization.slug(),
                organization.planTier(),
                organization.subscriptionStatus(),
                organization.seatCount(),
                organization.timezone(),
                organization.memberCount(),
                organization.createdAt(),
                modules);
    }

    public UsageSummary usageSummary() {
        return organizationRepository.usageSummary(TenantContext.orgId());
    }

    public List<TeamMember> teamMembers() {
        return organizationRepository.listTeamMembers(TenantContext.orgId());
    }

    public Organization updateOrganization(UpdateOrganizationInput input) {
        TenantContext.requirePrincipal();
        Map<String, Object> patch = new HashMap<>();
        if (input.name() != null) {
            patch.put("name", input.name());
        }
        if (input.slug() != null) {
            patch.put("slug", input.slug());
        }
        if (input.seatCount() != null) {
            patch.put("seatCount", input.seatCount());
        }
        if (input.timezone() != null) {
            patch.put("timezone", input.timezone());
        }
        String orgId = TenantContext.orgId();
        List<SaasModule> modules = saasRepository.listOrgModules(orgId);
        return organizationRepository.updateOrganization(orgId, patch, modules);
    }
}
