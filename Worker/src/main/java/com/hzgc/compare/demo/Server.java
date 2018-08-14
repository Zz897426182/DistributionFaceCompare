package com.hzgc.compare.demo;

import com.hzgc.compare.rpc.server.RpcServer;
import com.hzgc.compare.rpc.server.zk.ServiceRegistry;

public class Server {
    public static void main(String[] args) {
        ServiceRegistry registry = new ServiceRegistry("172.18.18.100:2181,172.18.18.101:2181,172.18.18.102:2181");
        RpcServer rpcServer = new RpcServer("localhost", 8889, registry);
        rpcServer.start();
    }
}
