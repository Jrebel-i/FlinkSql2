package com.hc.cep_module.cep2;
import org.apache.flink.cep.pattern.Pattern;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class Test {

    public static void main(String[] args) throws ScriptException, NoSuchMethodException {
        String groovyScript=
                " import com.hc.cep_module.cep2.utils.FilterCondition \n" +
                " import com.hc.cep_module.cep2.utils.SumIterativeCondition \n" +
                " import Pattern \n" +
                " import AfterMatchSkipStrategy \n" +
                " where1=new FilterCondition(\"getValue(data)>10\",\"value\") \n" +
                " where2=new SumIterativeCondition(100,\"getValue(data)>10\",\"value\") \n" +
                " def getPattern(){ \n" +
                " return Pattern.begin(\"start\",AfterMatchSkipStrategy.skipPastLastEvent()).where(where1).times(2).consecutive().next(\"next\").where(where2)\n" +
                "}";
        System.out.println(groovyScript);
        ScriptEngineManager  factory = new ScriptEngineManager();
        ScriptEngine engine =  factory.getEngineByName("groovy");
        assert engine != null;
        engine.eval(groovyScript);
        Pattern pattern = (Pattern)((Invocable)engine).invokeFunction("getPattern");
        System.out.println(pattern);


    }
}
