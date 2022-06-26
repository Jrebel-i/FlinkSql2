package com.hc.rpc_endpoint_module;

import org.apache.flink.runtime.rpc.RpcEndpoint;
import org.apache.flink.runtime.rpc.RpcService;

public class DemoEndpoint extends RpcEndpoint implements DemoGateway {
    public DemoEndpoint(RpcService rpcService) {
        super(rpcService);
    }

    @Override
    public String sayHello(String name) {
        return "Hello," + name + ".";
    }

    @Override
    public String sayGoodbye(String name) {
        return "Goodbye," + name + ".";
    }
}