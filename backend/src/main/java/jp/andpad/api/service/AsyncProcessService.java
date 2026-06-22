package jp.andpad.api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import jp.andpad.api.domain.AsyncProcessStatusView;
import jp.andpad.api.repository.AsyncProcessRepository;
import jp.andpad.api.security.TenantContext;
import jp.andpad.imart.asyncprocess.model.AsyncProcessStatusData;
import jp.andpad.imart.asyncprocess.model.AsyncProcessStatusSearchCondition;
import jp.andpad.imart.asyncprocess.spi.IntraMartAsyncProcessService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AsyncProcessService {

    private final AsyncProcessRepository asyncProcessRepository;
    private final IntraMartAsyncProcessService asyncProcessService;

    public List<AsyncProcessStatusView> listStatus(
            List<String> flowIds, List<String> systemMatterIds, Integer limit, String imSessionId) {
        String orgId = TenantContext.orgId();
        if (orgId != null) {
            List<AsyncProcessStatusView> fromDb = asyncProcessRepository.list(orgId, flowIds, systemMatterIds, limit);
            if (!fromDb.isEmpty()) {
                return fromDb;
            }
        }

        var result = asyncProcessService.getAsyncProcessStatusDataList(
                imSessionId, new AsyncProcessStatusSearchCondition(flowIds, systemMatterIds, limit));
        if (!result.success()) {
            return List.of();
        }
        return result.data().stream().map(AsyncProcessService::toView).toList();
    }

    private static AsyncProcessStatusView toView(AsyncProcessStatusData row) {
        return new AsyncProcessStatusView(
                row.acceptId(),
                row.acceptId(),
                row.asyncProcStatus(),
                row.authUserCode(),
                row.executeUserCode(),
                row.flowId(),
                row.matterName(),
                row.matterNumber(),
                row.message(),
                row.nodeId(),
                row.procComment(),
                row.procDate(),
                row.procType(),
                row.queueId(),
                row.subMessage(),
                row.systemMatterId(),
                null,
                null,
                null,
                row.procDate());
    }
}
