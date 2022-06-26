package com.hc.cep_module.cep2.utils;

import com.googlecode.aviator.AviatorEvaluator;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;

import java.util.HashMap;
import java.util.Map;

public class FilterCondition extends SimpleCondition<Map<String,Object>> {

    private String script;
    private String fieldName;
    public FilterCondition(String script,String fieldName){ //getValue(data)>10=====value
        System.out.println("FilterCondition传入参数："+script+"====="+fieldName);
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
        System.out.println(params.toString());
        //这个方法就是从data中获取到 value字段的值  然后与10进行比较
        return (Boolean) AviatorEvaluator.execute(script,params);// getValue(data)>10 {"data":{"value":1}}
    }
}