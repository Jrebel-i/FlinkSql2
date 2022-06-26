package com.hc.cep_module.cep2;

import com.alibaba.fastjson.JSONObject;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.state.BroadcastState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.state.ReadOnlyBroadcastState;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.RestOptions;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.BroadcastStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.BroadcastProcessFunction;
import org.apache.flink.streaming.api.functions.co.CoProcessFunction;
import org.apache.flink.streaming.api.functions.co.KeyedBroadcastProcessFunction;
import org.apache.flink.util.Collector;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public class FlinkCEPTestCepDy {
    public static void main(String[] args) throws Exception {
        //socket数据：{"dpId":"1","value":40.0}
        //1,40.0,10
        Configuration conf=new Configuration();
        conf.setInteger(RestOptions.PORT,8888);
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment(conf);
        env.setParallelism(1);
        env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);

        //数据：1,40,10  水印要递增
        DataStreamSource<String> socketTextStream = env.socketTextStream("localhost",9999);
        SingleOutputStreamOperator<DpData> mapStream = socketTextStream.map(new MapFunction<String, DpData>() {
            @Override
            public DpData map(String value) throws Exception {
                String[] split = value.split(",");
                if(split.length>3){
                    return new DpData(split[0]
                            ,Double.valueOf(split[1])
                            ,System.currentTimeMillis(),
                            Integer.valueOf(split[3]));
                }else{
                    return new DpData(split[0]
                            ,Double.valueOf(split[1])
                            ,System.currentTimeMillis());
                }

            }
        });
//                .assignTimestampsAndWatermarks(WatermarkStrategy
//                .<DpData>forBoundedOutOfOrderness(Duration.ofSeconds(0))
//                .withTimestampAssigner((element, recordTimestamp) -> element.getTs()));

        Pattern myPattern = new GetPattern().getMyPattern(); //初始化默认使用 规则1
        //======================================================================//
//        System.out.println(myPattern); //不要打印Pattern ，否则会导致匹配延迟

//        PatternStream<DpData> patternStream = CEP.pattern(mapStream, myPattern);
//        patternStream.select(new PatternSelectFunction<DpData, String>() {
//            @Override
//            public String select(Map<String, List<DpData>> pattern) throws Exception {
//                return pattern.toString();
//            }
//        }).print();
        //广播流  1  2
//        DataStreamSource<String> boardStream = env.socketTextStream("localhost",10000);
//        SingleOutputStreamOperator<DpData> outputStreamOperator = boardStream.map(new MapFunction<String, DpData>() {
//            @Override
//            public DpData map(String value) throws Exception {
//                DpData dpData = new DpData(Integer.valueOf(value));
//                dpData.setDpId(value);
//                dpData.setTs(System.currentTimeMillis());
//                return dpData; //广播流选择规则
//            }
//        });
//
//        MapStateDescriptor<String,DpData> mapStateDescriptor = new MapStateDescriptor<String, DpData>("register", String.class,DpData.class);
//        BroadcastStream<DpData> broadcastStream = outputStreamOperator.broadcast(mapStateDescriptor);
//        SingleOutputStreamOperator<DpData> processStream = mapStream
//                .keyBy(DpData::getDpId)
//                .connect(broadcastStream)
//                .process(new KeyedBroadcastProcessFunction<Object, DpData, DpData, DpData>() {
//                    private MapStateDescriptor<String, DpData> mapStateDescriptor;
//                    private Integer beforeRule = 1;
//
//                    @Override
//                    public void open(Configuration parameters) throws Exception {
//                        super.open(parameters);
//                        mapStateDescriptor = new MapStateDescriptor<String, DpData>("register", String.class, DpData.class);
//                    }
//
//                    @Override
//                    public void processElement(DpData dpData, ReadOnlyContext readOnlyContext, Collector<DpData> collector) throws Exception {
//                        collector.collect(dpData);
//                    }
//
//                    @Override
//                    public void processBroadcastElement(DpData dpData, Context context, Collector<DpData> collector) throws Exception {
//
//                    }
//                });
//        processStream.print("原数据：");

        // 2. 在流上应用模式
        PatternStream waterSensorPS = CEP.pattern(mapStream, myPattern);
        waterSensorPS.registerListener(new CepListener<DpData>() {
            @Override
            public void init() {
                System.out.println("初始化");
            }

            @Override
            public Boolean needChange(DpData element) {
//                System.out.println("判断规则"+element);
                if(element.getDataOrRule()!=null){
                    System.out.println("修改规则为："+element.getDataOrRule());
                    return true;
                }
                return false;
            }

            @Override
            public Pattern returnPattern(DpData flagElement) throws Exception {
                if(flagElement.getDataOrRule()!=null){
                    if(flagElement.getDataOrRule()==1){
                        return new GetPattern().getMyPattern();
                    }else if(flagElement.getDataOrRule()==2){
                        return new GetPattern().getMyPattern2();
                    }
                }
                return null;
            }
        })
       .select(new PatternSelectFunction<DpData, String>() {
            @Override
            public String select(Map<String, List<DpData>> pattern) throws Exception {
                return pattern.toString();
            }
        }).print();

        try {
            env.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
