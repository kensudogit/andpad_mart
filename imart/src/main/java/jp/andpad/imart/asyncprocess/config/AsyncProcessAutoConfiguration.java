package jp.andpad.imart.asyncprocess.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

import jp.andpad.imart.asyncprocess.IntraMartAsyncProcessProperties;

/** intra-mart 非同期処理ワークフローサブシステムの Spring Boot 自動設定。 */
@AutoConfiguration
@EnableConfigurationProperties(IntraMartAsyncProcessProperties.class)
@ComponentScan(basePackages = "jp.andpad.imart.asyncprocess")
public class AsyncProcessAutoConfiguration {}
