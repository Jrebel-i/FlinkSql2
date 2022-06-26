package com.hc.cep_module.cep2;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
//@AllArgsConstructor
@NoArgsConstructor
public class DpData {
    private String dpId;
    private Double value;
    private Long Ts;
    private Integer dataOrRule;

    public DpData(String dpId, Double value, Long ts) {
        this.dpId = dpId;
        this.value = value;
        Ts = ts;
    }

//    public DpData(Integer dataOrRule) {
//        this.dataOrRule = dataOrRule;
//    }

    public DpData(String dpId, Double value, Long ts, Integer dataOrRule) {
        this.dpId = dpId;
        this.value = value;
        Ts = ts;
        this.dataOrRule = dataOrRule;
    }
}
