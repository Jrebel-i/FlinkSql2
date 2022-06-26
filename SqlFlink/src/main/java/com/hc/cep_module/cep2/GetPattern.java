package com.hc.cep_module.cep2;

import com.alibaba.fastjson.JSONObject;
import org.apache.flink.cep.pattern.Pattern;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class GetPattern {
    public Pattern getMyPattern(){
        String groovyScript=
                " import com.hc.cep_module.cep2.EqualCondition \n" +
                        " import org.apache.flink.cep.pattern.Pattern \n" +
                        " import com.hc.cep_module.cep2.DpData \n" +
                        " where1=new EqualCondition<DpData>() \n" +
                        "def getPattern(){\n" +
                        "return Pattern.<DpData>begin(\"start\")." +
                        "where(where1)" +
                        "}\n";
        System.out.println("grovvy脚本为："+groovyScript);
        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine =  factory.getEngineByName("groovy");
        assert engine != null;
        try {
            engine.eval(groovyScript);
            Pattern pattern = (Pattern)((Invocable)engine).invokeFunction("getPattern");
            return pattern;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }  public Pattern getMyPattern2(){
        String groovyScript=
                " import com.hc.cep_module.cep2.EqualCondition2 \n" +
                        " import org.apache.flink.cep.pattern.Pattern \n" +
                        " import com.hc.cep_module.cep2.DpData \n" +
                        " where1=new EqualCondition2<DpData>() \n" +
                        "def getPattern(){\n" +
                        "return Pattern.<DpData>begin(\"start\")." +
                        "where(where1)" +
                        "}\n";
        System.out.println("grovvy脚本为："+groovyScript);
        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine =  factory.getEngineByName("groovy");
        assert engine != null;
        try {
            engine.eval(groovyScript);
            Pattern pattern = (Pattern)((Invocable)engine).invokeFunction("getPattern");
            return pattern;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
