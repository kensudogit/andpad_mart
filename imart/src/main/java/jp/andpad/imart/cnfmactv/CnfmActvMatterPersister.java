package jp.andpad.imart.cnfmactv;

import org.springframework.stereotype.Component;

import jp.andpad.imart.cnfmactv.model.ActvMatterCnfmData;
import jp.andpad.imart.cnfmactv.spi.CnfmActvMatterRecorder;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CnfmActvMatterPersister {

    private final IntraMartCnfmActvMatterProperties properties;
    private final CnfmActvMatterRecorder cnfmActvMatterRecorder;

    public void persist(ActvMatterCnfmData data) {
        if (properties.isStoreInDatabase()) {
            cnfmActvMatterRecorder.record(data);
        }
    }
}
