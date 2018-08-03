package com.hzgc.compare.worker;

import com.hzgc.compare.rpc.server.RpcServer;
import com.hzgc.compare.rpc.server.zk.ServiceRegistry;
import com.hzgc.compare.worker.conf.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RPCRegistry implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger(RPCRegistry.class);
    @Override
    public void run() {
        logger.info("Registry the service.");
        Config conf = Config.getConf();
        ServiceRegistry registry = new ServiceRegistry(conf.getValue(Config.ZOOKEEPER_ADDRESS));
        RpcServer rpcServer = new RpcServer(conf.getValue(Config.WORKER_ADDRESS),
                conf.getValue(Config.WORKER_RPC_PORT, 4086), registry);
        rpcServer.start();
    }
}
