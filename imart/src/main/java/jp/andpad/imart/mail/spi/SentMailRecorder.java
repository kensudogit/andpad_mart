package jp.andpad.imart.mail.spi;

import jp.andpad.imart.mail.model.SentMailSnapshot;

/**
 * 送信メールの永続化 SPI（PostgreSQL 等）。
 */
public interface SentMailRecorder {

    /** 送信メールを記録する。 */
    void record(SentMailSnapshot snapshot);
}
