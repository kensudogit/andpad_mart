package jp.andpad.api.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import jp.andpad.api.openai.OpenAiProperties;

@Configuration
@EnableConfigurationProperties(OpenAiProperties.class)
public class OpenAiConfig {}
