package jp.andpad.imart.asyncprocess.http;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import jp.andpad.imart.asyncprocess.AsyncProcessFileWriter;
import jp.andpad.imart.asyncprocess.AsyncProcessPersister;
import jp.andpad.imart.asyncprocess.AsyncProcessSupport;
import jp.andpad.imart.asyncprocess.model.AsyncProcessStatusData;
import jp.andpad.imart.asyncprocess.model.AsyncProcessStatusSearchCondition;
import jp.andpad.imart.asyncprocess.model.WorkflowAsyncProcessResult;
import jp.andpad.imart.asyncprocess.spi.IntraMartAsyncProcessService;
import jp.andpad.imart.auth.bridge.IntraMartSsjsBridgeClient;
import jp.andpad.imart.auth.bridge.SsjsInvokeRequest;
import jp.andpad.imart.auth.bridge.SsjsInvokeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** HTTP ブリッジ経由の {@code AsyncProcessWorkflow} 実装。 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.imart.asyncprocess.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnProperty(name = "app.imart.auth.enabled", havingValue = "true")
@ConditionalOnProperty(name = "app.imart.auth.mode", havingValue = "http")
public class HttpIntraMartAsyncProcessService implements IntraMartAsyncProcessService {

    private final IntraMartSsjsBridgeClient bridgeClient;
    private final AsyncProcessFileWriter fileWriter;
    private final AsyncProcessPersister persister;

    @Override
    public WorkflowAsyncProcessResult createAsyncProcessStatusData(
            String sessionId, List<AsyncProcessStatusData> models) {
        WorkflowAsyncProcessResult result =
                invoke("createAsyncProcessStatusData", List.of(toModelMaps(models)), sessionId);
        if (result.success()) {
            fileWriter.append("createAsyncProcessStatusData", models);
            models.forEach(m -> persister.persist(m, null, null, null));
        }
        return result;
    }

    @Override
    public WorkflowAsyncProcessResult updateAsyncProcessStatusData(
            String sessionId, List<AsyncProcessStatusData> models) {
        WorkflowAsyncProcessResult result =
                invoke("updateAsyncProcessStatusData", List.of(toModelMaps(models)), sessionId);
        if (result.success()) {
            fileWriter.append("updateAsyncProcessStatusData", models);
            models.forEach(m -> persister.persist(m, null, null, null));
        }
        return result;
    }

    @Override
    public WorkflowAsyncProcessResult getAsyncProcessStatusDataList(
            String sessionId, AsyncProcessStatusSearchCondition condition) {
        AsyncProcessStatusSearchCondition resolved =
                condition != null ? condition : new AsyncProcessStatusSearchCondition(List.of(), List.of(), null);
        return invoke("getAsyncProcessStatusDataList", List.of(resolved.toMap()), sessionId);
    }

    @Override
    public WorkflowAsyncProcessResult getAsyncProcessStatusDataListCount(
            String sessionId, AsyncProcessStatusSearchCondition condition) {
        AsyncProcessStatusSearchCondition resolved =
                condition != null ? condition : new AsyncProcessStatusSearchCondition(List.of(), List.of(), null);
        return invoke("getAsyncProcessStatusDataListCount", List.of(resolved.toMap()), sessionId);
    }

    private WorkflowAsyncProcessResult invoke(String method, List<Object> args, String sessionId) {
        SsjsInvokeResponse response = bridgeClient.invoke(new SsjsInvokeRequest(
                "im_workflow", "AsyncProcessWorkflow", method, args, sessionId, Map.of()));
        if (!response.success()) {
            log.warn("AsyncProcessWorkflow.{} failed: {}", method, response.error());
            return WorkflowAsyncProcessResult.fail(response.error());
        }
        return AsyncProcessSupport.mapWorkflowResult(response.result());
    }

    private static List<Map<String, Object>> toModelMaps(List<AsyncProcessStatusData> models) {
        List<Map<String, Object>> maps = new ArrayList<>();
        for (AsyncProcessStatusData model : models) {
            maps.add(model.toMap());
        }
        return maps;
    }
}
