package org.apache.flink.streaming.api.functions.sink.filesystem;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeutils.base.array.BytePrimitiveArraySerializer;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.api.java.operators.FlatMapOperator;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.typeutils.TupleTypeInfo;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.core.fs.Path;
import org.apache.flink.core.fs.RecoverableWriter;
import org.apache.flink.core.io.SimpleVersionedSerialization;
import org.apache.flink.core.io.SimpleVersionedSerializer;
import org.apache.flink.formats.compress.CompressWriterFactory;
import org.apache.flink.formats.compress.extractor.DefaultExtractor;
import org.apache.flink.fs.s3.common.writer.S3Recoverable;
import org.apache.flink.fs.s3.common.writer.S3RecoverableSerializer;
import org.apache.flink.kinesis.shaded.com.amazonaws.services.kinesis.model.HashKeyRange;
import org.apache.flink.kinesis.shaded.com.amazonaws.services.kinesis.model.SequenceNumberRange;
import org.apache.flink.kinesis.shaded.com.amazonaws.services.kinesis.model.Shard;
import org.apache.flink.runtime.OperatorIDPair;
import org.apache.flink.runtime.jobgraph.JobGraph;
import org.apache.flink.runtime.jobgraph.JobVertex;
import org.apache.flink.runtime.jobgraph.OperatorID;
import org.apache.flink.runtime.rest.handler.RestHandlerException;
import org.apache.flink.runtime.state.hashmap.HashMapStateBackend;
import org.apache.flink.shaded.netty4.io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.flink.state.api.ExistingSavepoint;
import org.apache.flink.state.api.Savepoint;
import org.apache.flink.streaming.api.functions.sink.filesystem.bucketassigners.SimpleVersionedStringSerializer;
import org.apache.flink.streaming.connectors.kinesis.internals.KinesisDataFetcher;
import org.apache.flink.streaming.connectors.kinesis.model.SequenceNumber;
import org.apache.flink.streaming.connectors.kinesis.model.StreamShardHandle;
import org.apache.flink.streaming.connectors.kinesis.model.StreamShardMetadata;
import org.apache.flink.util.Collector;

import java.io.File;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionException;

import static org.apache.flink.util.StringUtils.hexStringToByte;

public class TestReadState4_aboutKinesisShard {

