package jp.andpad.imart.stamp.web;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jp.andpad.imart.stamp.model.StampListSearchCondition;
import jp.andpad.imart.stamp.model.WorkflowStampResult;
import jp.andpad.imart.stamp.spi.IntraMartStampService;
import lombok.RequiredArgsConstructor;

/**
 * intra-mart 完了案件印影 REST API（{@code CplMatterStampList} 連携）。
 */
@RestController
@RequestMapping("/auth/imart/stamps")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.imart.stamp.enabled", havingValue = "true", matchIfMissing = true)
public class IntraMartStampController {

    private final IntraMartStampService stampService;

    /** 完了案件の印影データを取得する。 */
    @GetMapping
    public ResponseEntity<Map<String, Object>> listStamps(
            @RequestHeader(value = "X-IM-Session-Id", required = false) String sessionHeader,
            @RequestParam String systemMatterId) {
        WorkflowStampResult result =
                stampService.getStampList(sessionHeader, systemMatterId, StampListSearchCondition.empty());
        return toResponse(result);
    }

    /** 完了案件の印影データ件数を取得する。 */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> countStamps(
            @RequestHeader(value = "X-IM-Session-Id", required = false) String sessionHeader,
            @RequestParam String systemMatterId) {
        WorkflowStampResult result =
                stampService.getStampListCount(sessionHeader, systemMatterId, StampListSearchCondition.empty());
        return toCountResponse(result);
    }

    /** 完了案件の印影フレームデータを取得する。 */
    @GetMapping("/frames")
    public ResponseEntity<Map<String, Object>> listFrames(
            @RequestHeader(value = "X-IM-Session-Id", required = false) String sessionHeader,
            @RequestParam String systemMatterId,
            @RequestParam(defaultValue = "false") boolean sortType) {
        WorkflowStampResult result = stampService.getStampResultFrameList(sessionHeader, systemMatterId, sortType);
        return toFrameResponse(result);
    }

    private static ResponseEntity<Map<String, Object>> toResponse(WorkflowStampResult result) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", result.success());
        body.put("error", result.error());
        body.put("stampList", result.stamps().stream().map(s -> s.toMap()).toList());
        return result.success() ? ResponseEntity.ok(body) : ResponseEntity.badRequest().body(body);
    }

    private static ResponseEntity<Map<String, Object>> toCountResponse(WorkflowStampResult result) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", result.success());
        body.put("error", result.error());
        body.put("count", result.count() != null ? result.count() : 0);
        return result.success() ? ResponseEntity.ok(body) : ResponseEntity.badRequest().body(body);
    }

    private static ResponseEntity<Map<String, Object>> toFrameResponse(WorkflowStampResult result) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", result.success());
        body.put("error", result.error());
        body.put("stampFrameList", result.frames().stream().map(f -> f.toMap()).toList());
        return result.success() ? ResponseEntity.ok(body) : ResponseEntity.badRequest().body(body);
    }
}
