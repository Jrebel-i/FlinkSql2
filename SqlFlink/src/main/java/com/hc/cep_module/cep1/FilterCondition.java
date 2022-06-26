package com.hc.cep_module.cep1;

import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import com.googlecode.aviator.AviatorEvaluator;

import java.util.HashMap;
import java.util.Map;

public class FilterCondition extends SimpleCondition<Map<String,Object>> {
    private String script;
    private String fieldName;
    public FilterCondition(String script,String fieldName){
        this.script=script;
        this.fieldName=fieldName;
        //加载自定义的函数
        AviatorEvaluator.addFunction(new ParseValueFunction(this.fieldName));
    }
    //filter 方法表示的是条件判断
    @Override
    public boolean filter(Map<String,Object> value) throws Exception {
        Map<String,Object> params=new HashMap<>();
        params.put("data",value);
        return (Boolean) AviatorEvaluator.execute(script,params);
    }
}