    public static void main(String[] args) throws Exception {
        int partitions=4;
//        获取作业ck文件
//        aws s3 cp s3://bmg-datalake/garena/data/prd/checkpoint/gruel_client/fb06f8299c99bb3bd0336cfe9f76ca4a/  fb06f8299c99bb3bd0336cfe9f76ca4a/ --recursive
//        获取JobGraph
//        hadoop fs -get  /user/hadoop/.flink/application_1647428447557_0075/application_1647428447557_00757220043586029915571.tmp .
        Path jobGraphFile = Path.fromLocalFile(new File("SqlFlink/savePoint/application_1647428447557_00906922697475515294434.tmp"));
        System.out.println(jobGraphFile.toUri());
        JobGraph jobGraph;
        try (ObjectInputStream objectIn =
                     new ObjectInputStream(
                             jobGraphFile.getFileSystem().open(jobGraphFile))) {
            jobGraph = (JobGraph) objectIn.readObject();
        } catch (Exception e) {
            throw new CompletionException(
                    new RestHandlerException(
                            "Failed to deserialize JobGraph.",
                            HttpResponseStatus.BAD_REQUEST,
                            e));
        }
        //获取到这个作业对于operatorId  (根据这里。我们可以得出结论，vertexID,其实就是chain在一起算子中尾算子的OperatorID)
        System.out.println(jobGraph.getJobID());
        System.out.println("*************************************************************************************");
        for (JobVertex vertex : jobGraph.getVertices()) {
            System.out.println(vertex.getID()+"=====>"+vertex.getName());
            for (OperatorIDPair operatorID : vertex.getOperatorIDs()) {
                System.out.println("该vertex对应的operatorID为："+operatorID.getGeneratedOperatorID());
            }
            System.out.println("*************************************************************************************");
        }

        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        ExistingSavepoint savepoint = Savepoint.load(env,
                    "s3://big-data-warehouse-ods/odsData/checkpoint/webserver/env_type=prd/0440066c46b54ded46a86a059de167db/chk-33661/",
//                "s3a://big-data-warehouse-ods/odsData/checkpoint/server/env_type=prd/f32d540b8ceb77cc3d9eaa0c3ab45cc7/chk-10480",
//                "file:///D:\\DataCompilation\\MyCode\\code\\my\\learn\\FlinkSql2\\SqlFlink\\savePoint\\fb06f8299c99bb3bd0336cfe9f76ca4a\\chk-13370",
                new HashMapStateBackend());
        TypeInformation<Tuple2<StreamShardMetadata, SequenceNumber>> shardsStateTypeInfo =
                new TupleTypeInfo<>(
                        TypeInformation.of(StreamShardMetadata.class),
                        TypeInformation.of(SequenceNumber.class));

        //我们可以通过JobGraph获取到source的operatorID为：6cdc5bb954874d922eaee11a8e7b5dd5  [从后往前获取的]
        OperatorID operatorID = new OperatorID(hexStringToByte("cbc357ccb763df2852fee8c4fc7d55f2"));
        System.out.println(operatorID.getUpperPart()+"====>"+operatorID.getLowerPart());

        DataSet<Tuple2<StreamShardMetadata, SequenceNumber>> sourceState = savepoint.readUnionStateOfOperatorID(
                operatorID,
                "Kinesis-Stream-Shard-State",
                shardsStateTypeInfo
        );

        sourceState.print();

        FlatMapOperator<Tuple2<StreamShardMetadata, SequenceNumber>, String> tuple2StringFlatMapOperator = sourceState.flatMap(new RichFlatMapFunction<Tuple2<StreamShardMetadata, SequenceNumber>, String>() {

            @Override
            public void flatMap(Tuple2<StreamShardMetadata, SequenceNumber> value, Collector<String> out) throws Exception {
                StreamShardMetadata f0 = value.f0;
                SequenceNumber f1 = value.f1;

//                System.out.println(f0.getEndingSequenceNumber()+"==========>"+(f0.getEndingSequenceNumber()==null));
                //需要确保endingSequenceNumber为null(空)，这样的shard才是正在使用的，否则则就是已经结束了的
                if (f0.getEndingSequenceNumber() == null) {
                    Shard shard = new Shard();
                    shard.withShardId(f0.getShardId());
                    shard.withParentShardId(f0.getParentShardId());
                    shard.withAdjacentParentShardId(f0.getAdjacentParentShardId());

                    HashKeyRange hashKeyRange = new HashKeyRange();
                    hashKeyRange.withStartingHashKey(f0.getStartingHashKey());
                    hashKeyRange.withEndingHashKey(f0.getEndingHashKey());
                    shard.withHashKeyRange(hashKeyRange);

                    SequenceNumberRange sequenceNumberRange = new SequenceNumberRange();
                    sequenceNumberRange.withStartingSequenceNumber(
                            f0.getStartingSequenceNumber());
                    sequenceNumberRange.withEndingSequenceNumber(f0.getEndingSequenceNumber());
                    shard.withSequenceNumberRange(sequenceNumberRange);

                    StreamShardHandle handle = new StreamShardHandle(f0.getStreamName(), shard);
                    //默认策略
                    int hashCode = KinesisDataFetcher.DEFAULT_SHARD_ASSIGNER.assign(handle, partitions);
                    int abs = Math.abs(hashCode % partitions);
                    out.collect(f0.getShardId() + '_' + hashCode + "_" + abs);

                    //flink1.15版本引入的策略
//                    int hashCode = new UniformShardAssigner().assign(handle, partitions);
//                    int abs = Math.abs(hashCode % partitions);
//                    out.collect(f0.getShardId() + '_' + hashCode + "_" + abs);
                }

            }
        });

//        tuple2StringFlatMapOperator.print();
        tuple2StringFlatMapOperator.flatMap(new FlatMapFunction<String, String>() {
            Map<String,String> mp=new HashMap<>();
            @Override
            public void flatMap(String value, Collector<String> out) throws Exception {
                String[] s = value.split("_");
                if(mp.containsKey(s[2])){

                }else{
                    mp.put(s[2],"");
                    out.collect(s[2]);
                }
            }
        })
                .print();

        System.out.println("*****************************************************************************************************************************************************");

        operatorID = new OperatorID(hexStringToByte("6b41151dfba2a5f165b47cdbc7b8eaaf"));
        System.out.println(operatorID.getUpperPart()+"====>"+operatorID.getLowerPart());
        DataSource<byte[]> sinkBucketState = savepoint.readListStateOfOperatorID(
                operatorID,
                "bucket-states",
                new TypeHint<byte[]>() {
                }.getTypeInfo(),
                BytePrimitiveArraySerializer.INSTANCE
        );
        sinkBucketState.map(new MapFunction<byte[], String>() {

            @Override
            public String map(byte[] value) throws Exception {
                CompressWriterFactory<String> writerFactory =
                        new CompressWriterFactory<String>(new DefaultExtractor<>()).withHadoopCompression("GzipCodec");
                RecoverableWriter recoverableWriter = FileSystem.get(new Path("s3a://big-data-warehouse-ods/odsData/webserver/env_type=prd/").toUri()).createRecoverableWriter();
//                BulkBucketWriter<String, String> bucketWriter =
//                        new BulkBucketWriter<>(recoverableWriter, writerFactory);

                OutputStreamBasedPartFileWriter.OutputStreamBasedInProgressFileRecoverableSerializer outputStreamBasedInProgressFileRecoverableSerializer =
                        new OutputStreamBasedPartFileWriter.OutputStreamBasedInProgressFileRecoverableSerializer(
                        recoverableWriter.getResumeRecoverableSerializer());

                OutputStreamBasedPartFileWriter.OutputStreamBasedPendingFileRecoverableSerializer outputStreamBasedPendingFileRecoverableSerializer =
                        new OutputStreamBasedPartFileWriter.OutputStreamBasedPendingFileRecoverableSerializer(
                        recoverableWriter.getCommitRecoverableSerializer());
                BucketStateSerializer<String> stateSerializer = new BucketStateSerializer<>(
                        outputStreamBasedInProgressFileRecoverableSerializer,
                        outputStreamBasedPendingFileRecoverableSerializer,
                        SimpleVersionedStringSerializer.INSTANCE
                );
                BucketState<String> bucketState = SimpleVersionedSerialization.readVersionAndDeSerialize(stateSerializer, value);
                System.out.println(bucketState.getBucketId()+"--"+bucketState.getBucketId()+
                        "--"+bucketState.getInProgressFileCreationTime()+
                        "--"+bucketState.getInProgressFileRecoverable()+
                        "--"+bucketState.getPendingFileRecoverablesPerCheckpoint());//s3分段的最小为5mb，因此15.3MB会被划分为4个parts
                bucketState.getPendingFileRecoverablesPerCheckpoint().forEach((x,y)->{
                    System.out.println(x);
                    for (int i = 0; i < y.size(); i++) {
                        OutputStreamBasedPartFileWriter.OutputStreamBasedPendingFileRecoverable pendingFileRecoverable = (OutputStreamBasedPartFileWriter.OutputStreamBasedPendingFileRecoverable)y.get(i);
                        S3Recoverable commitRecoverable = (S3Recoverable)pendingFileRecoverable.getCommitRecoverable();

                        System.out.println(commitRecoverable);
                    }
                });
                return new String(value, "utf-8");
            }
        }).collect();
//                .print();
        System.out.println("*****************************************************************************************************************************************************");
        operatorID = new OperatorID(hexStringToByte("6b41151dfba2a5f165b47cdbc7b8eaaf"));
        System.out.println(operatorID.getUpperPart()+"====>"+operatorID.getLowerPart());
        DataSource<Long> sinkMaxPartCounter = savepoint.readUnionStateOfOperatorID(
                operatorID,
                "max-part-counter",
                new TypeHint<Long>() {
                }.getTypeInfo()
        );
        sinkMaxPartCounter.print();
    }
}
