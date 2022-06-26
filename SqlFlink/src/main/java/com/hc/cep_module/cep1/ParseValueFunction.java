package com.hc.cep_module.cep1;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorDouble;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.util.Map;

public class ParseValueFunction extends AbstractFunction {
    private String fieldName; //value
    public ParseValueFunction(String fieldName){
        this.fieldName=fieldName;
    }

    @Override public String getName() {
        return "getValue"; //定义函数名称
    }
    //env 就是上述的params 入参，arg1表示的就是 data参数
    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {

        Map<String,Object> map= (Map<String,Object>) FunctionUtils.getJavaObject(arg1,env);
        Double value=Double.valueOf((String)map.get(fieldName));
        return AviatorDouble.valueOf(value);
    }
}
