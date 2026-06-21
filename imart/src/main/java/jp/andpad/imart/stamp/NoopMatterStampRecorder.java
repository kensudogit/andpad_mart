package jp.andpad.imart.stamp;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import jp.andpad.imart.stamp.model.MatterStampData;
import jp.andpad.imart.stamp.spi.MatterStampRecorder;

/** 印影データ永続化の no-op 実装（DB 未設定時）。 */
@Component
@ConditionalOnMissingBean(MatterStampRecorder.class)
public class NoopMatterStampRecorder implements MatterStampRecorder {

    @Override
    public void record(MatterStampData stamp) {}
}
