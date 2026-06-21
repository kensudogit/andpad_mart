package jp.andpad.imart.stamp;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import jp.andpad.imart.stamp.model.StampListSearchCondition;
import jp.andpad.imart.stamp.model.WorkflowStampResult;
import jp.andpad.imart.stamp.spi.IntraMartStampService;

/** 印影連携無効時の no-op 実装。 */
@Service
@ConditionalOnProperty(name = "app.imart.stamp.enabled", havingValue = "false")
public class NoopIntraMartStampService implements IntraMartStampService {

    @Override
    public WorkflowStampResult getStampList(
            String sessionId, String systemMatterId, StampListSearchCondition condition) {
        return WorkflowStampResult.ok();
    }

    @Override
    public WorkflowStampResult getStampListCount(
            String sessionId, String systemMatterId, StampListSearchCondition condition) {
        return WorkflowStampResult.okCount(0);
    }

    @Override
    public WorkflowStampResult getStampResultFrameList(
            String sessionId, String systemMatterId, boolean sortType) {
        return WorkflowStampResult.okFrames(java.util.List.of());
    }
}
