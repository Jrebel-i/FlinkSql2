//package com.hc.state_process_module;
//
//import org.apache.flink.api.common.typeinfo.TypeInformation;
//import org.apache.flink.api.java.tuple.Tuple2;
//import org.apache.flink.api.java.typeutils.TupleTypeInfo;
//import org.apache.flink.core.fs.Path;
//import org.apache.flink.runtime.OperatorIDPair;
//import org.apache.flink.runtime.jobgraph.JobGraph;
//import org.apache.flink.runtime.jobgraph.JobVertex;
//import org.apache.flink.runtime.jobgraph.OperatorID;
//import org.apache.flink.runtime.rest.handler.RestHandlerException;
//import org.apache.flink.runtime.state.hashmap.HashMapStateBackend;
//import org.apache.flink.shaded.netty4.io.netty.handler.codec.http.HttpResponseStatus;
//import org.apache.flink.state.api.SavepointReader;
//import org.apache.flink.streaming.api.datastream.DataStream;
//import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
//import org.apache.flink.streaming.connectors.kinesis.FlinkKinesisConsumer;
//import org.apache.flink.streaming.connectors.kinesis.model.SequenceNumber;
//import org.apache.flink.streaming.connectors.kinesis.model.StreamShardMetadata;
//
//import java.io.File;
//import java.io.IOException;
//import java.io.ObjectInputStream;
//import java.nio.charset.Charset;
//import java.util.concurrent.CompletionException;
//
//import static org.apache.flink.util.StringUtils.byteToHexString;
//import static org.apache.flink.util.StringUtils.hexStringToByte;
//
//public class TestReadState {
//    public static void main(String[] args) throws IOException {
////        获取作业ck文件
////        aws s3 cp s3://bmg-datalake/garena/data/prd/checkpoint/gruel_client/fb06f8299c99bb3bd0336cfe9f76ca4a/  fb06f8299c99bb3bd0336cfe9f76ca4a/ --recursive
////        获取JobGraph
////        hadoop fs -get  /user/hadoop/.flink/application_1647428447557_0075/application_1647428447557_00757220043586029915571.tmp .
//        Path jobGraphFile = Path.fromLocalFile(new File("SqlFlink/savePoint/application_1647428447557_00757220043586029915571.tmp"));
//        System.out.println(jobGraphFile.toUri());
//        JobGraph jobGraph;
//        try (ObjectInputStream objectIn =
//                     new ObjectInputStream(
//                             jobGraphFile.getFileSystem().open(jobGraphFile))) {
//            jobGraph = (JobGraph) objectIn.readObject();
//        } catch (Exception e) {
//            throw new CompletionException(
//                    new RestHandlerException(
//                            "Failed to deserialize JobGraph.",
//                            HttpResponseStatus.BAD_REQUEST,
//                            e));
//        }
//        //获取到这个作业对于operatorId  (根据这里。我们可以得出结论，vertexID,其实就是chain在一起算子中尾算子的OperatorID)
//        System.out.println(jobGraph.getJobID());
//        System.out.println("*************************************************************************************");
//        for (JobVertex vertex : jobGraph.getVertices()) {
//            System.out.println(vertex.getID()+"=====>"+vertex.getName());
//            for (OperatorIDPair operatorID : vertex.getOperatorIDs()) {
//                System.out.println("该vertex对应的operatorID为："+operatorID.getGeneratedOperatorID());
//            }
//            System.out.println("*************************************************************************************");
//        }
//
//        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
//        SavepointReader savepoint = SavepointReader.read(env, "file:///D:\\DataCompilation\\MyCode\\code\\my\\learn\\FlinkSql2\\SqlFlink\\savePoint\\fb06f8299c99bb3bd0336cfe9f76ca4a\\chk-13370", new HashMapStateBackend());
//        TypeInformation<Tuple2<StreamShardMetadata, SequenceNumber>> shardsStateTypeInfo =
//                new TupleTypeInfo<>(
//                        TypeInformation.of(StreamShardMetadata.class),
//                        TypeInformation.of(SequenceNumber.class));
//
//        DataStream<Tuple2<StreamShardMetadata, SequenceNumber>> tuple2DataStream = savepoint.readUnionState(
//                "8cfbf24d572af11027afc9b517e44624",
//                "Kinesis-Stream-Shard-State",
//                shardsStateTypeInfo
//        );
//
//        tuple2DataStream.print();
//
//
////        HashFunction hashFunction = Hashing.murmur3_128(0);
////        Hasher hasher = hashFunction.newHasher();
////        hasher.putString("8cfbf24d572af11027afc9b517e44624", Charset.forName("UTF-8"));
//////        System.out.println("8cfbf24d572af11027afc9b517e44624".length());
////        byte[] bytes = hasher.hash().asBytes();
////
////
////        System.out.println(hexStringToByte("8cfbf24d572af11027afc9b517e44624").length);
////        System.out.println(new OperatorID(hexStringToByte("8cfbf24d572af11027afc9b517e44624")));
////        Hasher hasher1 = hasher.putBytes(hexStringToByte("8cfbf24d572af11027afc9b517e44624"));
//
//    }
//}
