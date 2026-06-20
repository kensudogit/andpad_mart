package jp.andpad.imart.workflow.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import jp.andpad.imart.workflow.NoopWorkflowAuthGuard;
import jp.andpad.imart.workflow.spi.WorkflowAuthGuard;

/**
 * 汎用ワークフローエンジンの自動設定。
 */
@AutoConfiguration
@ComponentScan(basePackages = "jp.andpad.imart.workflow")
public class WorkflowAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(WorkflowAuthGuard.class)
    WorkflowAuthGuard noopWorkflowAuthGuard() {
        return new NoopWorkflowAuthGuard();
    }
}
