package com.hc.hive_module;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.table.api.internal.TableEnvironmentImpl;
import org.apache.flink.table.operations.Operation;

import java.util.List;

public class TestRegularJoin {
    public static void main(String[] args) {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);
        tableEnv.executeSql("LOAD MODULE hive WITH ('hive-version' = '3.1.2')");
        tableEnv.executeSql("USE MODULES hive, core");

        DataStream<String> dataStream =env.fromElements("{\"id\":1,\"name\":\"zhangsan\"}");
        Table table = tableEnv.fromDataStream(dataStream);
        tableEnv.createTemporaryView("leftT",table);
        DataStream<String> dataStream2 =env.fromElements("{\"id\":1,\"sex\":\"man\"}");
        Table table2= tableEnv.fromDataStream(dataStream2);
        tableEnv.createTemporaryView("rightT",table2);

        String sql="" +
                " select l.id,l.name,r.sex from" +
                "(" +
                "   select get_json_object(f0,'$.id') as id" +
                "         ,get_json_object(f0,'$.name') as name from leftT" +
                ") as l " +
                " inner join" +
                "(" +
                "   select get_json_object(f0,'$.id') as id" +
                "         ,get_json_object(f0,'$.sex') as sex from rightT" +
                ") as  r" +
                " on l.id=r.id" +
                " ";
//        System.out.println(tableEnv.explainSql(sql));
        tableEnv.executeSql(sql).print();
//        List<Operation> operationList = ((TableEnvironmentImpl) tableEnv).getParser().parse(sql);
//        Operation operation = operationList.get(0);
        //1.  通过TableEnvironmentImpl得到解析器对SQL进行解析，获取operations
        //2.  分为PlannerQueryOperation、CatalogSinkModifyOperation进行，得到CalciteTree
        //3.  CalciteTree本质上就是Calcite的一颗RelNode Tree，对这个RelNode Tree进行递归先序遍历
        //4.  在遍历处理函数中对当前RelNode包含的字段进行打印，打印操作包含两个重要的索引：字段继承自上游索引的索引值，
        // 重新编排本字段在当前遍历操作中的顺序索引值。
        // 注意：Join操作还需要对当前遍历操作中的顺序索引值进行相同层次内的继承递增。
    }
}
