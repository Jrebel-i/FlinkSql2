package com.hc.metric_module;

import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.KafkaDeserializationSchema;

import java.util.Properties;

//其他方法全部使用父类的，只是在其基础上添加了Mertic的初始化和环境设置相关
public class CustomerKafkaConsumer<T>  extends FlinkKafkaConsumer<T>{
    private AbsDeserialization<T> valueDeserializer;

    public CustomerKafkaConsumer(String topic, AbsDeserialization<T> valueDeserializer, Properties props){
        super(topic,valueDeserializer,props);
        this.valueDeserializer=valueDeserializer;
    }

    @Override
    public void run(SourceContext<T> sourceContext) throws Exception {
        valueDeserializer.setRuntimeContext(getRuntimeContext());
        valueDeserializer.initMetric();
        super.run(sourceContext);
    }
}
