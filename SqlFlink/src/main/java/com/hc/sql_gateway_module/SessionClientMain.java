package com.hc.sql_gateway_module;

public class SessionClientMain {
    private static final String CREATE_SOURCE_SQL = "CREATE TABLE source_table (\n" +
            " f_sequence INT,\n" +
            " f_random INT,\n" +
            " f_random_str STRING,\n" +
            " ts AS localtimestamp,\n" +
            " WATERMARK FOR ts AS ts\n" +
            ") WITH (\n" +
            " 'connector' = 'datagen'\n" +
            ")";

    private static final String CREATE_PRINT_SQL = "CREATE TABLE print_table WITH ('connector' = 'print') LIKE source_table (EXCLUDING ALL)";

    private static final String SHOW_TABLES_SQL = "show tables";

    private static final String CREATE_JOB_SQL = "insert into print_table(f_sequence, f_random, f_random_str)  select f_sequence, f_random, f_random_str  from source_table";


    public static void main(String[] args) throws Exception {
        System.out.println(CREATE_SOURCE_SQL);
        System.out.println(CREATE_PRINT_SQL);
        System.out.println(SHOW_TABLES_SQL);
        System.out.println(CREATE_JOB_SQL);
        SessionClient session = new SessionClient("127.0.0.1", 8083, "streaming");
        System.out.println(session.submitStatement(CREATE_SOURCE_SQL).getResults());
        System.out.println(session.submitStatement(CREATE_PRINT_SQL).getResults());
        System.out.println(session.submitStatement(SHOW_TABLES_SQL).getResults());
        System.out.println(session.submitStatement(CREATE_JOB_SQL).getResults());
        session.close();

    }
}