package jp.andpad.imart.cnfmactv;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import jp.andpad.imart.cnfmactv.model.ActvMatterCnfmData;
import jp.andpad.imart.cnfmactv.spi.CnfmActvMatterRecorder;

@Component
@ConditionalOnMissingBean(CnfmActvMatterRecorder.class)
public class NoopCnfmActvMatterRecorder implements CnfmActvMatterRecorder {

    @Override
    public void record(ActvMatterCnfmData data) {
        // no-op
    }
}
