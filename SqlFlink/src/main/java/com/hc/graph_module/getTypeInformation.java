package com.hc.graph_module;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.Utils;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;
import org.apache.flink.util.MathUtils;

public class getTypeInformation {
    public static void main(String[] args) throws Exception {
        // the host and the port to connect to
        final String hostname;
        final int port;
        try {
//            final ParameterTool params = ParameterTool.fromArgs(args);
//            hostname = params.has("hostname") ? params.get("hostname") : "localhost";
//            port = params.getInt("port");
            hostname = "localhost";
            port = 9999;
        } catch (Exception e) {
            System.err.println(
                    "No port specified. Please run 'SocketWindowWordCount "
                    + "--hostname <hostname> --port <port>', where hostname (localhost by default) "
                    + "and port is the address of the text server");
            System.err.println(
                    "To start a simple text server, run 'netcat -l <port>' and "
                            + "type the input text into the command line");
            return;
        }

        // get the execution environment
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // get input data by connecting to the socket
        DataStream<String> text = env.socketTextStream(hostname, port, "\n");

        //看KeyGroupStreamPartitioner是如何进行key的分区的  查看selectChannel()方法
        // parse the data, group it, window it, and aggregate the counts
        DataStream<WordWithCount> windowCounts =
                text.flatMap(
                        new FlatMapFunction<String, WordWithCount>() {
                            @Override
                            public void flatMap(
                                    String value, Collector<WordWithCount> out) {
                                for (String word : value.split("\\s")) {
                                    out.collect(new WordWithCount(word, 1L));
                                }
                            }
                        })
                        .keyBy(value -> value.word)
                        .window(TumblingProcessingTimeWindows.of(Time.seconds(5)))
                        .reduce(
                                new ReduceFunction<WordWithCount>() {
                                    @Override
                                    public WordWithCount reduce(WordWithCount a, WordWithCount b) {
                                        return new WordWithCount(a.word, a.count + b.count);
                                    }
                                });

        // print the results with a single thread, rather than in parallel
        windowCounts.print().setParallelism(1);


        //KeyGroupStreamPartitioner计算逻辑
        int maxParallelism = 128; //最大并行度
        int parallelism =3; //设置的并行度
        String key ="test22";
        int keyGroupId = MathUtils.murmurHash(key.hashCode()) % maxParallelism; //对key的hash值进行取余
        System.out.println(keyGroupId);
        int partitionId = keyGroupId * parallelism / maxParallelism;
        System.out.println("最终的分区ID为："+partitionId);

        env.execute("Socket Window WordCount");
    }

    // ------------------------------------------------------------------------

    /** Data type for words with count. */
    public static class WordWithCount {

        public String word;
        public long count;

        public WordWithCount() {}

        public WordWithCount(String word, long count) {
            this.word = word;
            this.count = count;
        }

        @Override
        public String toString() {
            return word + " : " + count;
        }
    }
}
