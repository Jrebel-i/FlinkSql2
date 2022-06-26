package com.hc.cep_module.cep2;

import org.apache.flink.cep.pattern.conditions.IterativeCondition;

public class EqualCondition2 extends IterativeCondition<DpData> {
    @Override
    public boolean filter(DpData dpData, Context<DpData> context) throws Exception {
        return "2".equals(dpData.getDpId());
    }
}
