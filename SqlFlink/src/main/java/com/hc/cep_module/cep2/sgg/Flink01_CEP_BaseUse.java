package com.hc.cep_module.cep2.sgg;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * @Author lizhenchao@atguigu.cn
 * @Date 2021/4/10 14:01
 */
public class Flink01_CEP_BaseUse {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        
        SingleOutputStreamOperator<WaterSensor> waterSensorStream = env
                .socketTextStream("localhost",9999)
            .map(new MapFunction<String, WaterSensor>() {
                @Override
                public WaterSensor map(String value) throws Exception {
                    String[] split = value.split(",");
                    return new WaterSensor(split[0],
                                           Long.parseLong(split[1]) * 1000,
                                           Integer.parseInt(split[2]));
                }
            })
            .assignTimestampsAndWatermarks(
                WatermarkStrategy
                    .<WaterSensor>forBoundedOutOfOrderness(Duration.ofSeconds(5))
                    .withTimestampAssigner((element, recordTimestamp) -> element.getTs())
            );
        
        // 1. 定义模式
        Pattern<WaterSensor, WaterSensor> pattern = Pattern
            .<WaterSensor>begin("start")
            .where(new SimpleCondition<WaterSensor>() {
                @Override
                public boolean filter(WaterSensor value) throws Exception {
                    return value.getId().equalsIgnoreCase("sensor_1");
                }
            });
        // 2. 把模式运用在流上, 返回值就是复合模式的数据组成的流
        PatternStream<WaterSensor> ps = CEP.pattern(waterSensorStream, pattern);
    
        // 3. 获取满足模式的数据(放在流中)
        ps
            .select(new PatternSelectFunction<WaterSensor, String>() {
                @Override
                public String select(Map<String, List<WaterSensor>> pattern) throws Exception {
                    return pattern.get("start").toString();
                }
            })
            .print();
    
        env.execute();
        
    }
}
