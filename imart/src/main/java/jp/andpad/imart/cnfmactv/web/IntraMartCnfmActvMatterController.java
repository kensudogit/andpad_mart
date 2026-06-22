package jp.andpad.imart.cnfmactv.web;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jp.andpad.imart.cnfmactv.model.ActvMatterCnfmData;
import jp.andpad.imart.cnfmactv.model.CnfmActvMatterSearchCondition;
import jp.andpad.imart.cnfmactv.model.WorkflowCnfmActvMatterResult;
import jp.andpad.imart.cnfmactv.spi.IntraMartCnfmActvMatterService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth/imart/cnfmactv")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.imart.cnfmactv.enabled", havingValue = "true", matchIfMissing = true)
public class IntraMartCnfmActvMatterController {

    private final IntraMartCnfmActvMatterService cnfmActvMatterService;

    @GetMapping("/confirm")
    public ResponseEntity<Map<String, Object>> listConfirm(
            @RequestHeader(value = "X-IM-Session-Id", required = false) String sessionHeader,
            @RequestParam(required = false) List<String> flowId,
            @RequestParam(required = false) Integer limit) {
        WorkflowCnfmActvMatterResult result = cnfmActvMatterService.getCnfmList(
                sessionHeader, new CnfmActvMatterSearchCondition(flowId, limit));
        return toResponse(result);
    }

    @GetMapping("/lump-confirm")
    public ResponseEntity<Map<String, Object>> listLumpConfirm(
            @RequestHeader(value = "X-IM-Session-Id", required = false) String sessionHeader,
            @RequestParam(defaultValue = "false") boolean noOrgzConditionFlag,
            @RequestParam(required = false) List<String> flowId,
            @RequestParam(required = false) Integer limit) {
        WorkflowCnfmActvMatterResult result = cnfmActvMatterService.getLumpCnfmList(
                sessionHeader, noOrgzConditionFlag, new CnfmActvMatterSearchCondition(flowId, limit));
        return toResponse(result);
    }

    private static ResponseEntity<Map<String, Object>> toResponse(WorkflowCnfmActvMatterResult result) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", result.success());
        body.put("error", result.error());
        body.put("cnfmActvMatterList", result.data().stream().map(ActvMatterCnfmData::toMap).toList());
        body.put("count", result.count() != null ? result.count() : result.data().size());
        return result.success() ? ResponseEntity.ok(body) : ResponseEntity.badRequest().body(body);
    }
}
