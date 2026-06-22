package jp.andpad.imart.asyncprocess.spi;

import jp.andpad.imart.asyncprocess.model.AsyncProcessStatusData;

/** 非同期処理状況情報の永続化 SPI（PostgreSQL 等）。 */
public interface AsyncProcessRecorder {

    void record(AsyncProcessStatusData data, String workflowInstanceId, String entityType, String entityId);
}
