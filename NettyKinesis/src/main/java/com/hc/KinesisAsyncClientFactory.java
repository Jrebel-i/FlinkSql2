package com.hc;

import org.apache.flink.kinesis.shaded.org.reactivestreams.Subscriber;
import org.apache.flink.kinesis.shaded.org.reactivestreams.Subscription;
import org.apache.flink.kinesis.shaded.software.amazon.awssdk.core.async.SdkPublisher;
import org.apache.flink.kinesis.shaded.software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import org.apache.flink.kinesis.shaded.software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import org.apache.flink.kinesis.shaded.software.amazon.awssdk.regions.Region;
import org.apache.flink.kinesis.shaded.software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import org.apache.flink.kinesis.shaded.software.amazon.awssdk.services.kinesis.KinesisClient;
import org.apache.flink.kinesis.shaded.software.amazon.awssdk.services.kinesis.model.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;


public class KinesisAsyncClientFactory {
    private static final String CONSUMER_ARN = "arn:aws:kinesis:us-east-1:163639519626:stream/BlockmanGoBetaEventStream/consumer/cathch:1661400761";

    public static void main(String[] args) {
        Region region = Region.US_EAST_1;
        SdkAsyncHttpClient httpClient = NettyNioAsyncHttpClient.builder()  //https://github.com/aws/aws-sdk-java-v2/issues/446
                .maxConcurrency(10) //https://docs.aws.amazon.com/zh_cn/sdk-for-java/latest/developer-guide/http-configuration-netty.html
                .maxPendingConnectionAcquires(1000)
                .build();
        KinesisAsyncClient client = KinesisAsyncClient.builder()
                .region(region)
                .httpClient(httpClient)
                .build();

        SubscribeToShardRequest request = SubscribeToShardRequest.builder()
                .consumerARN(CONSUMER_ARN)
                .shardId("arn:aws:kinesis:us-east-1:163639519626:stream/BlockmanGoBetaEventStream")
                .startingPosition(s -> s.type(ShardIteratorType.LATEST)).build();

        SubscribeToShardResponseHandler responseHandler = SubscribeToShardResponseHandler
                .builder()
                .onError(t -> System.err.println("Error during stream - " + t.getMessage()))
                .onComplete(() -> System.out.println("All records stream successfully"))
                // Must supply some type of subscriber
                .subscriber(e -> System.out.println("Received event - " + e))
//                .subscriber(MySubscriber::new)
                .build();
        client.subscribeToShard(request, responseHandler); //订阅碎片
        client.close();
    }


    //自定义响应处理程序 实施 SubscribeToShardResponseHandler 接口。
    private static CompletableFuture<Void> responseHandlerBuilderClassic(KinesisAsyncClient client, SubscribeToShardRequest request) {
        SubscribeToShardResponseHandler responseHandler = new SubscribeToShardResponseHandler() {

            @Override
            public void responseReceived(SubscribeToShardResponse response) {
                System.out.println("Receieved initial response");
            }

            @Override
            public void onEventStream(SdkPublisher<SubscribeToShardEventStream> publisher) {
                publisher
                        // Filter to only SubscribeToShardEvents
                        .filter(SubscribeToShardEvent.class)
                        // Flat map into a publisher of just records
                        .flatMapIterable(SubscribeToShardEvent::records)
                        // Limit to 1000 total records
                        .limit(1000)
                        // Batch records into lists of 25
                        .buffer(25)
                        // Print out each record batch
                        .subscribe(batch -> System.out.println("Record Batch - " + batch));
            }

            @Override
            public void complete() {
                System.out.println("All records stream successfully");
            }

            @Override
            public void exceptionOccurred(Throwable throwable) {
                System.err.println("Error during stream - " + throwable.getMessage());
            }
        };
        return client.subscribeToShard(request, responseHandler);
    }
    //访客接口
    private static CompletableFuture<Void> responseHandlerBuilderVisitorBuilder(KinesisAsyncClient client, SubscribeToShardRequest request) {
        SubscribeToShardResponseHandler.Visitor visitor = SubscribeToShardResponseHandler.Visitor
                .builder()
                .onSubscribeToShardEvent(e -> System.out.println("Received subscribe to shard event " + e))
                .build();
        SubscribeToShardResponseHandler responseHandler = SubscribeToShardResponseHandler
                .builder()
                .onError(t -> System.err.println("Error during stream - " + t.getMessage()))
                .subscriber(visitor)
                .build();
        return client.subscribeToShard(request, responseHandler);
    }
    //自定义订阅者  自定义订阅者以订阅流
    private static class MySubscriber implements Subscriber<SubscribeToShardEventStream> {

        private Subscription subscription;
        private AtomicInteger eventCount = new AtomicInteger(0);

        @Override
        public void onSubscribe(Subscription subscription) {
            this.subscription = subscription;
            this.subscription.request(1);
        }

        @Override
        public void onNext(SubscribeToShardEventStream shardSubscriptionEventStream) {
            System.out.println("Received event " + shardSubscriptionEventStream);
            if (eventCount.incrementAndGet() >= 100) {
                // You can cancel the subscription at any time if you wish to stop receiving events.
                subscription.cancel();
            }
            subscription.request(1);
        }

        @Override
        public void onError(Throwable throwable) {
            System.err.println("Error occurred while stream - " + throwable.getMessage());
        }

        @Override
        public void onComplete() {
            System.out.println("Finished streaming all events");
        }
    }
}
