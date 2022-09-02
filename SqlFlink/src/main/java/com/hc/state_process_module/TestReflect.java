package com.hc.state_process_module;

import org.apache.flink.annotation.Internal;
import org.apache.flink.api.common.serialization.SimpleStringEncoder;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.core.fs.Path;
import org.apache.flink.core.io.SimpleVersionedSerializer;
import org.apache.flink.streaming.api.functions.sink.filesystem.*;
import org.apache.flink.streaming.api.functions.sink.filesystem.bucketassigners.SimpleVersionedStringSerializer;
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.DefaultRollingPolicy;


import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 测试反射
 */
class DateAssigner<IN> implements BucketAssigner<IN,String>{
    public DateAssigner() {
    }

    @Override
    public String getBucketId(IN in, Context context) {
        String year = new SimpleDateFormat("yyyy").format(new Date());
        String month = new SimpleDateFormat("MM").format(new Date());
        String day = new SimpleDateFormat("dd").format(new Date());
        String hour = new SimpleDateFormat("HH").format(new Date());

        return String.format("year=%s/month=%s/day=%s/hour=%s", year, month, day, hour);
    }

    @Override
    public SimpleVersionedSerializer<String> getSerializer() {
        return SimpleVersionedStringSerializer.INSTANCE;
    }
}

class RecordBucketLifeCycleListener
        implements BucketLifeCycleListener<String, String> {
    public enum EventType {
        CREATED,
        INACTIVE
    }

    private List<Tuple2<EventType, String>> events = new ArrayList<>();

    @Override
    public void bucketCreated(Bucket<String, String> bucket) {
        events.add(new Tuple2<>(EventType.CREATED, bucket.getBucketId()));
    }

    @Override
    public void bucketInactive(Bucket<String, String> bucket) {
        events.add(new Tuple2<>(EventType.INACTIVE, bucket.getBucketId()));
    }

    public List<Tuple2<EventType, String>> getEvents() {
        return events;
    }
}
@Internal
public class TestReflect {
    Map<String, Bucket<String, String>> activeBuckets=new HashMap<>();
    public void getBucket(){
        try {
            final String child = "year=2022/month=08/day=10/hour=20".toString();
            if ("".equals(child)) {
                Path bucketPath =  new Path("D:\\CloudMusic");
            }
            Path bucketPath = new Path("D:\\CloudMusic", child);

            System.out.println(bucketPath.getPath());
            Bucket<String,String> bucket = null;

                bucket = new DefaultBucketFactoryImpl<>().getNewBucket(
                        0,
                        "year=2022/month=08/day=10/hour=20",
                        bucketPath,
                        100,
                        new RowWiseBucketWriter<>(
                                FileSystem.get(new Path("D:\\CloudMusic").toUri()).createRecoverableWriter(),
                                new SimpleStringEncoder<>()),
                        DefaultRollingPolicy.builder().build(),
                        new FileLifeCycleListener() {
                            @Override
                            public void onPartFileOpened(Object o, Path newPath) {

                            }
                        },
                        OutputFileConfig.builder().build()
                );

            System.out.println(bucket.getBucketId()+"创建成功");
            activeBuckets.put("year=2022/month=08/day=10/hour=20",bucket);


            Iterator<Map.Entry<String, Bucket<String, String>>> activeBucketIt = activeBuckets.entrySet().iterator();
            while (activeBucketIt.hasNext()) {// 对 所有活跃 bucket 调用 bucket.onSuccessfulCompletionOfCheckpoint
                final Bucket<String, String> activeBucket = activeBucketIt.next().getValue();
                Method assembleNewPartPath = activeBucket.getClass().getDeclaredMethod("assembleNewPartPath");
                assembleNewPartPath.setAccessible(true);
                Path path = (Path) assembleNewPartPath.invoke(activeBucket);
                System.out.println(path.getPath());

                Method isActive = activeBucket.getClass().getDeclaredMethod("isActive");
                isActive.setAccessible(true);
                boolean is = (boolean) isActive.invoke(activeBucket);
                System.out.println(is);

                Method write = activeBucket.getClass().getDeclaredMethod("write", OutputStreamWriter.class,Long.class);
                write.setAccessible(true);
                write.invoke(activeBucket,"xxxxxx",System.currentTimeMillis());
                System.out.println("=====");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        new TestReflect().getBucket();
//            Buckets<Object, String> buckets = new Buckets<>(
//                    new Path("D:\\reps\\S3Sink4_Transform_Gruel\\outputdata"),
//                    new DateAssigner(),//new DateTimeBucketAssigner<>(),//
//                    new DefaultBucketFactoryImpl<>(),
//                    new RowWiseBucketWriter<>(
//                            FileSystem.get(new Path("D:\\reps\\S3Sink4_Transform_Gruel\\outputdata").toUri()).createRecoverableWriter(),
//                            new SimpleStringEncoder<>()),
//                    DefaultRollingPolicy.builder().build(),
//                    0,
//                    OutputFileConfig.builder().build()
//            );
//            if (buckets == null) {

//            }


    }
}

