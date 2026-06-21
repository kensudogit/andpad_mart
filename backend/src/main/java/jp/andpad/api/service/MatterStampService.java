package jp.andpad.api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import jp.andpad.api.domain.MatterStampView;
import jp.andpad.api.repository.MatterStampRepository;
import jp.andpad.api.security.TenantContext;
import jp.andpad.imart.stamp.model.MatterStampData;
import jp.andpad.imart.stamp.model.StampListSearchCondition;
import jp.andpad.imart.stamp.spi.IntraMartStampService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MatterStampService {

    private final MatterStampRepository matterStampRepository;
    private final IntraMartStampService stampService;

    public List<MatterStampView> listStamps(
            String flowId, String entityType, String entityId, String systemMatterId, Integer limit, String imSessionId) {
        String orgId = TenantContext.orgId();
        if (orgId != null) {
            List<MatterStampView> fromDb =
                    matterStampRepository.list(orgId, flowId, entityType, entityId, systemMatterId, limit);
            if (!fromDb.isEmpty()) {
                return fromDb;
            }
        }

        if (systemMatterId == null || systemMatterId.isBlank()) {
            return List.of();
        }
        var result = stampService.getStampList(imSessionId, systemMatterId, StampListSearchCondition.empty());
        if (!result.success()) {
            return List.of();
        }
        return result.stamps().stream().map(MatterStampService::toView).toList();
    }

    private static MatterStampView toView(MatterStampData row) {
        return new MatterStampView(
                null,
                row.systemMatterId(),
                row.no(),
                row.nodeId(),
                row.processDate(),
                row.processId(),
                row.stampStr1(),
                row.stampStr1Type(),
                row.stampStr2(),
                row.stampStr2Type(),
                row.stampStr3(),
                row.stampStr3Type(),
                row.stampType(),
                row.cancelFlag(),
                row.flowId(),
                row.entityType(),
                row.entityId(),
                row.workflowInstanceId(),
                null);
    }
}
