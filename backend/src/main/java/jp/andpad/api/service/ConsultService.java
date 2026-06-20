package jp.andpad.api.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jp.andpad.api.domain.ExtendedTypes.ConsultMessage;
import jp.andpad.api.domain.ExtendedTypes.ConsultMessageReply;
import jp.andpad.api.domain.ExtendedTypes.ConsultThread;
import jp.andpad.api.domain.ExtendedTypes.RagAnswer;
import jp.andpad.api.domain.ExtendedTypes.RagDocument;
import jp.andpad.api.domain.ExtendedTypes.RagSearchHit;
import jp.andpad.api.graphql.input.LearningInputs.CreateRagDocumentInput;
import jp.andpad.api.openai.OpenAiChatMessage;
import jp.andpad.api.openai.OpenAiClient;
import jp.andpad.api.repository.ConsultRagRepository;
import jp.andpad.api.security.AuthPrincipal;
import jp.andpad.api.security.TenantContext;
import jp.andpad.api.util.RagHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsultService {

    private final ConsultRagRepository consultRagRepository;
    private final OpenAiClient openAiClient;

    public List<ConsultThread> listThreads() {
        AuthPrincipal p = TenantContext.requirePrincipal();
        return consultRagRepository.listThreads(p.orgId(), p.userId(), isOrgWide(p));
    }

    public ConsultThread getThread(String id) {
        AuthPrincipal p = TenantContext.requirePrincipal();
        return consultRagRepository.getThread(p.orgId(), p.userId(), id, isOrgWide(p));
    }

    public ConsultMessageReply sendMessage(String threadId, String message) {
        AuthPrincipal p = TenantContext.requirePrincipal();
        String orgId = p.orgId();
        String userId = p.userId();
        boolean orgWide = isOrgWide(p);
        String tid = threadId;
        if (tid == null || tid.isBlank()) {
            String title = truncate(message, 40);
            if (title.isBlank()) {
                title = "相談スレッド";
            }
            tid = consultRagRepository.createThread(orgId, userId, title).id();
        } else if (!consultRagRepository.verifyThreadAccess(orgId, userId, tid, orgWide)) {
            throw new IllegalArgumentException("thread not found or access denied");
        }
        List<ConsultMessage> historyBefore = consultRagRepository.listMessages(orgId, tid);
        ConsultMessage userMessage = consultRagRepository.addMessage(orgId, tid, "user", message);
        String reply = generateReply(message, historyBefore);
        ConsultMessage assistantMessage = consultRagRepository.addMessage(orgId, tid, "assistant", reply);
        consultRagRepository.incrementConsultUsage(orgId, message.length() + reply.length());
        return new ConsultMessageReply(tid, userMessage, assistantMessage);
    }

    public List<RagDocument> listRagDocuments() {
        return consultRagRepository.listRagDocuments(TenantContext.orgId());
    }

    public RagDocument createRagDocument(CreateRagDocumentInput input) {
        return consultRagRepository.createRagDocument(TenantContext.orgId(), input);
    }

    public List<RagSearchHit> searchRag(String query, int limit) {
        String orgId = TenantContext.orgId();
        List<RagSearchHit> hits = consultRagRepository.searchRagDocuments(orgId, query, limit);
        if (!hits.isEmpty()) {
            return hits;
        }
        return RagHelper.localSearch(consultRagRepository.listRagDocuments(orgId), query, limit);
    }

    public RagAnswer ragAnswer(String query) {
        List<RagSearchHit> hits = searchRag(query, 5);
        if (hits.isEmpty()) {
            return new RagAnswer(RagHelper.formatHitsAsAnswer(hits), hits);
        }
        if (openAiClient.isEnabled()) {
            try {
                StringBuilder context = new StringBuilder();
                for (int i = 0; i < Math.min(3, hits.size()); i++) {
                    RagSearchHit h = hits.get(i);
                    context.append("【").append(h.title()).append("】\n").append(h.snippet()).append("\n\n");
                }
                String prompt = "以下の社内文書抜粋を参考に、ユーザの質問に日本語で簡潔に答えてください。\n\n"
                        + context
                        + "\n質問: "
                        + query;
                String answer = openAiClient.chat(
                        "You summarize internal construction documents for project managers. Cite document titles when helpful.",
                        List.of(),
                        prompt);
                if (StringUtils.hasText(answer)) {
                    return new RagAnswer(answer.trim(), hits);
                }
            } catch (Exception ex) {
                log.warn("RAG OpenAI answer failed: {}", ex.getMessage());
            }
        }
        return new RagAnswer(RagHelper.formatHitsAsAnswer(hits), hits);
    }

    private String generateReply(String message, List<ConsultMessage> history) {
        String topic = truncate(message, 40);
        if (topic.isBlank()) {
            topic = "（質問内容）";
        }
        if (openAiClient.isEnabled()) {
            try {
                List<OpenAiChatMessage> chatHistory = toChatHistory(history);
                String answer = openAiClient.chat(OpenAiClient.CONSTRUCTION_CONSULT_SYSTEM, chatHistory, message);
                if (StringUtils.hasText(answer)) {
                    return answer.trim();
                }
            } catch (Exception ex) {
                log.warn("Consult OpenAI chat failed: {}", ex.getMessage());
                return replyOpenAiError(topic, ex.getMessage());
            }
        }
        return replyWithoutOpenAi(topic);
    }

    private static List<OpenAiChatMessage> toChatHistory(List<ConsultMessage> history) {
        List<OpenAiChatMessage> out = new ArrayList<>();
        if (history == null) {
            return out;
        }
        for (ConsultMessage msg : history) {
            if (msg == null || !StringUtils.hasText(msg.content())) {
                continue;
            }
            String role = msg.role() == null ? "user" : msg.role().trim().toLowerCase();
            if (!"user".equals(role) && !"assistant".equals(role)) {
                role = "user";
            }
            out.add(new OpenAiChatMessage(role, msg.content()));
        }
        return out;
    }

    private static String replyWithoutOpenAi(String topic) {
        return """
                現在 OpenAI 連携（OPENAI_API_KEY）が未設定のため、AI による詳細回答を生成できません。

                Railway の andpad_j サービス → Variables → OPENAI_API_KEY を追加し Redeploy すると、\
                ご質問「%s」に対する本格的な回答が得られます。

                ※ デモ応答として受け付けました。建設現場の安全・工程・品質管理などについてお気軽にご質問ください。"""
                .formatted(topic);
    }

    private static String replyOpenAiError(String topic, String err) {
        String detail = err == null ? "unknown error" : truncate(err, 120);
        return """
                OpenAI への問い合わせに失敗しました（%s）。しばらく待ってから再送してください。

                （質問: %s）"""
                .formatted(detail, topic);
    }

    private static boolean isOrgWide(AuthPrincipal p) {
        return "OWNER".equals(p.role()) || "ADMIN".equals(p.role());
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        String t = s.trim();
        return t.length() <= max ? t : t.substring(0, max);
    }
}
