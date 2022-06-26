package com.hc.rpc_endpoint_module;

import akka.actor.ActorSystem;
import org.apache.flink.runtime.akka.AkkaUtils;
import org.apache.flink.runtime.highavailability.HighAvailabilityServices;
import org.apache.flink.runtime.jobmaster.JobMasterGateway;
import org.apache.flink.runtime.jobmaster.ResourceManagerAddress;
import org.apache.flink.runtime.resourcemanager.ResourceManagerGateway;
import org.apache.flink.runtime.resourcemanager.ResourceManagerId;
import org.apache.flink.runtime.rpc.FencedRpcGateway;
import org.apache.flink.runtime.rpc.akka.AkkaRpcService;
import org.apache.flink.runtime.rpc.akka.AkkaRpcServiceConfiguration;
import org.apache.flink.runtime.taskexecutor.TaskExecutorGateway;
import org.apache.flink.runtime.taskexecutor.exceptions.TaskManagerException;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class FlinkRpcClientWithResource {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 1. 创建RPC服务
        ActorSystem defaultActorSystem = AkkaUtils.createDefaultActorSystem();
        AkkaRpcService akkaRpcService = new AkkaRpcService(defaultActorSystem,
                AkkaRpcServiceConfiguration.defaultConfiguration());
        // 2. 连接远程RPC服务，注意：连接地址是服务端程序打印的地址
//        CompletableFuture<ResourceManagerGateway> gatewayFuture = akkaRpcService
//                //akka.tcp://flink@192.168.1.1:63187/user/rpc/47d9d1da-7078-4b14-a735-6723ae60920e
//                .connect("akka.tcp://flink@3.231.206.202:6123/user/rpc/resourcemanager_0", ResourceManagerGateway.class);
        // 3. 远程调用
//        ResourceManagerGateway gateway = gatewayFuture.get();


        UUID leaderId=HighAvailabilityServices.DEFAULT_LEADER_ID;
        System.out.println(leaderId);
        ResourceManagerId managerId = ResourceManagerId.fromUuidOrNull(leaderId);
        final CompletableFuture<FencedRpcGateway> rpcGatewayFuture;
        rpcGatewayFuture=(CompletableFuture<FencedRpcGateway>)akkaRpcService.connect(
                "akka.tcp://flink@ip-172-31-65-180.ec2.internal:44731/user/rpc/resourcemanager_*",
                managerId,
                FencedRpcGateway.class);
        CompletableFuture<Void> rpcGatewayAcceptFuture =
                rpcGatewayFuture.thenAcceptAsync(
                        (FencedRpcGateway rpcGateway) -> {
                            System.out.println("Resolved {} address, beginning registration");
                        });


    }
}