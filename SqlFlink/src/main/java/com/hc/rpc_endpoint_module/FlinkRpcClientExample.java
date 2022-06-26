package com.hc.rpc_endpoint_module;

import akka.actor.ActorSystem;
import org.apache.flink.runtime.akka.AkkaUtils;
import org.apache.flink.runtime.rpc.akka.AkkaRpcService;
import org.apache.flink.runtime.rpc.akka.AkkaRpcServiceConfiguration;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class FlinkRpcClientExample {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 1. 创建RPC服务
        ActorSystem defaultActorSystem = AkkaUtils.createDefaultActorSystem();
        AkkaRpcService akkaRpcService = new AkkaRpcService(defaultActorSystem,
                AkkaRpcServiceConfiguration.defaultConfiguration());
        // 2. 连接远程RPC服务，注意：连接地址是服务端程序打印的地址
        System.out.println(akkaRpcService.getPort());
        CompletableFuture<DemoGateway> gatewayFuture = akkaRpcService
                .connect("akka.tcp://flink@192.168.1.1:54614/user/rpc/efd8bd7b-9702-4736-92fb-acfae2e34e10", DemoGateway.class);
        // 3. 远程调用
        DemoGateway gateway = gatewayFuture.get();
        System.out.println(gateway.sayHello("flink-rpc"));
        System.out.println(gateway.sayGoodbye("flink-rpc"));
    }
}