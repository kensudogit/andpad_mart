package jp.andpad.api.cnfmactv;

import org.springframework.stereotype.Component;

import jp.andpad.api.repository.CnfmActvMatterRepository;
import jp.andpad.api.security.TenantContext;
import jp.andpad.imart.cnfmactv.model.ActvMatterCnfmData;
import jp.andpad.imart.cnfmactv.spi.CnfmActvMatterRecorder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostgresCnfmActvMatterRecorder implements CnfmActvMatterRecorder {

    private final CnfmActvMatterRepository cnfmActvMatterRepository;

    @Override
    public void record(ActvMatterCnfmData data) {
        try {
            String orgId = TenantContext.orgId();
            if (orgId == null) {
                log.warn("cnfm actv matter not stored: organization context missing");
                return;
            }
            cnfmActvMatterRepository.upsert(orgId, data);
        } catch (Exception ex) {
            log.warn("failed to store cnfm actv matter: {}", ex.getMessage());
        }
    }
}
