package com.hzgc.compare.rpc.util;

public interface ProperConstant {
    String curatorBaseSleepTime = "rpc.curator.baseSleepTime";
    String curatorMaxRetries = "rpc.curator.maxRetries";
    String zkSessionTime = "rpc.zk.session.time";
    String zkConnectTime = "rpc.zk.connect.time";
    String rpcServerThreadPoolSize = "rpc.server.threadPool.size";
    String rpcServerThreadPoolMaxSize = "rpc.server.threadPool.maxSize";
    String rpcServerThreadPoolKeepAliveTime = "rpc.server.threadPool.aliveTime";
    String rpcServerThreadPollQueueSize = "rpc.server.threadPool.queueSize";
    String rpcClientResponseTimeThreshold = "rpc.client.responseTime.threshold";
    String rpcConnectManagerConnectTimeout = "rpc.client.connectManager.connectTimeOut";
}
