package com.hc.cep_module.cep2;

import org.apache.flink.cep.pattern.conditions.IterativeCondition;

public class EqualCondition extends IterativeCondition<DpData> {
    @Override
    public boolean filter(DpData dpData, Context<DpData> context) throws Exception {
        return "1".equals(dpData.getDpId());
    }
}
