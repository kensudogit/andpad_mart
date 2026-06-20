package jp.andpad.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import jp.andpad.api.config.AppProperties;

/**
 * ANDPAD API エントリポイント。
 * スタンドアロン（bootRun）と intra-mart WAR デプロイ（{@link SpringBootServletInitializer}）の両方に対応。
 */
@SpringBootApplication(scanBasePackages = {"jp.andpad.api", "jp.andpad.imart"})
@EnableConfigurationProperties(AppProperties.class)
public class AndpadApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(AndpadApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(AndpadApplication.class);
    }
}
