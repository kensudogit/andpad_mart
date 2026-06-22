package jp.andpad.imart.cnfmactv.http;

import java.util.List;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import jp.andpad.imart.cnfmactv.CnfmActvMatterSupport;
import jp.andpad.imart.cnfmactv.model.CnfmActvMatterSearchCondition;
import jp.andpad.imart.cnfmactv.model.WorkflowCnfmActvMatterResult;
import jp.andpad.imart.cnfmactv.spi.IntraMartCnfmActvMatterService;
import jp.andpad.imart.auth.bridge.IntraMartSsjsBridgeClient;
import jp.andpad.imart.auth.bridge.SsjsInvokeRequest;
import jp.andpad.imart.auth.bridge.SsjsInvokeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.imart.cnfmactv.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnProperty(name = "app.imart.auth.enabled", havingValue = "true")
@ConditionalOnProperty(name = "app.imart.auth.mode", havingValue = "http")
public class HttpIntraMartCnfmActvMatterService implements IntraMartCnfmActvMatterService {

    private final IntraMartSsjsBridgeClient bridgeClient;

    @Override
    public WorkflowCnfmActvMatterResult getCnfmList(String sessionId, CnfmActvMatterSearchCondition condition) {
        return invoke("getCnfmList", List.of(resolve(condition).toMap()), sessionId);
    }

    @Override
    public WorkflowCnfmActvMatterResult getCnfmListCount(String sessionId, CnfmActvMatterSearchCondition condition) {
        return invoke("getCnfmListCount", List.of(resolve(condition).toMap()), sessionId);
    }

    @Override
    public WorkflowCnfmActvMatterResult getLumpCnfmList(
            String sessionId, boolean noOrgzConditionFlag, CnfmActvMatterSearchCondition condition) {
        return invoke("getLumpCnfmList", List.of(noOrgzConditionFlag, resolve(condition).toMap()), sessionId);
    }

    @Override
    public WorkflowCnfmActvMatterResult getLumpCnfmListCount(
            String sessionId, boolean noOrgzConditionFlag, CnfmActvMatterSearchCondition condition) {
        return invoke("getLumpCnfmListCount", List.of(noOrgzConditionFlag, resolve(condition).toMap()), sessionId);
    }

    private WorkflowCnfmActvMatterResult invoke(String method, List<Object> args, String sessionId) {
        SsjsInvokeResponse response = bridgeClient.invoke(new SsjsInvokeRequest(
                "im_workflow", "CnfmActvMatterList", method, args, sessionId, Map.of()));
        if (!response.success()) {
            log.warn("CnfmActvMatterList.{} failed: {}", method, response.error());
            return WorkflowCnfmActvMatterResult.fail(response.error());
        }
        return CnfmActvMatterSupport.mapWorkflowResult(response.result());
    }

    private static CnfmActvMatterSearchCondition resolve(CnfmActvMatterSearchCondition condition) {
        return condition != null ? condition : new CnfmActvMatterSearchCondition(List.of(), null);
    }
}
