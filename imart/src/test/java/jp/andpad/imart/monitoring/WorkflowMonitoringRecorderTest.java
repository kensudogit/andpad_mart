package jp.andpad.imart.monitoring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jp.andpad.imart.monitoring.model.MonitoringFlowData;
import jp.andpad.imart.monitoring.model.WorkflowMonitoringResult;
import jp.andpad.imart.monitoring.spi.IntraMartMonitoringService;
import jp.andpad.imart.workflow.WorkflowDefinitionRegistry;
import jp.andpad.imart.workflow.model.WorkflowAction;
import jp.andpad.imart.workflow.model.WorkflowDefinition;
import jp.andpad.imart.workflow.model.WorkflowInstanceStatus;

@ExtendWith(MockitoExtension.class)
class WorkflowMonitoringRecorderTest {

    @Mock
    private IntraMartMonitoringProperties properties;

    @Mock
    private IntraMartMonitoringService monitoringService;

    @InjectMocks
    private WorkflowMonitoringRecorder recorder;

    @Test
    void ensureFlowMonitoringCreatesDocumentApprovalRow() {
        WorkflowDefinition definition =
                WorkflowDefinitionRegistry.getBuiltin(WorkflowDefinitionRegistry.FLOW_DOC);

        when(properties.isEnabled()).thenReturn(true);
        when(properties.isRecordOnTransition()).thenReturn(true);
        when(properties.getFlowIds()).thenReturn(List.of("document-approval"));
        when(monitoringService.getMonitoringFlowDataList(any(), any()))
                .thenReturn(WorkflowMonitoringResult.ok(List.of()));
        when(monitoringService.createMonitoringFlowData(eq("dev-imart-session"), any()))
                .thenReturn(WorkflowMonitoringResult.ok());

        recorder.ensureFlowMonitoring(definition, "dev-imart-session");

        ArgumentCaptor<List<MonitoringFlowData>> captor = ArgumentCaptor.forClass(List.class);
        verify(monitoringService).createMonitoringFlowData(eq("dev-imart-session"), captor.capture());
        assertThat(captor.getValue()).hasSize(1);
        assertThat(captor.getValue().get(0).flowId()).isEqualTo("document-approval");
    }

    @Test
    void recordTransitionUpdatesOnApprovedTerminalState() {
        WorkflowDefinition definition =
                WorkflowDefinitionRegistry.getBuiltin(WorkflowDefinitionRegistry.FLOW_DOC);
        MonitoringFlowData existing = new MonitoringFlowData(
                "document-approval", "書類承認", "1", "1", "1", "1", "1", "1", "1", "1", "1", "5");

        when(properties.isEnabled()).thenReturn(true);
        when(properties.isRecordOnTransition()).thenReturn(true);
        when(properties.getFlowIds()).thenReturn(List.of("document-approval"));
        when(monitoringService.getMonitoringFlowDataList(any(), any()))
                .thenReturn(WorkflowMonitoringResult.ok(List.of(existing)));
        when(monitoringService.updateMonitoringFlowData(eq("dev-imart-session"), any()))
                .thenReturn(WorkflowMonitoringResult.ok());

        recorder.recordTransition(
                definition,
                "inst-1",
                WorkflowAction.APPROVE,
                WorkflowInstanceStatus.APPROVED,
                "2026-06-08T00:00:00Z",
                "2026-06-08T01:00:00Z",
                "dev-imart-session");

        verify(monitoringService).updateMonitoringFlowData(eq("dev-imart-session"), any());
    }
}
