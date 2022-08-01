package com.hc.state_process_module;

import com.github.shyiko.mysql.binlog.BinaryLogFileReader;
import com.github.shyiko.mysql.binlog.event.Event;
import com.github.shyiko.mysql.binlog.event.deserialization.ChecksumType;
import com.github.shyiko.mysql.binlog.event.deserialization.EventDeserializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 从数据库导出binlog文件
 * mysqlbinlog \
 * --read-from-remote-server \
 * --host=xxx.xxx.xxx.xxx \
 * --port=3306  \
 * --user root \
 * --password \
 * --raw \
 * --result-file=/home/ubuntu/ \
 * mysql-bin-changelog.000005
 *
 * 输入密码
 */

/**
 * 解析binlog数据
 */
public class readSqlBinlog {
    public static void main(String[] args) throws IOException {
        String filePath = "C:\\Users\\Administrator\\Desktop\\mysql-bin-changelog.000005";
        File binlogFile = new File(filePath);
        EventDeserializer eventDeserializer = new EventDeserializer();
        eventDeserializer.setChecksumType(ChecksumType.CRC32);
        BinaryLogFileReader reader = new BinaryLogFileReader(binlogFile,
                eventDeserializer);
        try {
            // 准备写入的文件名称
            /*
             * File f1 = new File("D:\\mysql-bin.000845.sql"); if
             * (f1.exists()==false){ f1.getParentFile().mkdirs(); }
             */
            FileOutputStream fos = new FileOutputStream(
                    "C:\\Users\\Administrator\\Desktop\\mysql-bin-changelog_watch.000005", true);
            for (Event event; (event = reader.readEvent()) != null;) {
//                System.out.println(event.toString());

                // 把数据写入到输出流
                fos.write(event.toString().getBytes());
            }
            // 关闭输出流
            fos.close();
            System.out.println("输入完成");
        } finally {
            reader.close();
        }

    }
}
