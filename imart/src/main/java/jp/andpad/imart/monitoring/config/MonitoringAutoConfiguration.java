package jp.andpad.imart.monitoring.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

import jp.andpad.imart.monitoring.IntraMartMonitoringProperties;

/** intra-mart ワークフローモニタリングサブシステムの Spring Boot 自動設定。 */
@AutoConfiguration
@EnableConfigurationProperties(IntraMartMonitoringProperties.class)
@ComponentScan(basePackages = "jp.andpad.imart.monitoring")
public class MonitoringAutoConfiguration {}
