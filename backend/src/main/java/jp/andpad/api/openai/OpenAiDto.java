package jp.andpad.api.openai;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
record OpenAiChatRequest(String model, List<OpenAiChatMessage> messages) {}

@JsonIgnoreProperties(ignoreUnknown = true)
record OpenAiChatResponse(List<OpenAiChoice> choices, OpenAiError error) {}

@JsonIgnoreProperties(ignoreUnknown = true)
record OpenAiChoice(OpenAiChatMessage message) {}

@JsonIgnoreProperties(ignoreUnknown = true)
record OpenAiError(String message) {}
