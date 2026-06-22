package jp.andpad.api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import jp.andpad.api.domain.CnfmActvMatterView;
import jp.andpad.api.repository.CnfmActvMatterRepository;
import jp.andpad.api.security.TenantContext;
import jp.andpad.imart.cnfmactv.model.ActvMatterCnfmData;
import jp.andpad.imart.cnfmactv.model.CnfmActvMatterSearchCondition;
import jp.andpad.imart.cnfmactv.spi.IntraMartCnfmActvMatterService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CnfmActvMatterService {

    private final CnfmActvMatterRepository cnfmActvMatterRepository;
    private final IntraMartCnfmActvMatterService cnfmActvMatterService;

    public List<CnfmActvMatterView> listConfirmMatters(List<String> flowIds, Integer limit, String imSessionId) {
        return list(ActvMatterCnfmData.LIST_CONFIRM, flowIds, limit, imSessionId, false);
    }

    public List<CnfmActvMatterView> listLumpConfirmMatters(
            List<String> flowIds, Integer limit, boolean noOrgzConditionFlag, String imSessionId) {
        return list(ActvMatterCnfmData.LIST_LUMP_CONFIRM, flowIds, limit, imSessionId, noOrgzConditionFlag);
    }

    private List<CnfmActvMatterView> list(
            String listType, List<String> flowIds, Integer limit, String imSessionId, boolean noOrgzConditionFlag) {
        String orgId = TenantContext.orgId();
        if (orgId != null) {
            List<CnfmActvMatterView> fromDb = cnfmActvMatterRepository.list(orgId, listType, flowIds, limit);
            if (!fromDb.isEmpty()) {
                return fromDb;
            }
        }

        var condition = new CnfmActvMatterSearchCondition(flowIds, limit);
        var result = ActvMatterCnfmData.LIST_LUMP_CONFIRM.equals(listType)
                ? cnfmActvMatterService.getLumpCnfmList(imSessionId, noOrgzConditionFlag, condition)
                : cnfmActvMatterService.getCnfmList(imSessionId, condition);
        if (!result.success()) {
            return List.of();
        }
        return result.data().stream().map(row -> toView(row, listType)).toList();
    }

    private static CnfmActvMatterView toView(ActvMatterCnfmData row, String listType) {
        return new CnfmActvMatterView(
                row.systemMatterId(),
                row.listType() != null ? row.listType() : listType,
                row.systemMatterId(),
                row.flowId(),
                row.flowName(),
                row.matterName(),
                row.matterNumber(),
                row.nodeId(),
                row.applyAuthUserCode(),
                row.applyAuthUserName(),
                row.applyDate(),
                row.arrivedDate(),
                row.confirmCplFlag(),
                row.priorityLevel(),
                row.arrivedDate());
    }
}
