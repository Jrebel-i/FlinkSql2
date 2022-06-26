package com.hc.cep_module.cep2.utils;

import com.googlecode.aviator.AviatorEvaluator;
import org.apache.flink.cep.pattern.conditions.IterativeCondition;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.StreamSupport;

public class SumIterativeCondition extends IterativeCondition<HashMap<String,Object>> {

    private double sum;
    private String script;
    private String fieldName;

    public SumIterativeCondition(double sum,String scrpit,String fieldName){
        this.sum=sum;
        this.script=scrpit;
        this.fieldName=fieldName;
        System.out.println("FilterCondition传入参数："+sum+"====="+script+"====="+fieldName);
    }

    @Override
    public boolean filter(HashMap<String,Object> value, Context<HashMap<String,Object>> ctx) throws Exception {

        Map<String,Object> params=new HashMap<>();
        params.put("data",value);

        if((Boolean) AviatorEvaluator.execute(script,params)){
            double sumNow= Double.valueOf((String)value.get(fieldName))+ StreamSupport.stream(ctx.getEventsForPattern("start").spliterator(),false)
                    .map(x->Double.valueOf((String)value.get(fieldName))).reduce((acc,item)->{
                        return acc+item;
                    }).orElse(0.0);
            return sumNow>sum;
        }
        return false;
    }
}