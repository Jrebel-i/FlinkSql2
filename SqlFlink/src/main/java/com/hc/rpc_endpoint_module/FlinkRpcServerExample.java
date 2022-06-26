package com.hc.rpc_endpoint_module;

import akka.actor.ActorSystem;
import org.apache.flink.runtime.akka.AkkaUtils;
import org.apache.flink.runtime.resourcemanager.ResourceManager;
import org.apache.flink.runtime.rpc.akka.AkkaRpcService;
import org.apache.flink.runtime.rpc.akka.AkkaRpcServiceConfiguration;

public class FlinkRpcServerExample {
    public static void main(String[] args) {
        // 1. 创建RPC服务
        ActorSystem defaultActorSystem = AkkaUtils.createDefaultActorSystem();
        AkkaRpcService akkaRpcService = new AkkaRpcService(defaultActorSystem,
                AkkaRpcServiceConfiguration.defaultConfiguration());

        // 2. 创建RpcEndpoint实例
        //创建ActorSystem为：akka://flink
        //通过ActorSystem创建AkkaRpcService
        //将RpcService设置到RpcEndpoint中
        //以ResourceManager为例子，其继承了FencedRpcEndpoint和ResourceManagerGateway，
        //并设置endpointId为resourcemanager字符串拼接上随机数字,RpcService的地址拼接上
        //因此可以直接ResourceManager调用getAddress获取到ResourceManager对应的地址

        DemoEndpoint endpoint = new DemoEndpoint(akkaRpcService);
        System.out.println("Address: "+endpoint.getAddress());
        DemoEndpoint endpoint2 = new DemoEndpoint(akkaRpcService);
        System.out.println("Address: "+endpoint2.getAddress());
        // 3. 启动Endpoint
        endpoint.start();
    }
}
