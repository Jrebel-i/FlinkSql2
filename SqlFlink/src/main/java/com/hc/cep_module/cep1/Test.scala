package com.hc.cep_module.cep1

import javax.script.{Invocable, ScriptEngineManager}
import org.apache.flink.cep.pattern.Pattern

object Test {

  case class  DpData(dpId:String,value:Double)

  def main(args: Array[String]): Unit = {

    val groovyScript=
      """
      import cep.FilterCondition
      import cep.SumIterativeCondition
      import org.apache.flink.cep.scala.pattern.Pattern
      import AfterMatchSkipStrategy
      where1=new FilterCondition("getValue(data)>10","value")
      where2=new SumIterativeCondition(_sum_,"_script_","_fieldName_")
      def getPattern(){
      return Pattern.begin("start",AfterMatchSkipStrategy.skipPastLastEvent()).where(where1).times(2).consecutive().next("next").where(where2)
      }
      """.stripMargin

    val factory = new ScriptEngineManager()
    val engine =  factory.getEngineByName("groovy")
    engine.eval(groovyScript)
    val p = engine.asInstanceOf[Invocable].invokeFunction("getPattern").asInstanceOf[Pattern[DpData,DpData]]

  }
}
