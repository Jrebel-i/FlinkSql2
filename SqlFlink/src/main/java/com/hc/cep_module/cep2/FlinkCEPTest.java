package com.hc.cep_module.cep2;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.RestOptions;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public class FlinkCEPTest {
    public static void main(String[] args) {
        //socket数据：{"dpId":"1","value":40.0}
        //1,40.0,10
        Configuration conf=new Configuration();
        conf.setInteger(RestOptions.PORT,8888);
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment(conf);
        env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);

        env.setParallelism(1);
        DataStreamSource<String> socketTextStream = env.socketTextStream("localhost",9999);
        SingleOutputStreamOperator<DpData> mapStream = socketTextStream.map(new MapFunction<String, DpData>() {
            @Override
            public DpData map(String value) throws Exception {
                String[] split = value.split(",");
                return new DpData(split[0]
                        ,Double.valueOf(split[1])
                        ,Long.parseLong(split[2]) * 1000);//
            }
        }); //如果是事件时间，需要设置水印及延迟
//                .assignTimestampsAndWatermarks(WatermarkStrategy
//                .<DpData>forBoundedOutOfOrderness(Duration.ofSeconds(0)
//                )
//                .withTimestampAssigner((element, recordTimestamp) -> element.getTs()));
        mapStream.print();
//        SingleOutputStreamOperator<DpData> mapStream = env
//                .readTextFile("input/sensor2.txt")
//                .map(new MapFunction<String, DpData>() {
//                    @Override
//                    public DpData map(String value) throws Exception {
//                        String[] split = value.split(",");
//                        return new DpData(split[0],
//                                Double.valueOf(split[1]),
//                                Long.parseLong(split[2]) * 1000
//                                );
//                    }
//                })
//                .assignTimestampsAndWatermarks(WatermarkStrategy
//                        .<DpData>forBoundedOutOfOrderness(Duration.ofSeconds(5))
//                        .withTimestampAssigner((element, recordTimestamp) -> element.getTs()));


//        Pattern<DpData, DpData> pattern = Pattern
//                .<DpData>begin("start")
//                .where(new SimpleCondition<DpData>() {
//                    @Override
//                    public boolean filter(DpData value) throws Exception {
//                        return "1".equals(value.getDpId());
//                    }
//                });
//        System.out.println(pattern);//不要打印Pattern ，否则会导致匹配延迟
        Pattern myPattern = new GetPattern().getMyPattern();
//        System.out.println(myPattern);//不要打印Pattern ，否则会导致匹配延迟


        // 2. 在流上应用模式
        PatternStream<DpData> waterSensorPS = CEP.pattern(mapStream, myPattern);
//        PatternStream<DpData> patternStream = CEP.pattern(mapStream, pattern2);
//        waterSensorPS.registerListener(new CepListener<JSONObject>() {
//            @Override
//            public void init() {
//                System.out.println("初始化");
//            }
//
//            @Override
//            public Boolean needChange(JSONObject element) {
//                return null;
//            }
//
//            @Override
//            public Pattern returnPattern(JSONObject flagElement) throws Exception {
//                return null;
//            }
//        }).select(new PatternSelectFunction() {
//            @Override
//            public String select(Map pattern) throws Exception {
//                return pattern.toString();
//            }
//        }).print();

//        // 3. 获取匹配到的结果
        waterSensorPS
                .select(new PatternSelectFunction<DpData, String>() {
                    @Override
                    public String select(Map<String, List<DpData>> pattern) throws Exception {
                        return pattern.get("start").toString();
                    }
                })
                .print();

        try {
            env.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
