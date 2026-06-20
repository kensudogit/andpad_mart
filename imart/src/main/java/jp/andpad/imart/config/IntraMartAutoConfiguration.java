package jp.andpad.imart.config;

import jp.andpad.imart.IntraMartPluginRegistrar;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 * intra-mart 統合モジュールの Spring Boot 自動設定。
 * backend の {@code jp.andpad.api} と組み合わせて WAR / スタンドアロン両方で動作する。
 */
@AutoConfiguration
@ComponentScan(basePackages = "jp.andpad.imart")
@Import(IntraMartPluginRegistrar.class)
public class IntraMartAutoConfiguration {
}
