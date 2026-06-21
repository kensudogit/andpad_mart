package jp.andpad.imart.stamp.spi;

import jp.andpad.imart.stamp.model.MatterStampData;

/** 印影データの永続化 SPI（backend が PostgreSQL 実装を提供）。 */
public interface MatterStampRecorder {

    void record(MatterStampData stamp);
}
