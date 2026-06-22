package jp.andpad.imart.cnfmactv.spi;

import jp.andpad.imart.cnfmactv.model.ActvMatterCnfmData;

/** 確認案件一覧の永続化 SPI（PostgreSQL 等）。 */
public interface CnfmActvMatterRecorder {

    void record(ActvMatterCnfmData data);
}
