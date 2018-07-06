package com.hzgc.compare.worker;

import com.hzgc.compare.rpc.server.connect.RpcServer;
import com.hzgc.compare.rpc.server.connect.ServiceRegistry;

public class Server {
    public static void main(String[] args) {
        ServiceRegistry registry = new ServiceRegistry("172.18.18.105");
        RpcServer rpcServer = new RpcServer("172.18.18.146", 4085, registry);
        rpcServer.start();
    }
}
