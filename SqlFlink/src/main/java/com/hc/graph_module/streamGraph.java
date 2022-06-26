package com.hc.graph_module;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.dag.Pipeline;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.MultipleParameterTool;
import org.apache.flink.client.deployment.executors.LocalExecutor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.jobgraph.JobGraph;
import org.apache.flink.runtime.jobgraph.JobVertex;
import org.apache.flink.runtime.jobgraph.jsonplan.JsonPlanGenerator;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.graph.StreamGraph;
import org.apache.flink.util.Collector;
import org.apache.flink.util.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class streamGraph {

    private static final Logger LOG = LoggerFactory.getLogger(streamGraph.class);
    public static void main(String[] args) throws Exception {
        // Checking input parameters
        final MultipleParameterTool params = MultipleParameterTool.fromArgs(args);

        // set up the execution environment
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // make parameters available in the web interface
        env.getConfig().setGlobalJobParameters(params);

        // get input data
        DataStream<String> text = null;
        if (params.has("input")) {
            // union all the inputs from text files
            for (String input : params.getMultiParameterRequired("input")) {
                if (text == null) {
                    text = env.readTextFile(input);
                } else {
                    text = text.union(env.readTextFile(input));
                }
            }
            Preconditions.checkNotNull(text, "Input DataStream should not be null.");
        } else {
            System.out.println("Executing WordCount example with default input data set.");
            System.out.println("Use --input to specify file input.");
            // get default test text data
            text = env.fromElements(WordCountData.WORDS);
        }

        DataStream<Tuple2<String, Integer>> counts =
                // split up the lines in pairs (2-tuples) containing: (word,1)
                text.flatMap(new Tokenizer())
                        // group by the tuple field "0" and sum up tuple field "1"
                        .keyBy(value -> value.f0)
                        .sum(1);

        // emit result
        if (params.has("output")) {
            counts.writeAsText(params.get("output"));
        } else {
            System.out.println("Printing result to stdout. Use --output to specify output path.");
            counts.print();
        }
        StreamGraph streamGraph = env.getStreamGraph("Streaming WordCount");
        System.out.printf("streamGraph数据：%s",streamGraph.getStreamingPlanAsJSON());

        Field field = StreamExecutionEnvironment.class.getDeclaredField("configuration");
        field.setAccessible(true);
        Configuration configuration = (Configuration)field.get(env);
        System.out.println("配置信息："+configuration);

        Configuration effectiveConfig = new Configuration();
        effectiveConfig.addAll(configuration);

        LocalExecutor localExecutor = LocalExecutor.create(configuration);
        Method privateStringMethod = LocalExecutor.class.getDeclaredMethod("getJobGraph", Pipeline.class,Configuration.class);
        privateStringMethod.setAccessible(true);
        JobGraph jobGraph = (JobGraph)privateStringMethod.invoke(localExecutor, streamGraph, effectiveConfig);

        if (jobGraph != null) {
            File tmpJobGraphFile = null;
            try {
                tmpJobGraphFile = File.createTempFile(jobGraph.getJobID().toString(), null);
                try (FileOutputStream output = new FileOutputStream(tmpJobGraphFile);
                     ObjectOutputStream obOutput = new ObjectOutputStream(output)) {
                    obOutput.writeObject(jobGraph);
                }
                System.out.printf("jobGraph临时文件路径：%s",tmpJobGraphFile.getPath()+"\r\n");
                System.out.printf("jobGraph字符串：%s", JsonPlanGenerator.generatePlan(jobGraph));
                // topologically sort the job vertices and attach the graph to the existing one
                List<JobVertex> sortedTopology = jobGraph.getVerticesSortedTopologicallyFromSources();//按照拓扑顺序获取JobGraph的所有顶点JobVertex
                for (JobVertex vertex : sortedTopology) {
                    System.out.println(vertex.getName());
                }

            } catch (Exception e) {
                LOG.warn("Add job graph to local resource fail.");
                throw e;
            }
        }
//        // execute program
//        System.out.println("ExecutionGraph字符串："+env.getExecutionPlan());
//        env.execute("Streaming WordCount");
    }
    public static final class Tokenizer
            implements FlatMapFunction<String, Tuple2<String, Integer>> {

        @Override
        public void flatMap(String value, Collector<Tuple2<String, Integer>> out) {
            // normalize and split the line
            String[] tokens = value.toLowerCase().split("\\W+");

            // emit the pairs
            for (String token : tokens) {
                if (token.length() > 0) {
                    out.collect(new Tuple2<>(token, 1));
                }
            }
        }
    }
}
