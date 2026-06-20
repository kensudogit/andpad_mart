package jp.andpad.api.web;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.andpad.api.domain.Health;
import jp.andpad.api.service.RuntimeStatusService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class HealthController {

    private final RuntimeStatusService runtimeStatus;

    @GetMapping("/health")
    public Health health() {
        return new Health(true, "andpad-api", "2.0.0-saas");
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        boolean postgres = runtimeStatus.isPostgresConnected();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("service", "andpad-api");
        body.put("ok", true);
        body.put("postgres", postgres);
        body.put("openai", StringUtils.hasText(System.getenv("OPENAI_API_KEY")));
        body.put("setup", runtimeStatus.setupStatus(postgres));
        return body;
    }
}
