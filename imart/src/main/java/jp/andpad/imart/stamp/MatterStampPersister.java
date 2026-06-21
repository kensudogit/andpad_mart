package jp.andpad.imart.stamp;

import org.springframework.stereotype.Component;

import jp.andpad.imart.stamp.model.MatterStampData;
import jp.andpad.imart.stamp.spi.MatterStampRecorder;
import lombok.RequiredArgsConstructor;

/** 印影データを永続化 SPI へ保存する。 */
@Component
@RequiredArgsConstructor
public class MatterStampPersister {

    private final IntraMartStampProperties properties;
    private final MatterStampRecorder matterStampRecorder;

    public void persist(MatterStampData stamp) {
        if (properties.isStoreInDatabase()) {
            matterStampRecorder.record(stamp);
        }
    }
}
