package jp.andpad.api.stamp;

import org.springframework.stereotype.Component;

import jp.andpad.api.repository.MatterStampRepository;
import jp.andpad.api.security.TenantContext;
import jp.andpad.imart.stamp.model.MatterStampData;
import jp.andpad.imart.stamp.spi.MatterStampRecorder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** 印影データを PostgreSQL に保存する。 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PostgresMatterStampRecorder implements MatterStampRecorder {

    private final MatterStampRepository matterStampRepository;

    @Override
    public void record(MatterStampData stamp) {
        try {
            String orgId = TenantContext.orgId();
            if (orgId == null) {
                log.warn("matter stamp not stored: organization context missing");
                return;
            }
            matterStampRepository.insert(orgId, stamp);
        } catch (Exception ex) {
            log.warn("failed to store matter stamp: {}", ex.getMessage());
        }
    }
}
