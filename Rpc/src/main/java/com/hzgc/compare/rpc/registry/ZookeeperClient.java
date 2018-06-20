package com.hzgc.compare.rpc.registry;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ZookeeperClient {
    private static final Logger logger = LoggerFactory.getLogger(ZookeeperClient.class);
    CuratorFramework zkClient;
    private String zkAddress;

    ZookeeperClient(String zkAddress) {
        this.zkAddress = zkAddress;
        this.zkClient = connectZookeeper();
    }

    private CuratorFramework connectZookeeper() {
        CuratorFramework zkClient;
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        zkClient = CuratorFrameworkFactory
                .builder()
                .connectString(zkAddress)
                .retryPolicy(retryPolicy)
                .namespace(Constant.ZK_REGISTRY_ROOT_NAMESPACE)
                .build();
        zkClient.start();
        logger.info("Connect zookeeper successfull, zk address is {} ", zkAddress);
        return zkClient;
    }
}
