package com.hc.state_process_module;

import com.amazonaws.services.kinesisanalytics.TableProcess;
import com.ververica.cdc.connectors.mysql.source.split.MySqlBinlogSplit;
import com.ververica.cdc.connectors.mysql.source.split.MySqlSplit;
import com.ververica.cdc.connectors.mysql.source.split.MySqlSplitSerializer;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.common.typeutils.base.array.BytePrimitiveArraySerializer;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.typeutils.TupleTypeInfo;
import org.apache.flink.core.fs.Path;
import org.apache.flink.core.io.SimpleVersionedSerialization;
import org.apache.flink.runtime.OperatorIDPair;
import org.apache.flink.runtime.jobgraph.JobGraph;
import org.apache.flink.runtime.jobgraph.JobVertex;
import org.apache.flink.runtime.jobgraph.OperatorID;
import org.apache.flink.runtime.rest.handler.RestHandlerException;
import org.apache.flink.runtime.state.hashmap.HashMapStateBackend;
import org.apache.flink.shaded.netty4.io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.flink.state.api.ExistingSavepoint;
import org.apache.flink.state.api.Savepoint;
import org.apache.flink.streaming.api.operators.util.SimpleVersionedListState;
import org.apache.flink.streaming.connectors.kinesis.model.SequenceNumber;
import org.apache.flink.streaming.connectors.kinesis.model.StreamShardMetadata;
import org.apache.flink.table.runtime.typeutils.StringDataTypeInfo;

import java.io.File;
import java.io.ObjectInputStream;
import java.util.concurrent.CompletionException;

import static org.apache.flink.util.StringUtils.byteToHexString;
import static org.apache.flink.util.StringUtils.hexStringToByte;

public class TestReadState2 {
    public static void main(String[] args) throws Exception {
//        获取作业ck文件
//        aws s3 cp s3://bmg-datalake/garena/data/prd/checkpoint/gruel_client/fb06f8299c99bb3bd0336cfe9f76ca4a/  fb06f8299c99bb3bd0336cfe9f76ca4a/ --recursive
//        获取JobGraph
//        hadoop fs -get  /user/hadoop/.flink/application_1647428447557_0075/application_1647428447557_00757220043586029915571.tmp .
        Path jobGraphFile = Path.fromLocalFile(new File("SqlFlink/savePoint/application_1647428447557_00757220043586029915571.tmp"));
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
        ExistingSavepoint savepoint = Savepoint.load(env,
//                "s3a://bmg-datalake/garena/data/prd/checkpoint/gruel_client/fb06f8299c99bb3bd0336cfe9f76ca4a/chk-13422/",
                "file:///D:\\DataCompilation\\MyCode\\code\\my\\learn\\FlinkSql2\\SqlFlink\\savePoint\\fb06f8299c99bb3bd0336cfe9f76ca4a\\chk-13370",
                new HashMapStateBackend());
        TypeInformation<Tuple2<StreamShardMetadata, SequenceNumber>> shardsStateTypeInfo =
                new TupleTypeInfo<>(
                        TypeInformation.of(StreamShardMetadata.class),
                        TypeInformation.of(SequenceNumber.class));

//        DataSet<Tuple2<StreamShardMetadata, SequenceNumber>> tuple2DataStream = savepoint.readUnionState(
//                "6cdc5bb954874d922eaee11a8e7b5dd5",
//                "Kinesis-Stream-Shard-State",
//                shardsStateTypeInfo
//        );

        //我们可以通过JobGraph获取到source的operatorID为：6cdc5bb954874d922eaee11a8e7b5dd5  [从后往前获取的]
        OperatorID operatorID = new OperatorID(hexStringToByte("6cdc5bb954874d922eaee11a8e7b5dd5"));
        System.out.println(operatorID.getUpperPart()+"====>"+operatorID.getLowerPart());

        DataSet<Tuple2<StreamShardMetadata, SequenceNumber>> sourceState = savepoint.readUnionStateOfOperatorID(
                operatorID,
                "Kinesis-Stream-Shard-State",
                shardsStateTypeInfo
        );

//        sourceState.print();
        System.out.println("*****************************************************************************************************************************************************");
//*****************************************************************************************************************************************************

//        OperatorID operatorID2 = new OperatorID(hexStringToByte("4150b807e25f98bebfeb73f2fab67d53"));
//        System.out.println(operatorID2.getUpperPart()+"====>"+operatorID2.getLowerPart());
//
//        DataSource<Tuple2<String, TableProcess>> broadState = savepoint.readBroadcastStateOfOperatorID(
//                operatorID2,
//                "tableProcessState",
//                TypeInformation.of(String.class),
//                TypeInformation.of(TableProcess.class)
//        );

//        broadState.print();
        System.out.println("*****************************************************************************************************************************************************");
        //*****************************************************************************************************************************************************
        //flink cdc 调用的是SourceOperator  状态名称为SourceReaderState，类型为ListStateDescriptor

        OperatorID operatorID3 = new OperatorID(hexStringToByte("bc764cd8ddf7a0cff126f51c16239658"));
        System.out.println(operatorID3.getUpperPart()+"====>"+operatorID3.getLowerPart());

        DataSource<byte[]> cdcState = savepoint.readListStateOfOperatorID(
                operatorID3,
                "SourceReaderState",
                new TypeHint<byte[]>() {
                }.getTypeInfo(),
                BytePrimitiveArraySerializer.INSTANCE
        );

        cdcState.map(new MapFunction<byte[], String>() {
            @Override
            public String map(byte[] value) throws Exception {
                System.out.println(byteToHexString(value));
                MySqlSplit mySqlSplit = SimpleVersionedSerialization.readVersionAndDeSerialize(MySqlSplitSerializer.INSTANCE, value);
                System.out.println(mySqlSplit.splitId());
                mySqlSplit.getTableSchemas().forEach((x,y)->{
                    System.out.println("对应数据库及表："+x);
                    System.out.println("字段及类型："+y.getTable());
                    System.out.println("语句类型："+y.getType());
                });

                System.out.println("是否是SnapshotSplit："+mySqlSplit.isSnapshotSplit());
                if(mySqlSplit.isSnapshotSplit()){
                    System.out.println(mySqlSplit.asSnapshotSplit().toString());
                }
                System.out.println("是否是BinlogSplit："+mySqlSplit.isBinlogSplit());
                if(mySqlSplit.isBinlogSplit()){
                    /**
                     * MySqlBinlogSplit{
                     * splitId='binlog-split',
                     * offset={
                     * 	transaction_id=null,
                     * 	ts_sec=1656123405,
                     * 	file=mysql-bin-changelog.000005,
                     * 	pos=109564438,
                     * 	row=1,
                     * 	server_id=1048764807,
                     * 	event=2
                     * },
                     * endOffset={
                     * ts_sec=0,
                     * file=,
                     * pos=-9223372036854775808,
                     * row=0,
                     * event=0
                     * }}
                     */
                    MySqlBinlogSplit mySqlBinlogSplit = mySqlSplit.asBinlogSplit();
                    System.out.println(mySqlBinlogSplit);
                    System.out.println("startOffset文件为：："+mySqlBinlogSplit.getStartingOffset().getFilename()+
                            "，以及pos为："+mySqlBinlogSplit.getStartingOffset().getPosition());
                }


                return byteToHexString(value);
            }
        }).collect();

    }
}
