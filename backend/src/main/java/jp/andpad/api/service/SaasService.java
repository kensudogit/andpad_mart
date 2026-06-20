package jp.andpad.api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import jp.andpad.api.domain.AttendanceRecord;
import jp.andpad.api.domain.Contract;
import jp.andpad.api.domain.ContractTemplate;
import jp.andpad.api.domain.CrmContact;
import jp.andpad.api.domain.CrmInteraction;
import jp.andpad.api.domain.DxInitiative;
import jp.andpad.api.domain.LeaveRequest;
import jp.andpad.api.domain.SaasModule;
import jp.andpad.api.domain.SaasModuleCode;
import jp.andpad.api.graphql.input.CreateContractInput;
import jp.andpad.api.graphql.input.CreateCrmContactInput;
import jp.andpad.api.graphql.input.CreateDxInitiativeInput;
import jp.andpad.api.graphql.input.CreateLeaveRequestInput;
import jp.andpad.api.repository.SaasRepository;
import jp.andpad.api.security.TenantContext;
import jp.andpad.api.util.Dates;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SaasService {

    private final SaasRepository saasRepository;

    public List<SaasModule> saasModules() {
        return saasRepository.listOrgModules(TenantContext.orgId());
    }

    public SaasModule setModuleEnabled(SaasModuleCode code, boolean enabled) {
        return saasRepository.setModuleEnabled(TenantContext.orgId(), code, enabled);
    }

    public List<DxInitiative> dxInitiatives() {
        return saasRepository.listDxInitiatives(TenantContext.orgId());
    }

    public DxInitiative createDxInitiative(CreateDxInitiativeInput input) {
        return saasRepository.createDxInitiative(
                TenantContext.orgId(),
                input.title(),
                input.description(),
                input.status(),
                input.progressPct() == null ? 0 : input.progressPct(),
                input.ownerName(),
                Dates.parseDate(input.dueDate()));
    }

    public List<CrmContact> crmContacts() {
        return saasRepository.listCrmContacts(TenantContext.orgId());
    }

    public CrmContact createCrmContact(CreateCrmContactInput input) {
        return saasRepository.createCrmContact(
                TenantContext.orgId(),
                input.name(),
                input.email(),
                input.phone(),
                input.company(),
                input.stage(),
                input.notes());
    }

    public List<CrmInteraction> crmInteractions(String contactId) {
        return saasRepository.listCrmInteractions(TenantContext.orgId(), contactId);
    }

    public CrmInteraction createCrmInteraction(String contactId, String kind, String summary) {
        return saasRepository.createCrmInteraction(TenantContext.orgId(), contactId, kind, summary);
    }

    public List<AttendanceRecord> attendanceRecords() {
        return saasRepository.listAttendanceRecords(TenantContext.orgId());
    }

    public AttendanceRecord clockIn(String note) {
        return saasRepository.clockIn(TenantContext.orgId(), TenantContext.userId(), note);
    }

    public AttendanceRecord clockOut() {
        return saasRepository.clockOut(TenantContext.orgId(), TenantContext.userId());
    }

    public List<LeaveRequest> leaveRequests() {
        return saasRepository.listLeaveRequests(TenantContext.orgId());
    }

    public LeaveRequest createLeaveRequest(CreateLeaveRequestInput input) {
        return saasRepository.createLeaveRequest(
                TenantContext.orgId(),
                TenantContext.userId(),
                Dates.parseDate(input.startDate()),
                Dates.parseDate(input.endDate()),
                input.reason());
    }

    public LeaveRequest approveLeaveRequest(String id) {
        return saasRepository.approveLeaveRequest(TenantContext.orgId(), id);
    }

    public List<ContractTemplate> contractTemplates() {
        return saasRepository.listContractTemplates(TenantContext.orgId());
    }

    public ContractTemplate createContractTemplate(String name, String body) {
        return saasRepository.createContractTemplate(TenantContext.orgId(), name, body);
    }

    public List<Contract> contracts() {
        return saasRepository.listContracts(TenantContext.orgId());
    }

    public Contract createContract(CreateContractInput input) {
        return saasRepository.createContract(
                TenantContext.orgId(),
                input.templateId(),
                input.title(),
                input.partyName(),
                input.partyEmail(),
                input.body());
    }

    public Contract signContract(String id) {
        return saasRepository.signContract(TenantContext.orgId(), id);
    }
}
