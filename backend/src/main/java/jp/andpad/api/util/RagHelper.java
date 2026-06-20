package jp.andpad.api.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import jp.andpad.api.domain.ExtendedTypes.RagDocument;
import jp.andpad.api.domain.ExtendedTypes.RagSearchHit;

public final class RagHelper {

    private RagHelper() {}

    public static List<RagSearchHit> localSearch(List<RagDocument> docs, String query, int limit) {
        String q = query == null ? "" : query.trim();
        if (q.isEmpty() || docs.isEmpty()) {
            return List.of();
        }
        int max = limit <= 0 ? 5 : limit;
        String qLower = q.toLowerCase(Locale.ROOT);
        List<RagSearchHit> out = new ArrayList<>();
        for (RagDocument doc : docs) {
            String titleLower = doc.title().toLowerCase(Locale.ROOT);
            String contentLower = doc.content().toLowerCase(Locale.ROOT);
            double score;
            if (titleLower.contains(qLower)) {
                score = 1.0;
            } else if (contentLower.contains(qLower)) {
                score = 0.7;
            } else {
                continue;
            }
            out.add(new RagSearchHit(doc.id(), doc.title(), snippet(doc.content(), q, 180), score));
            if (out.size() >= max) {
                break;
            }
        }
        return out;
    }

    public static String snippet(String content, String query, int maxChars) {
        if (content == null || content.isBlank()) {
            return "";
        }
        if (maxChars <= 0) {
            maxChars = 180;
        }
        String lower = content.toLowerCase(Locale.ROOT);
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        int idx = q.isEmpty() ? -1 : lower.indexOf(q);
        if (idx < 0) {
            return truncate(content, maxChars);
        }
        int start = Math.max(0, idx - maxChars / 4);
        int end = Math.min(content.length(), start + maxChars * 2);
        String s = content.substring(start, end);
        s = truncate(s, maxChars);
        return start > 0 ? "..." + s : s;
    }

    public static String formatHitsAsAnswer(List<RagSearchHit> hits) {
        if (hits == null || hits.isEmpty()) {
            return "関連する文書が見つかりませんでした。文書を登録するか、別のキーワードで検索してください。";
        }
        StringBuilder b = new StringBuilder("登録文書から見つかった関連情報です。\n\n");
        for (int i = 0; i < Math.min(3, hits.size()); i++) {
            RagSearchHit h = hits.get(i);
            b.append("【").append(h.title()).append("】\n").append(h.snippet()).append("\n\n");
        }
        b.append("（OPENAI_API_KEY を設定すると、参照文書に基づく要約回答を生成できます。）");
        return b.toString();
    }

    private static String truncate(String s, int max) {
        if (s.length() <= max) {
            return s;
        }
        return s.substring(0, max) + "...";
    }
}
