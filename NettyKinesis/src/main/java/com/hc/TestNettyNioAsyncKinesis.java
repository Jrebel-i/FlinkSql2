package com.hc;

import org.apache.flink.kinesis.shaded.com.amazonaws.ClientConfiguration;
import org.apache.flink.kinesis.shaded.com.amazonaws.ClientConfigurationFactory;
import org.apache.flink.kinesis.shaded.com.amazonaws.services.kinesis.AmazonKinesisAsyncClient;
import org.apache.flink.kinesis.shaded.software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import org.apache.flink.kinesis.shaded.software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import org.apache.flink.kinesis.shaded.software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import org.apache.flink.streaming.connectors.kinesis.config.AWSConfigConstants;
import org.apache.flink.streaming.connectors.kinesis.config.ConsumerConfigConstants;
import org.apache.flink.streaming.connectors.kinesis.internals.publisher.fanout.FanOutRecordPublisherConfiguration;
import org.apache.flink.streaming.connectors.kinesis.internals.publisher.fanout.StreamConsumerRegistrar;
import org.apache.flink.streaming.connectors.kinesis.proxy.FullJitterBackoff;
import org.apache.flink.streaming.connectors.kinesis.proxy.KinesisProxyV2;
import org.apache.flink.streaming.connectors.kinesis.proxy.KinesisProxyV2Factory;
import org.apache.flink.streaming.connectors.kinesis.proxy.KinesisProxyV2Interface;
import org.apache.flink.streaming.connectors.kinesis.util.AwsV2Util;
import org.apache.flink.streaming.connectors.kinesis.util.StreamConsumerRegistrarUtil;
//import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
//import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;

import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import static java.util.Collections.emptyList;
import static org.apache.flink.streaming.connectors.kinesis.config.ConsumerConfigConstants.EFO_CONSUMER_NAME;
import static org.apache.flink.streaming.connectors.kinesis.config.ConsumerConfigConstants.efoConsumerArn;

public class TestNettyNioAsyncKinesis {
    private static final FullJitterBackoff BACKOFF = new FullJitterBackoff();

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Properties configProps = new Properties();
        String stream="measuremen_data";

        configProps.put(AWSConfigConstants.AWS_REGION, "us-east-1");
        configProps.put(ConsumerConfigConstants.STREAM_INITIAL_POSITION, "LATEST");
//        //每多少时间去发现新分区
        configProps.setProperty(ConsumerConfigConstants.SHARD_DISCOVERY_INTERVAL_MILLIS,"60000");
        //设置消费者重试次数
        configProps.put(ConsumerConfigConstants.SHARD_GETRECORDS_RETRIES, "6");
//        //单个 GetRecords 调用中获取的记录数量
//        inputProperties.put(ConsumerConfigConstants.SHARD_GETRECORDS_MAX, count+"");
        //从数据流中增加两次读取操作之间的时间也是合理的。当您将默认值 200 毫秒增加到 1 秒时，延迟会略有增加，但它有助于多个使用者从同一数据流中读取数据。
        configProps.put(ConsumerConfigConstants.SHARD_GETRECORDS_INTERVAL_MILLIS, 400 + "");

        configProps.put(ConsumerConfigConstants.RECORD_PUBLISHER_TYPE,
                ConsumerConfigConstants.RecordPublisherType.EFO.name());

        configProps.put(ConsumerConfigConstants.EFO_CONSUMER_NAME, "test_async");
        configProps.put(ConsumerConfigConstants.EFO_REGISTRATION_TYPE,
                ConsumerConfigConstants.EFORegistrationType.EAGER.name());
        //增大flink.shard.subscribetoshard.maxretries 最大尝试次数为30
        configProps.put(ConsumerConfigConstants.SUBSCRIBE_TO_SHARD_RETRIES, 30 +"");

        //向流注册消费者
        FullJitterBackoff backoff = new FullJitterBackoff();
        FanOutRecordPublisherConfiguration configuration =
                new FanOutRecordPublisherConfiguration(configProps, Collections.singletonList(stream));


//////////////////////////////////////////////////////////////////////////////////////
//        KinesisProxyV2Interface kinesis = KinesisProxyV2Factory.createKinesisProxyV2(configProps);
//        StreamConsumerRegistrar registrar = new StreamConsumerRegistrar(kinesis, configuration, backoff);
//        String streamConsumerName = configProps.getProperty(EFO_CONSUMER_NAME);
//        try {
//            //注册消费者并获取Arn
//            String streamConsumerArn =
//                    registrar.registerStreamConsumer(stream, streamConsumerName);
//            configProps.setProperty(efoConsumerArn(stream), streamConsumerArn);
//            System.out.println(streamConsumerArn);
//        } catch (ExecutionException ex) {
//            throw new StreamConsumerRegistrarUtil.FlinkKinesisStreamConsumerRegistrarException(
//                    "Error registering stream: " + stream, ex);
//        } catch (InterruptedException ex) {
//            Thread.currentThread().interrupt();
//            throw new StreamConsumerRegistrarUtil.FlinkKinesisStreamConsumerRegistrarException(
//                    "Error registering stream: " + stream, ex);
//        }

        final ClientConfiguration clientConfiguration =
                new ClientConfigurationFactory().getConfig();

        //将 HTTP 客户端直接传递给服务客户端
        final SdkAsyncHttpClient httpClient = // HTTP 客户端
                AwsV2Util.createHttpClient(
                        clientConfiguration, NettyNioAsyncHttpClient.builder(), configProps);
        final FanOutRecordPublisherConfiguration publisherConfiguration =
                new FanOutRecordPublisherConfiguration(configProps, emptyList());
        final KinesisAsyncClient client = // 服务客户端
                AwsV2Util.createKinesisAsyncClient(configProps, clientConfiguration, httpClient);

        KinesisProxyV2Interface kinesis = new KinesisProxyV2(client, httpClient, publisherConfiguration, BACKOFF);
        //获取到流消费注册者
        StreamConsumerRegistrar registrar = new StreamConsumerRegistrar(kinesis, configuration, backoff);

        String streamConsumerName = configProps.getProperty(EFO_CONSUMER_NAME);


        try {
            String streamConsumerArn =
                    registrar.registerStreamConsumer(stream, streamConsumerName);
            configProps.setProperty(efoConsumerArn(stream), streamConsumerArn);
        } catch (ExecutionException ex) {
            throw new StreamConsumerRegistrarUtil.FlinkKinesisStreamConsumerRegistrarException(
                    "Error registering stream: " + stream, ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new StreamConsumerRegistrarUtil.FlinkKinesisStreamConsumerRegistrarException(
                    "Error registering stream: " + stream, ex);
        }

    }
}
