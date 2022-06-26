package com.hc.metric_module;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.RestOptions;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.IOException;
import java.util.Properties;

public class countMetric {
    public static void main(String[] args) throws Exception {
        Configuration conf=new Configuration();
        conf.setInteger(RestOptions.PORT,8081);
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment(conf);
        env.setParallelism(1);
        // 0.Kafka相关配置
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "s1:9092,s2:9092,s3:9092");
        properties.setProperty("group.id", "flink_source_kafka");
        properties.setProperty("auto.offset.reset", "latest");

//        env
//                .addSource(new FlinkKafkaConsumer<>("sensor", new SimpleStringSchema(), properties))
//                .print("kafka source");
        DataStreamSource<String> streamSource = env
                .addSource(new CustomerKafkaConsumer<>("ods_db", new AbsDeserialization<String>() {
                    //KafkaConsumer会初始化counter，并且将runtimeContext设置进去
                    //实现反序列化方法
                    @Override
                    public String deserialize(byte[] message) throws IOException {
                        try {
                            String str = new String(message);
                            JSONObject object = JSON.parseObject(str);
                            normalDataNum.inc();//正常数据指标
                            return str;
                        } catch (Exception e) {
                            dirtyDataNum.inc();//脏数据指标
                            return null;
                        }
                    }
                }, properties));
        streamSource.print();
        env.execute();


    }
}
