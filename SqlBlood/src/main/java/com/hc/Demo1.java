package com.hc;

import org.apache.hadoop.hive.ql.parse.ParseException;
import org.apache.hadoop.hive.ql.parse.SemanticException;
import org.apache.hadoop.hive.ql.tools.LineageInfo;

/**
 * 借助LineageInfo分析Hive表关系
 */
public class Demo1 {
    public static void main(String[] args) throws ParseException, SemanticException {
        String query = "WITH tempa AS (\n" +
                "\t\tSELECT *\n" +
                "\t\tFROM tablea\n" +
                "\t\tWHERE col1 = 'abc'\n" +
                "\t)\n" +
                "INSERT INTO tabled\n" +
                "SELECT *\n" +
                "FROM tableb b\n" +
                "\tINNER JOIN tempa a ON b.col3 = a.col3\n" +
                "WHERE b.col4 IN (\n" +
                "\tSELECT col4\n" +
                "\tFROM tablec\n" +
                ")";
        LineageInfo pares = new LineageInfo();
        pares.getLineageInfo(query);
        for (String tab : pares.getInputTableList()) {
            System.out.println("InputTable= "+tab);
        }
        for (String tab : pares.getOutputTableList()) {
            System.out.println("OutputTable= "+tab);
        }
    }
}
