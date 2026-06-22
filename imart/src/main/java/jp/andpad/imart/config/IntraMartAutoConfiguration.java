package jp.andpad.imart.config;

import jp.andpad.imart.IntraMartPluginRegistrar;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;

/**
 * intra-mart 統合モジュールの Spring Boot 自動設定。
 *
 * <p>{@code backend} モジュールの {@code jp.andpad.api} と組み合わせて、
 * WAR デプロイ（intra-mart コンテナ内）およびスタンドアロン JAR の両方で動作する。
 *
 * <p>コンポーネントスキャン対象は {@code jp.andpad.imart} パッケージだが、
 * 認証サブシステム（{@code jp.andpad.imart.auth}）は除外する。
 * 認証は {@link jp.andpad.imart.auth.config.IntraMartAuthAutoConfiguration} が
 * {@code app.imart.auth.enabled=true} のときのみ読み込む。
 *
 * @see jp.andpad.imart.auth.config.IntraMartAuthAutoConfiguration
 * @see IntraMartPluginRegistrar
 */
@AutoConfiguration
@ComponentScan(
        basePackages = "jp.andpad.imart",
        excludeFilters = {
            @ComponentScan.Filter(type = FilterType.REGEX, pattern = "jp\\.andpad\\.imart\\.auth\\..*"),
            @ComponentScan.Filter(type = FilterType.REGEX, pattern = "jp\\.andpad\\.imart\\.workflow\\..*"),
            @ComponentScan.Filter(type = FilterType.REGEX, pattern = "jp\\.andpad\\.imart\\.mail\\..*"),
            @ComponentScan.Filter(type = FilterType.REGEX, pattern = "jp\\.andpad\\.imart\\.monitoring\\..*"),
            @ComponentScan.Filter(type = FilterType.REGEX, pattern = "jp\\.andpad\\.imart\\.stamp\\..*"),
            @ComponentScan.Filter(type = FilterType.REGEX, pattern = "jp\\.andpad\\.imart\\.asyncprocess\\..*"),
            @ComponentScan.Filter(type = FilterType.REGEX, pattern = "jp\\.andpad\\.imart\\.cnfmactv\\..*")
        })
@Import(IntraMartPluginRegistrar.class)
public class IntraMartAutoConfiguration {
}
