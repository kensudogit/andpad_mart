package jp.andpad.imart.cnfmactv.stub;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import jp.andpad.imart.cnfmactv.CnfmActvMatterFileWriter;
import jp.andpad.imart.cnfmactv.CnfmActvMatterPersister;
import jp.andpad.imart.cnfmactv.model.ActvMatterCnfmData;
import jp.andpad.imart.cnfmactv.model.CnfmActvMatterSearchCondition;
import jp.andpad.imart.cnfmactv.model.WorkflowCnfmActvMatterResult;
import jp.andpad.imart.cnfmactv.spi.IntraMartCnfmActvMatterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.imart.cnfmactv.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnProperty(name = "app.imart.auth.mode", havingValue = "stub", matchIfMissing = true)
public class DevIntraMartCnfmActvMatterService implements IntraMartCnfmActvMatterService {

    private final InMemoryCnfmActvMatterStore store;
    private final CnfmActvMatterFileWriter fileWriter;
    private final CnfmActvMatterPersister persister;

    public void register(ActvMatterCnfmData data) {
        store.put(data);
        persister.persist(data);
        fileWriter.append("register", List.of(data));
        log.info(
                "[stub-cnfmactv] registered type={} systemMatterId={} matter={}",
                data.listType(),
                data.systemMatterId(),
                data.matterName());
    }

    @Override
    public WorkflowCnfmActvMatterResult getCnfmList(String sessionId, CnfmActvMatterSearchCondition condition) {
        return list(ActvMatterCnfmData.LIST_CONFIRM, condition);
    }

    @Override
    public WorkflowCnfmActvMatterResult getCnfmListCount(String sessionId, CnfmActvMatterSearchCondition condition) {
        return WorkflowCnfmActvMatterResult.okCount(list(ActvMatterCnfmData.LIST_CONFIRM, condition).data().size());
    }

    @Override
    public WorkflowCnfmActvMatterResult getLumpCnfmList(
            String sessionId, boolean noOrgzConditionFlag, CnfmActvMatterSearchCondition condition) {
        return list(ActvMatterCnfmData.LIST_LUMP_CONFIRM, condition);
    }

    @Override
    public WorkflowCnfmActvMatterResult getLumpCnfmListCount(
            String sessionId, boolean noOrgzConditionFlag, CnfmActvMatterSearchCondition condition) {
        return WorkflowCnfmActvMatterResult.okCount(list(ActvMatterCnfmData.LIST_LUMP_CONFIRM, condition).data().size());
    }

    private WorkflowCnfmActvMatterResult list(String listType, CnfmActvMatterSearchCondition condition) {
        CnfmActvMatterSearchCondition resolved =
                condition != null ? condition : new CnfmActvMatterSearchCondition(List.of(), null);
        List<ActvMatterCnfmData> rows =
                new ArrayList<>(store.listByTypeAndFlowIds(listType, resolved.flowIds()));
        if (resolved.limit() != null && resolved.limit() > 0) {
            rows = rows.stream().limit(resolved.limit()).toList();
        }
        return WorkflowCnfmActvMatterResult.ok(rows);
    }
}
