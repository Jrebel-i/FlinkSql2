package com.hc.hive_module;

import com.alibaba.fastjson.JSONObject;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.table.catalog.DataTypeFactory;
import org.apache.flink.table.functions.ScalarFunction;
import org.apache.flink.table.types.inference.TypeInference;

import java.util.Arrays;

public class TestHive_GetJsonObject {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
//        EnvironmentSettings settings = EnvironmentSettings.newInstance().useBlinkPlanner().build();
//        TableEnvironment tableEnv = TableEnvironment.create(settings);
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);
//        tableEnv.executeSql("SHOW FUNCTIONS").print();
        tableEnv.executeSql("LOAD MODULE hive WITH ('hive-version' = '3.1.2')");
//        tableEnv.executeSql("SHOW MODULES").print();
        tableEnv.executeSql("USE MODULES hive, core");
//        tableEnv.executeSql("SHOW MODULES").print();
//        tableEnv.executeSql("SHOW FUNCTIONS").print();

//        JSONObject object = new JSONObject();
//        object.put("owner","zhangsan");
        DataStream<String> dataStream =env.fromElements("{\"owner\":\"zhangsan\"}");
        Table table = tableEnv.fromDataStream(dataStream);

        tableEnv.createTemporaryView("tt",table);
        tableEnv.createTemporaryFunction("MyJson",MyJsonFunction.class);

        tableEnv.executeSql("CREATE TABLE print_table (\n" +
                "  eventType STRING,\n" +
                "  fromType STRING,\n" +
                "  columnType STRING\n" +
                ") WITH (\n" +
                "  'connector' = 'print'\n" +
                ")");
//        tableEnv.executeSql("select str_to_map(MyJson(f0))['eventType'] as mp from tt").print();
        tableEnv.executeSql("" +
                " insert into print_table" +
                " select \n" +
        "  mp['eventType'] AS eventType,\n" +
                "  mp['fromType'] AS fromType,\n" +
                "  mp['columnType'] AS columnType \n" +
                " from( \n" +
                " select str_to_map(MyJson(f0)) as mp from tt \n" +
                ")");//get_json_object(f0,'$.owner') as xx

//        //获取sql explain执行计划
//        System.out.println(tableEnv.explainSql("" +
//                " insert into print_table" +
//                " select \n" +
//                "  mp['eventType'] AS eventType,\n" +
//                "  mp['fromType'] AS fromType,\n" +
//                "  mp['columnType'] AS columnType \n" +
//                " from( \n" +
//                " select str_to_map(MyJson(f0)) as mp from tt \n" +
//                ")"));
    }

    public static class MyJsonFunction extends ScalarFunction{
        public String eval(String str){
            return "eventType:play,fromType:CN,columnType:1";//"\"eventType\":\"play\",\"fromType\":\"CN\",\"columnType\":1";
        }
    }
}
