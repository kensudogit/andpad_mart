package jp.andpad.api.openai;

/** OpenAI Chat Completions の1メッセージ。 */
public record OpenAiChatMessage(String role, String content) {}
