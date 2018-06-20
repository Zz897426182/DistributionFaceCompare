package com.hzgc.compare.rpc.registry;

import com.google.common.base.Strings;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceRegistry extends ZookeeperClient {
    private static final Logger logger = LoggerFactory.getLogger(ServiceRegistry.class);

    public ServiceRegistry(String zkAddress) {
        super(zkAddress);
    }

    public void register(String data) {
        if (!Strings.isNullOrEmpty(data)) {
            String flag = createZnode(data);
            if (!Strings.isNullOrEmpty(flag) && data.contains(Constant.ZK_REGISTRY_WORKER_PATH)) {
                logger.info("Create znode {} successfull", flag);
            }
        }
    }


    private String createZnode(String data) {
        try {
            return zkClient.create()
                    .creatingParentContainersIfNeeded()
                    .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                    .forPath(Constant.ZK_REGISTRY_WORKER_PATH, data.getBytes());
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return null;
    }
}
