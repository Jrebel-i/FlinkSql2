package com.hc.metric_module;

import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.api.common.serialization.AbstractDeserializationSchema;
import org.apache.flink.metrics.Counter;
import org.apache.flink.util.Collector;

import java.io.IOException;

public abstract  class AbsDeserialization<T> extends AbstractDeserializationSchema<T> {
    private RuntimeContext runtimeContext;
    private String DIRTY_DATA_NAME="dirtyDataNum";
    private String NORMAL_DATA_NAME="normalDataNum";

    protected transient Counter dirtyDataNum;
    protected transient Counter normalDataNum;

    public RuntimeContext getRuntimeContext() {
        return runtimeContext;
    }

    public void setRuntimeContext(RuntimeContext runtimeContext) {
        this.runtimeContext = runtimeContext;
    }

    public void initMetric(){
        dirtyDataNum=runtimeContext.getMetricGroup().counter("dirtyDataNum");
        normalDataNum=runtimeContext.getMetricGroup().counter("NORMAL_DATA_NAME");
    }

}
