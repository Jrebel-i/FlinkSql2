package com.hc.hive_module;

import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.TableEnvironment;

public class TestHiveUdf {
    public static void main(String[] args) {
        EnvironmentSettings settings = EnvironmentSettings.newInstance().useBlinkPlanner().build();
        TableEnvironment tableEnv = TableEnvironment.create(settings);
        // 展示加载和启⽤的 Module
        tableEnv.executeSql("SHOW MODULES").print();
        // +-------------+
        // | module name |
        // +-------------+
        // | core |
        // +-------------+
        tableEnv.executeSql("SHOW FULL MODULES").print();
        // +-------------+------+
        // | module name | used |
        // +-------------+------+
        // | core | true |
        // +-------------+------+
        // 加载 hive module
        tableEnv.executeSql("LOAD MODULE hive WITH ('hive-version' = '3.1.2')");
        // 展示所有启⽤的 module
        tableEnv.executeSql("SHOW MODULES").print();
        // +-------------+
        // | module name |
        // +-------------+
        // | core |
        // | hive |
        // +-------------+
        // 展示所有加载的 module 以及它们的启⽤状态
        tableEnv.executeSql("SHOW FULL MODULES").print();
        // +-------------+------+
        // | module name | used |
        // +-------------+------+
        // | core | true |
        // | hive | true |
        // +-------------+------+
        // 改变 module 解析顺序
        tableEnv.executeSql("USE MODULES hive, core");
        tableEnv.executeSql("SHOW MODULES").print();
        // +-------------+
        // | module name |
        // +-------------+
        // | hive |
        // | core |
        // +-------------+
        tableEnv.executeSql("SHOW FULL MODULES").print();
        // +-------------+------+
        // | module name | used |
        // +-------------+------+
        // | hive | true |
        // | core | true |
        // +-------------+------+
        // 禁⽤ core module
        tableEnv.executeSql("USE MODULES hive");
        tableEnv.executeSql("SHOW MODULES").print();
        // +-------------+
        // | module name |
        // +-------------+
        // | hive |
        // +-------------+
        tableEnv.executeSql("SHOW FULL MODULES").print();
        // +-------------+-------+
        // | module name | used |
        // +-------------+-------+
        // | hive | true |
        // | core | false |
        // +-------------+-------+
        // 卸载 hive module
        tableEnv.executeSql("UNLOAD MODULE hive");
        tableEnv.executeSql("SHOW MODULES").print();
        // Empty set
        tableEnv.executeSql("SHOW FULL MODULES").print();
        // +-------------+-------+
        // | module name | used |
        // +-------------+-------+
        // | hive | false |
        // +-------------+-------+
    }
}
