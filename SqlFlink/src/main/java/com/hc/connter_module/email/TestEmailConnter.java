package com.hc.connter_module.email;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.table.functions.ScalarFunction;

public class TestEmailConnter {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);
        //直接读取163邮箱数据
        tableEnv.executeSql("CREATE TABLE T (\n" +
                "    subject STRING,\n" +
//                "    sent TIMESTAMP(3),\n" +
//                "    received TIMESTAMP(3)\n" +
                " times bigint \n"+
                ") WITH (\n" +
                "    'connector' = 'imap',\n" +
                "    'host' = 'imap.163.com',\n" +
                "    'port' = '143',\n" +
                "    'user' = 'jrebel_i@163.com',\n" +
                "    'password' = 'SRBKZFVQCHFLCZJP'\n" +
                ")");
        tableEnv.executeSql("select * from T").print();
    }


}
