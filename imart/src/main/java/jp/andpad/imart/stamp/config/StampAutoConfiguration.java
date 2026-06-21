package jp.andpad.imart.stamp.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

import jp.andpad.imart.stamp.IntraMartStampProperties;

/** intra-mart 完了案件印影サブシステムの Spring Boot 自動設定。 */
@AutoConfiguration
@EnableConfigurationProperties(IntraMartStampProperties.class)
@ComponentScan(basePackages = "jp.andpad.imart.stamp")
public class StampAutoConfiguration {}
