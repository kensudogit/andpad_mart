package jp.andpad.imart.cnfmactv;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import jp.andpad.imart.cnfmactv.model.CnfmActvMatterSearchCondition;
import jp.andpad.imart.cnfmactv.model.WorkflowCnfmActvMatterResult;
import jp.andpad.imart.cnfmactv.spi.IntraMartCnfmActvMatterService;

@Service
@ConditionalOnProperty(name = "app.imart.cnfmactv.enabled", havingValue = "false")
public class NoopIntraMartCnfmActvMatterService implements IntraMartCnfmActvMatterService {

    @Override
    public WorkflowCnfmActvMatterResult getCnfmList(String sessionId, CnfmActvMatterSearchCondition condition) {
        return WorkflowCnfmActvMatterResult.ok(java.util.List.of());
    }

    @Override
    public WorkflowCnfmActvMatterResult getCnfmListCount(String sessionId, CnfmActvMatterSearchCondition condition) {
        return WorkflowCnfmActvMatterResult.okCount(0L);
    }

    @Override
    public WorkflowCnfmActvMatterResult getLumpCnfmList(
            String sessionId, boolean noOrgzConditionFlag, CnfmActvMatterSearchCondition condition) {
        return WorkflowCnfmActvMatterResult.ok(java.util.List.of());
    }

    @Override
    public WorkflowCnfmActvMatterResult getLumpCnfmListCount(
            String sessionId, boolean noOrgzConditionFlag, CnfmActvMatterSearchCondition condition) {
        return WorkflowCnfmActvMatterResult.okCount(0L);
    }
}
