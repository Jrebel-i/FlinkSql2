package com.hc.rpc_endpoint_module;

import org.apache.flink.runtime.rpc.RpcGateway;

public interface DemoGateway extends RpcGateway {
    String sayHello(String name);
    String sayGoodbye(String name);
}
