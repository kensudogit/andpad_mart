package jp.andpad.imart.mail.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

import jp.andpad.imart.mail.IntraMartMailProperties;

/**
 * intra-mart メール送信サブシステムの Spring Boot 自動設定。
 */
@AutoConfiguration
@EnableConfigurationProperties(IntraMartMailProperties.class)
@ComponentScan(basePackages = "jp.andpad.imart.mail")
public class MailAutoConfiguration {
}
