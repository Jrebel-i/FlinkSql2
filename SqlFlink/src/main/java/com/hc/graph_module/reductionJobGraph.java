package com.hc.graph_module;

import org.apache.flink.runtime.jobgraph.JobGraph;
import org.apache.flink.runtime.rest.handler.RestHandlerException;
import org.apache.flink.shaded.netty4.io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.flink.core.fs.Path;

import java.io.File;
import java.io.ObjectInputStream;
import java.util.concurrent.CompletionException;

public class reductionJobGraph {
    public static void main(String[] args) {
        Path jobGraphFile = Path.fromLocalFile(new File("SqlFlink/file/hdfs/application_1647428447557_00757220043586029915571.tmp"));
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
        System.out.println(jobGraph.getCheckpointingSettings());
        System.out.println(jobGraph.getSlotSharingGroups());
        System.out.println(jobGraph.getVertices());
        System.out.println(jobGraph.getJobID());
    }
}
