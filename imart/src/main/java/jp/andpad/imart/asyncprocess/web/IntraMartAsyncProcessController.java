package jp.andpad.imart.asyncprocess.web;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jp.andpad.imart.asyncprocess.model.AsyncProcessStatusData;
import jp.andpad.imart.asyncprocess.model.AsyncProcessStatusSearchCondition;
import jp.andpad.imart.asyncprocess.model.WorkflowAsyncProcessResult;
import jp.andpad.imart.asyncprocess.spi.IntraMartAsyncProcessService;
import lombok.RequiredArgsConstructor;

/** intra-mart 非同期処理ワークフロー REST API（{@code AsyncProcessWorkflow} 連携）。 */
@RestController
@RequestMapping("/auth/imart/asyncprocess")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.imart.asyncprocess.enabled", havingValue = "true", matchIfMissing = true)
public class IntraMartAsyncProcessController {

    private final IntraMartAsyncProcessService asyncProcessService;

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> listStatus(
            @RequestHeader(value = "X-IM-Session-Id", required = false) String sessionHeader,
            @RequestParam(required = false) List<String> flowId,
            @RequestParam(required = false) List<String> systemMatterId,
            @RequestParam(required = false) Integer limit) {
        WorkflowAsyncProcessResult result = asyncProcessService.getAsyncProcessStatusDataList(
                sessionHeader, new AsyncProcessStatusSearchCondition(flowId, systemMatterId, limit));
        return toResponse(result);
    }

    @GetMapping("/status/count")
    public ResponseEntity<Map<String, Object>> countStatus(
            @RequestHeader(value = "X-IM-Session-Id", required = false) String sessionHeader,
            @RequestParam(required = false) List<String> flowId,
            @RequestParam(required = false) List<String> systemMatterId) {
        WorkflowAsyncProcessResult result = asyncProcessService.getAsyncProcessStatusDataListCount(
                sessionHeader, new AsyncProcessStatusSearchCondition(flowId, systemMatterId, null));
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", result.success());
        body.put("error", result.error());
        body.put("count", result.count() != null ? result.count() : 0L);
        return result.success() ? ResponseEntity.ok(body) : ResponseEntity.badRequest().body(body);
    }

    @PostMapping("/status")
    public ResponseEntity<Map<String, Object>> createStatus(
            @RequestHeader(value = "X-IM-Session-Id", required = false) String sessionHeader,
            @RequestBody List<AsyncProcessStatusBody> body) {
        WorkflowAsyncProcessResult result = asyncProcessService.createAsyncProcessStatusData(
                sessionHeader, body.stream().map(AsyncProcessStatusBody::toModel).toList());
        return toResponse(result);
    }

    @PostMapping("/status/update")
    public ResponseEntity<Map<String, Object>> updateStatus(
            @RequestHeader(value = "X-IM-Session-Id", required = false) String sessionHeader,
            @RequestBody List<AsyncProcessStatusBody> body) {
        WorkflowAsyncProcessResult result = asyncProcessService.updateAsyncProcessStatusData(
                sessionHeader, body.stream().map(AsyncProcessStatusBody::toModel).toList());
        return toResponse(result);
    }

    private static ResponseEntity<Map<String, Object>> toResponse(WorkflowAsyncProcessResult result) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", result.success());
        body.put("error", result.error());
        body.put("asyncProcessStatusDataList", result.data().stream().map(AsyncProcessStatusData::toMap).toList());
        return result.success() ? ResponseEntity.ok(body) : ResponseEntity.badRequest().body(body);
    }

    public record AsyncProcessStatusBody(
            String acceptId,
            String asyncProcStatus,
            String authUserCode,
            String executeUserCode,
            String flowId,
            String matterName,
            String matterNumber,
            String message,
            String nodeId,
            String procComment,
            String procDate,
            String procType,
            String queueId,
            String subMessage,
            String systemMatterId) {

        AsyncProcessStatusData toModel() {
            return new AsyncProcessStatusData(
                    acceptId,
                    asyncProcStatus,
                    authUserCode,
                    executeUserCode,
                    flowId,
                    matterName,
                    matterNumber,
                    message,
                    nodeId,
                    procComment,
                    procDate,
                    procType,
                    queueId,
                    subMessage,
                    systemMatterId);
        }
    }
}
