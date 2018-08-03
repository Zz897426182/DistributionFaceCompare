package com.hzgc.compare.rpc.util;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZookeeperClient {
    private static final Logger logger = LoggerFactory.getLogger(ZookeeperClient.class);
    protected CuratorFramework zkClient;
    private String zkAddress;

    protected ZookeeperClient(String zkAddress) {
        this.zkAddress = zkAddress;
        this.zkClient = connectZookeeper();
    }

    private CuratorFramework connectZookeeper() {
        CuratorFramework zkClient;
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(RpcConfig.getCuratorBaseSleepTime(), RpcConfig.getCuratorMaxRetries());
        zkClient = CuratorFrameworkFactory
                .builder()
                .connectString(zkAddress)
                .retryPolicy(retryPolicy)
                .sessionTimeoutMs(RpcConfig.getZkSessionTime())
                .connectionTimeoutMs(RpcConfig.getZkConnectTime())
                .build();
        zkClient.start();
        try {
            zkClient.checkExists().forPath(Constant.ZK_REGISTRY_ROOT_PATH);
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info("Connect zookeeper successfull, zk address is {} ", zkAddress);
        return zkClient;
    }

    public void stop() {
        if (zkClient != null) {
            zkClient.close();
        }
    }
}
